package csc227.ai;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Job queue, ready queue, suspend queue, and the 2048 MB counter.
 *
 * <p>Three threads share this object. {@code job-reader} parses the file and
 * stops. {@code long-term} loads the next job when it fits. {@code medium-term}
 * swaps when it is enabled. The main thread is the short-term scheduler: it
 * advances a 1 ms clock and asks the selected policy which ready process runs.
 * One lock orders those steps, so the result does not depend on which thread
 * the JVM happens to run first.
 */
public final class Machine {
    public static final int MEMORY_MB = 2048;
    private static final double SWAP_OUT_ABOVE = 90.0;
    private static final double SWAP_IN_BELOW = 70.0;
    private static final int TIME_LIMIT = 1000000;

    private final Object lock = new Object();
    private final List<PCB> jobQueue = new ArrayList<PCB>();
    private final ArrayDeque<PCB> ready = new ArrayDeque<PCB>();
    private final ArrayDeque<PCB> suspended = new ArrayDeque<PCB>();
    private final List<PCB> all = new ArrayList<PCB>();
    private final List<Slice> slices = new ArrayList<Slice>();
    private final List<Intervention> interventions = new ArrayList<Intervention>();

    private final boolean mediumEnabled;
    private int usedMemory;
    private int time;
    private PCB running;
    private boolean stop;
    private boolean readerFinished;
    private Phase phase = Phase.IDLE;
    private boolean phaseDone;
    private boolean phaseWorked;
    private RuntimeException helperFailure;
    private Thread longTerm;
    private Thread mediumTerm;

    private enum Phase {
        IDLE, LONG, MEDIUM
    }

    public Machine(boolean mediumEnabled) {
        this.mediumEnabled = mediumEnabled;
    }

    public static ScheduleResult run(Path file, Policy policy, boolean mediumEnabled) {
        Machine machine = new Machine(mediumEnabled);
        machine.readOnThread(file);
        machine.startHelpers();
        try {
            machine.clock(policy);
        } finally {
            machine.stopHelpers();
        }
        return machine.result(policy);
    }

