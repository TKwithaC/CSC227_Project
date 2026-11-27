package csc227.ai.task4;

import csc227.ai.task1.JobReader;
import csc227.ai.task1.PCB;
import csc227.ai.task1.Slice;
import csc227.ai.task2.LongTermScheduler;
import csc227.ai.task3.Intervention;
import csc227.ai.task3.MediumTermScheduler;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The main thread. It advances the 1 ms clock and asks the selected algorithm
 * which ready process runs. Thread 2 and thread 3 wait on the same lock and
 * take one step when this thread asks them.
 */
public final class Machine {
    public static final int MEMORY_MB = 2048;
    private static final int TIME_LIMIT = 1000000;

    private final Object lock = new Object();
    private final List<PCB> jobQueue = new ArrayList<PCB>();
    private final ArrayDeque<PCB> ready = new ArrayDeque<PCB>();
    private final ArrayDeque<PCB> suspended = new ArrayDeque<PCB>();
    private final List<PCB> all = new ArrayList<PCB>();
    private final List<Slice> slices = new ArrayList<Slice>();
    private final List<Intervention> interventions = new ArrayList<Intervention>();

    private final boolean mediumOn;
    private int usedMemory;
    private int time;
    private PCB running;
    private boolean stop;
    private Phase phase = Phase.IDLE;
    private boolean phaseDone;
    private boolean phaseWorked;
    private RuntimeException helperFailure;
    private Thread longTerm;
    private Thread mediumTerm;

    private enum Phase {
        IDLE, LONG, MEDIUM
    }

    public Machine(boolean mediumOn) {
        this.mediumOn = mediumOn;
    }

    public static ScheduleResult run(Path file, Policy policy, boolean mediumOn) {
        Machine machine = new Machine(mediumOn);
        JobReader.read(file, machine.jobQueue, machine.all, machine.lock);
        machine.startHelpers();
        try {
            machine.clock(policy);
        } finally {
            machine.stopHelpers();
        }
        return machine.result(policy);
    }

    public List<PCB> jobQueue() {
        return jobQueue;
    }

    public ArrayDeque<PCB> readyQueue() {
        return ready;
    }

    public ArrayDeque<PCB> suspendQueue() {
        return suspended;
    }

    public int usedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(int usedMemory) {
        this.usedMemory = usedMemory;
    }

    public int time() {
        return time;
    }

    public boolean mediumEnabled() {
        return mediumOn;
    }

    public void record(Intervention event) {
        interventions.add(event);
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
                    phaseWorked = mine == Phase.LONG
                            ? LongTermScheduler.loadNext(Machine.this)
                            : MediumTermScheduler.step(Machine.this);
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
        while (callHelper(Phase.LONG)) {
            // every process arrives at time 0, so load each job that fits before the clock moves
        }
        callHelper(Phase.MEDIUM);
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
                Aging.apply(ready, time);
                Starvation.mark(ready, time);
            }
            callHelper(Phase.LONG);
            callHelper(Phase.MEDIUM);
            reconsiderShortest(policy);
            dispatchIfIdle(policy);
        }
    }

    private boolean finished() {
        return running == null && ready.isEmpty() && jobQueue.isEmpty() && suspended.isEmpty();
    }

    private void reconsiderShortest(Policy policy) {
        if (running == null || policy.quantum() > 0 || policy.aging()) {
            return;
        }
        List<PCB> candidates = new ArrayList<PCB>(ready);
        candidates.add(running);
        PCB best = policy.select(candidates);
        if (best == null || best == running) {
            return;
        }
        closeSlice();
        ready.remove(best);
        ready.addLast(running);
        running.enterReady(time, ready.size());
        best.dispatch(time);
        running = best;
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
            return new ScheduleResult(policy.name(), mediumOn, policy.aging(), all, slices, interventions);
        }
    }
}
