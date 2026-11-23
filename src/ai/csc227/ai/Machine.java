package csc227.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds the job queue, the ready queue, and the 8192 MB memory counter.
 *
 * <p>The reader thread fills the job queue. The loader thread is the only
 * thread that moves a process from WAITING to READY, and it does that only
 * when {@code used + request <= 8192}. The scheduler thread (the main thread)
 * chooses who runs. One lock covers every change so the three threads do not
 * corrupt the queues.
 */
public final class Machine {
    public static final int MEMORY_CAPACITY_MB = 8192;

    private final Object lock = new Object();
    private final List<PCB> jobQueue = new ArrayList<PCB>();
    private final ArrayDeque<PCB> ready = new ArrayDeque<PCB>();
    private final List<PCB> all = new ArrayList<PCB>();
    private final List<Slice> slices = new ArrayList<Slice>();
    private final List<String> trace = new ArrayList<String>();
    private final List<String> rejected = new ArrayList<String>();

    private int usedMemory;
    private int clock;
    private boolean threaded;
    private boolean stop;
    private boolean admissionNeeded;
    private boolean admissionDone;
    private Thread loader;

    public void loadJobs(List<PCB> jobs) {
        synchronized (lock) {
            if (!all.isEmpty()) {
                throw new IllegalStateException("Jobs were already loaded into this machine");
            }
            if (jobs.size() > 30) {
                throw new IllegalArgumentException(
                        "The project allows at most 30 jobs, found " + jobs.size());
            }
            for (int i = 0; i < jobs.size(); i++) {
                PCB copy = jobs.get(i).copy();
                trace.add(Thread.currentThread().getName() + " created " + copy.jobName()
                        + " (pid " + copy.pid() + ", burst " + copy.burst()
                        + " ms, memory " + copy.memory() + " MB), state " + ProcessState.NEW);
                copy.setState(ProcessState.WAITING);
                trace.add(copy.jobName()
                        + " joined the job queue, state WAITING (waiting for memory)");
                jobQueue.add(copy);
                all.add(copy);
            }
        }
    }

    public void startLoader() {
        synchronized (lock) {
            if (loader != null) {
                return;
            }
            threaded = true;
            loader = new Thread(new Runnable() {
                @Override
                public void run() {
                    loaderLoop();
                }
            }, "job-loader");
            loader.start();
        }
    }

    private void loaderLoop() {
        try {
            synchronized (lock) {
                while (!stop) {
                    while (!admissionNeeded && !stop) {
                        lock.wait();
                    }
                    if (stop) {
                        break;
                    }
                    admitUnlocked();
                    admissionNeeded = false;
                    admissionDone = true;
                    lock.notifyAll();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("job-loader was interrupted", ex);
        }
    }

    /**
     * Asks the loader to admit every waiting job that fits. When the loader
     * thread is not running, admission happens on the caller.
     */
    public void requestAdmission() {
        synchronized (lock) {
            if (!threaded) {
                admitUnlocked();
                return;
            }
            admissionDone = false;
            admissionNeeded = true;
            lock.notifyAll();
            while (!admissionDone && !stop) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("scheduler was interrupted", ex);
                }
            }
        }
    }

    public void stopLoader() {
        Thread joining;
        synchronized (lock) {
            stop = true;
            lock.notifyAll();
            joining = loader;
        }
        if (joining != null) {
            try {
                joining.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while stopping job-loader", ex);
            }
        }
    }

    private void admitUnlocked() {
        Iterator<PCB> it = jobQueue.iterator();
        while (it.hasNext()) {
            PCB pcb = it.next();
            if (pcb.memory() > MEMORY_CAPACITY_MB) {
                it.remove();
                rejected.add(pcb.jobName() + " (pid " + pcb.pid() + ") needs " + pcb.memory()
                        + " MB, which is more than main memory (" + MEMORY_CAPACITY_MB + " MB)");
                trace.add(Thread.currentThread().getName() + " rejected " + pcb.jobName()
                        + ": memory request is larger than main memory");
                continue;
            }
            if (usedMemory + pcb.memory() <= MEMORY_CAPACITY_MB) {
                usedMemory += pcb.memory();
                pcb.setState(ProcessState.READY);
                ready.addLast(pcb);
                it.remove();
                trace.add(Thread.currentThread().getName() + " admitted " + pcb.jobName()
                        + ", state READY, used memory " + usedMemory + "/" + MEMORY_CAPACITY_MB + " MB");
            }
        }
    }

    public boolean hasReady() {
        synchronized (lock) {
            return !ready.isEmpty();
        }
    }

    public PCB pollReady() {
        synchronized (lock) {
            return ready.pollFirst();
        }
    }

    /** Non-preemptive SJF: smallest remaining burst, then earlier file order. */
    public PCB takeShortest() {
        synchronized (lock) {
            PCB best = null;
            for (PCB pcb : ready) {
                if (best == null
                        || pcb.remaining() < best.remaining()
                        || (pcb.remaining() == best.remaining() && pcb.fileOrder() < best.fileOrder())) {
                    best = pcb;
                }
            }
            if (best != null) {
                ready.remove(best);
            }
            return best;
        }
    }

    public void requeue(PCB pcb) {
        synchronized (lock) {
            pcb.setState(ProcessState.READY);
            ready.addLast(pcb);
            trace.add(pcb.jobName() + " preempted by the quantum, state READY, remaining burst "
                    + pcb.remaining() + " ms");
        }
    }

    public void runFor(PCB pcb, int units) {
        synchronized (lock) {
            if (units <= 0) {
                throw new IllegalArgumentException("A CPU slice must be a positive number of milliseconds");
            }
            if (pcb.remaining() <= 0) {
                throw new IllegalStateException(pcb.jobName() + " has no remaining burst");
            }
            int step = Math.min(units, pcb.remaining());
            pcb.setState(ProcessState.RUNNING);
            pcb.markDispatched(clock);
            int burstAtStart = pcb.remaining();
            int start = clock;
            pcb.consume(step);
            clock += step;
            slices.add(new Slice(pcb.jobName(), pcb.pid(), start, clock, burstAtStart, pcb.remaining()));
            trace.add("t=" + start + ".." + clock + " " + pcb.jobName()
                    + " RUNNING, burst " + burstAtStart + " -> " + pcb.remaining());
            if (pcb.remaining() == 0) {
                pcb.complete(clock);
            }
        }
    }

    public void release(PCB pcb) {
        synchronized (lock) {
            usedMemory -= pcb.memory();
            if (usedMemory < 0) {
                throw new IllegalStateException("Memory accounting became negative");
            }
            trace.add(pcb.jobName() + " state TERMINATED, released " + pcb.memory()
                    + " MB, free memory " + (MEMORY_CAPACITY_MB - usedMemory) + " MB");
        }
    }

    public ScheduleResult result(String algorithm) {
        synchronized (lock) {
            List<PCB> done = new ArrayList<PCB>();
            for (PCB pcb : all) {
                if (pcb.state() == ProcessState.TERMINATED) {
                    done.add(pcb);
                }
            }
            return new ScheduleResult(algorithm, done, slices, rejected, trace, clock);
        }
    }
}
