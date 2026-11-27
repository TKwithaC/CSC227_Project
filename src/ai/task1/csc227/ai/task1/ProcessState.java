package csc227.ai.task1;

/** Process states used by the three schedulers. SUSPENDED is the swapped-out state. */
public enum ProcessState {
    NEW,
    READY,
    RUNNING,
    SUSPENDED,
    TERMINATED;

    public String label() {
        if (this == NEW) {
            return "New";
        }
        if (this == READY) {
            return "Ready";
        }
        if (this == RUNNING) {
            return "Running";
        }
        if (this == SUSPENDED) {
            return "Suspended";
        }
        return "Terminated";
    }
}
