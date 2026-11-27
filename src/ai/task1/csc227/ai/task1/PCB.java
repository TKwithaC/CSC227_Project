package csc227.ai.task1;

/**
 * Process control block. The handout requires the process id, state, CPU burst,
 * priority, memory, waiting time, and turnaround time. The other fields are
 * bookkeeping for the 1 ms clock, aging, and starvation.
 */
public final class PCB {
    private final int pid;
    private final int burst;
    private final int basePriority;
    private final int memory;
    private final int fileOrder;
    private int remaining;
    private int priority;
    private int agedSteps;
    private ProcessState state;
    private int readyEnteredAt = -1;
    private int readyCohort;
    private int firstStart = -1;
    private int completion = -1;
    private int sliceStart = -1;
    private int sliceBurstStart;
    private int ranThisStay;
    private boolean starved;

    public PCB(int pid, int burst, int priority, int memory, int fileOrder) {
        this.pid = pid;
        this.burst = burst;
        this.basePriority = priority;
        this.priority = priority;
        this.memory = memory;
        this.fileOrder = fileOrder;
        this.remaining = burst;
        this.state = ProcessState.NEW;
    }

    public PCB copy() {
        return new PCB(pid, burst, basePriority, memory, fileOrder);
    }

    public void enterReady(int time, int cohort) {
        state = ProcessState.READY;
        readyEnteredAt = time;
        readyCohort = cohort;
        agedSteps = 0;
        ranThisStay = 0;
    }

    public void dispatch(int time) {
        state = ProcessState.RUNNING;
        if (firstStart < 0) {
            firstStart = time;
        }
        sliceStart = time;
        sliceBurstStart = remaining;
        ranThisStay = 0;
    }

    public void consumeOne() {
        remaining--;
        ranThisStay++;
    }

    public void suspend() {
        state = ProcessState.SUSPENDED;
    }

    public void complete(int time) {
        state = ProcessState.TERMINATED;
        completion = time;
    }

    public void noteStarved() {
        starved = true;
    }

    /** Apply one aging step. Priority number decreases, and 1 is the best priority. */
    public void ageOneStep() {
        if (priority > 1) {
            priority--;
        }
        agedSteps++;
    }

    public int pid() {
        return pid;
    }

    public int burst() {
        return burst;
    }

    public int basePriority() {
        return basePriority;
    }

    public int priority() {
        return priority;
    }

    public int memory() {
        return memory;
    }

    public int fileOrder() {
        return fileOrder;
    }

    public int remaining() {
        return remaining;
    }

    public ProcessState state() {
        return state;
    }

    public int readyEnteredAt() {
        return readyEnteredAt;
    }

    public int readyCohort() {
        return readyCohort;
    }

    public int agedSteps() {
        return agedSteps;
    }

    public int firstStart() {
        return firstStart;
    }

    public int completion() {
        return completion;
    }

    public int sliceStart() {
        return sliceStart;
    }

    public int sliceBurstStart() {
        return sliceBurstStart;
    }

    public int ranThisStay() {
        return ranThisStay;
    }

    public boolean starved() {
        return starved;
    }

    public int waiting() {
        return completion - burst;
    }

    public int turnaround() {
        return completion;
    }
}