    private void readOnThread(final Path file) {
        final RuntimeException[] failure = new RuntimeException[1];
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PCB> jobs = JobFile.parse(file);
                    synchronized (lock) {
                        if (!all.isEmpty()) {
                            throw new IllegalStateException("Processes were already loaded");
                        }
                        for (int i = 0; i < jobs.size(); i++) {
                            PCB copy = jobs.get(i).copy();
                            copyAsNew(copy);
                        }
                        readerFinished = true;
                    }
                } catch (RuntimeException ex) {
                    failure[0] = ex;
                }
            }
        }, "job-reader");
        reader.start();
        try {
            reader.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("job-reader was interrupted", ex);
        }
        if (failure[0] != null) {
            throw failure[0];
        }
        synchronized (lock) {
            if (!readerFinished) {
                throw new IllegalStateException("job-reader did not finish");
            }
        }
    }

    private void copyAsNew(PCB pcb) {
        jobQueue.add(pcb);
        all.add(pcb);
    }

    private void startHelpers() {
        longTerm = new Thread(new Runnable() {
            @Override
            public void run() {
                helperLoop(Phase.LONG);
            }
        }, "long-term");
        mediumTerm = new Thread(new Runnable() {
            @Override
            public void run() {
                helperLoop(Phase.MEDIUM);
            }
        }, "medium-term");
        longTerm.start();
        mediumTerm.start();
    }

    private void helperLoop(Phase mine) {
        synchronized (lock) {
            while (!stop) {
                while (phase != mine && !stop) {
                    waitOnLock();
                }
                if (stop) {
                    break;
                }
                try {
                    phaseWorked = mine == Phase.LONG ? longTermOnce() : mediumOnce();
                } catch (RuntimeException ex) {
                    helperFailure = ex;
                    phaseWorked = false;
                }
                phase = Phase.IDLE;
                phaseDone = true;
                lock.notifyAll();
            }
        }
    }

    private boolean callHelper(Phase wanted) {
        synchronized (lock) {
            if (helperFailure != null) {
                throw helperFailure;
            }
            phaseDone = false;
            phaseWorked = false;
            phase = wanted;
            lock.notifyAll();
            while (!phaseDone && helperFailure == null) {
                waitOnLock();
            }
            if (helperFailure != null) {
                throw helperFailure;
            }
            return phaseWorked;
        }
    }

    private void clock(Policy policy) {
        settle();
        dispatchIfIdle(policy);
        while (true) {
            if (time > TIME_LIMIT) {
                throw new IllegalStateException("The simulation did not finish");
            }
            if (running == null) {
                if (finished()) {
                    return;
                }
                throw new IllegalStateException(
                        "No ready process, and the next job cannot enter memory. Time " + time + " ms");
            }
            executeOneMillisecond(policy);
            if (policy.aging()) {
                ageReady();
                markStarvation();
            }
            settle();
            dispatchIfIdle(policy);
        }
    }

    private boolean finished() {
        return running == null && ready.isEmpty() && jobQueue.isEmpty() && suspended.isEmpty();
    }

    /**
     * Let the long-term and medium-term threads act until neither one can move
     * a process. A swap-in is refused when it would push utilization back above
     * 90%, so the two thresholds cannot undo each other in the same millisecond.
     */
    private void settle() {
        int guard = 0;
        boolean moved = true;
        while (moved) {
            if (++guard > 10000) {
                throw new IllegalStateException("The memory schedulers did not settle");
            }
            boolean admitted = callHelper(Phase.LONG);
            boolean swapped = callHelper(Phase.MEDIUM);
            moved = admitted || swapped;
        }
    }

    /** Load the next job only. A job that does not fit stays at the head of the job queue. */
    private boolean longTermOnce() {
        if (jobQueue.isEmpty()) {
            return false;
        }
        PCB next = jobQueue.get(0);
        if (next.memory() > MEMORY_MB) {
            throw new IllegalArgumentException(
                    "Process " + next.pid() + " needs " + next.memory()
                            + " MB, which is more than main memory (" + MEMORY_MB + " MB)");
        }
        if (usedMemory + next.memory() > MEMORY_MB) {
            return false;
        }
        jobQueue.remove(0);
        usedMemory += next.memory();
        ready.addLast(next);
        next.enterReady(time, ready.size());
        return true;
    }

    private boolean mediumOnce() {
        if (!mediumEnabled) {
            return false;
        }
        double before = utilization();
        if (before > SWAP_OUT_ABOVE) {
            PCB victim = largestReady();
            if (victim == null) {
                return false;
            }
            ready.remove(victim);
            usedMemory -= victim.memory();
            String stateBefore = victim.state().label();
            victim.suspend();
            suspended.addLast(victim);
            interventions.add(new Intervention(time, before, utilization(), "Swap out",
                    victim.pid(), stateBefore, victim.state().label()));
            return true;
        }
        if (before < SWAP_IN_BELOW) {
            PCB returning = firstThatCanReturn();
            if (returning == null) {
                return false;
            }
            suspended.remove(returning);
            usedMemory += returning.memory();
            String stateBefore = returning.state().label();
            ready.addLast(returning);
            returning.enterReady(time, ready.size());
            interventions.add(new Intervention(time, before, utilization(), "Swap in",
                    returning.pid(), stateBefore, returning.state().label()));
            return true;
        }
        return false;
    }

    private PCB largestReady() {
        PCB best = null;
        for (PCB pcb : ready) {
            if (best == null || pcb.memory() > best.memory()
                    || (pcb.memory() == best.memory() && pcb.fileOrder() > best.fileOrder())) {
                best = pcb;
            }
        }
        return best;
    }

    /**
     * Earliest swapped-out process that fits and leaves utilization at or below 90%.
     * Swapping it in above 90% would make the medium-term thread swap it straight back out.
     */
    private PCB firstThatCanReturn() {
        for (PCB pcb : suspended) {
            if (usedMemory + pcb.memory() <= MEMORY_MB) {
                double after = (100.0 * (usedMemory + pcb.memory())) / MEMORY_MB;
                if (after <= SWAP_OUT_ABOVE) {
                    return pcb;
                }
            }
        }
        return null;
    }

    private void dispatchIfIdle(Policy policy) {
        if (running != null || ready.isEmpty()) {
            return;
        }
        List<PCB> snapshot = new ArrayList<PCB>(ready);
        PCB chosen = policy.select(snapshot);
        if (chosen == null || !ready.contains(chosen)) {
            throw new IllegalStateException(policy.name() + " did not select a ready process");
        }
        ready.remove(chosen);
        chosen.dispatch(time);
        running = chosen;
    }

    private void executeOneMillisecond(Policy policy) {
        running.consumeOne();
        time++;
        if (running.remaining() == 0) {
            closeSlice();
            usedMemory -= running.memory();
            running.complete(time);
            running = null;
            return;
        }
        if (policy.quantum() > 0 && running.ranThisStay() >= policy.quantum()) {
            closeSlice();
            ready.addLast(running);
            running.enterReady(time, ready.size());
            running = null;
        }
    }

    private void closeSlice() {
        slices.add(new Slice(running.pid(), running.sliceStart(), time));
    }

    /** Every 4 ms spent in this ready stay, decrease the priority number by 1, down to 1. */
    private void ageReady() {
        for (PCB pcb : ready) {
            int waited = time - pcb.readyEnteredAt();
            int due = waited / 4;
            while (pcb.agedSteps() < due) {
                pcb.ageOneStep();
            }
        }
    }

    /** Starved when this ready stay has lasted more than N × 5 ms. N was fixed on entry. */
    private void markStarvation() {
        for (PCB pcb : ready) {
            int waited = time - pcb.readyEnteredAt();
            if (waited > pcb.readyCohort() * 5) {
                pcb.noteStarved();
            }
        }
    }

    private double utilization() {
        return (100.0 * usedMemory) / MEMORY_MB;
    }

    private void stopHelpers() {
        Thread first;
        Thread second;
        synchronized (lock) {
            stop = true;
            lock.notifyAll();
            first = longTerm;
            second = mediumTerm;
        }
        join(first);
        join(second);
    }

    private void join(Thread thread) {
        if (thread == null) {
            return;
        }
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(thread.getName() + " was interrupted", ex);
        }
    }

    private void waitOnLock() {
        try {
            lock.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for a scheduler thread", ex);
        }
    }

    private ScheduleResult result(Policy policy) {
        synchronized (lock) {
            return new ScheduleResult(policy.name(), mediumEnabled, policy.aging(),
                    all, slices, interventions);
        }
    }
}
