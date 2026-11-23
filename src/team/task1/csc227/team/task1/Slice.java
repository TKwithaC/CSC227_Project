package csc227.team.task1;

/** One continuous interval on the CPU, used for the Gantt chart and the burst log. */
public final class Slice {
    public final String jobName;
    public final int pid;
    public final int start;
    public final int end;
    public final int burstStart;
    public final int burstEnd;

    public Slice(String jobName, int pid, int start, int end, int burstStart, int burstEnd) {
        this.jobName = jobName;
        this.pid = pid;
        this.start = start;
        this.end = end;
        this.burstStart = burstStart;
        this.burstEnd = burstEnd;
    }
}
