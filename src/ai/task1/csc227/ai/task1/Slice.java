package csc227.ai.task1;

/** One continuous CPU allocation, from the moment a process is dispatched until it leaves the CPU. */
public final class Slice {
    public final int pid;
    public final int start;
    public final int end;

    public Slice(int pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }
}
