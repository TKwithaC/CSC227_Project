package csc227.ai.task3;

/** One medium-term swap. */
public final class Intervention {
    public final int time;
    public final double utilizationBefore;
    public final double utilizationAfter;
    public final String action;
    public final int pid;
    public final String stateBefore;
    public final String stateAfter;

    public Intervention(int time, double utilizationBefore, double utilizationAfter,
                        String action, int pid, String stateBefore, String stateAfter) {
        this.time = time;
        this.utilizationBefore = utilizationBefore;
        this.utilizationAfter = utilizationAfter;
        this.action = action;
        this.pid = pid;
        this.stateBefore = stateBefore;
        this.stateAfter = stateAfter;
    }
}
