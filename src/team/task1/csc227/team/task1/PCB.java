package csc227.team.task1;

/**
 * Process control block. The project asks for the process id, the state,
 * the CPU burst, and the memory requirement. The extra fields are the
 * Chapter 5 measurements: arrival, completion, waiting, turnaround, and
 * response time.
 */
public final class PCB {
    private final String jobName;
    private final int pid;
    private final int burst;
    private final int memory;
    private final int fileOrder;
    private final int arrival;
    private int remaining;
    private ProcessState state;
    private int firstStart = -1;
    private int completion = -1;
    private int waiting;
    private int turnaround;
    private int response = -1;

    public PCB(String jobName, int pid, int burst, int memory, int fileOrder) {
        this.jobName = jobName;
        this.pid = pid;
        this.burst = burst;
        this.memory = memory;
        this.fileOrder = fileOrder;
        this.arrival = 0;
        this.remaining = burst;
        this.state = ProcessState.NEW;
    }

    public PCB copy() {
        return new PCB(jobName, pid, burst, memory, fileOrder);
    }

    void setState(ProcessState next) {
        this.state = next;
    }

    void markDispatched(int time) {
        if (firstStart < 0) {
            firstStart = time;
        }
    }

    void consume(int units) {
        remaining -= units;
    }

    void complete(int time) {
        state = ProcessState.TERMINATED;
        completion = time;
        turnaround = completion - arrival;
        waiting = turnaround - burst;
        response = firstStart - arrival;
    }

    public String jobName() {
        return jobName;
    }

    public int pid() {
        return pid;
    }

    public int burst() {
        return burst;
    }

    public int memory() {
        return memory;
    }

    public int fileOrder() {
        return fileOrder;
    }

    public int arrival() {
        return arrival;
    }

    public int remaining() {
        return remaining;
    }

    public ProcessState state() {
        return state;
    }

    public int firstStart() {
        return firstStart;
    }

    public int completion() {
        return completion;
    }

    public int waiting() {
        return waiting;
    }

    public int turnaround() {
        return turnaround;
    }

    public int response() {
        return response;
    }
}
