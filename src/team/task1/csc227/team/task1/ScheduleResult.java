package csc227.team.task1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Numbers and trace for one run of one algorithm. */
public final class ScheduleResult {
    private final String algorithm;
    private final List<PCB> finished;
    private final List<Slice> slices;
    private final List<String> rejected;
    private final List<String> trace;
    private final int makespan;

    public ScheduleResult(String algorithm, List<PCB> finished, List<Slice> slices,
                          List<String> rejected, List<String> trace, int makespan) {
        this.algorithm = algorithm;
        List<PCB> ordered = new ArrayList<PCB>(finished);
        Collections.sort(ordered, new Comparator<PCB>() {
            @Override
            public int compare(PCB left, PCB right) {
                return left.fileOrder() - right.fileOrder();
            }
        });
        this.finished = Collections.unmodifiableList(ordered);
        this.slices = Collections.unmodifiableList(new ArrayList<Slice>(slices));
        this.rejected = Collections.unmodifiableList(new ArrayList<String>(rejected));
        this.trace = Collections.unmodifiableList(new ArrayList<String>(trace));
        this.makespan = makespan;
    }

    public String algorithm() {
        return algorithm;
    }

    public List<PCB> finished() {
        return finished;
    }

    public List<Slice> slices() {
        return slices;
    }

    public List<String> rejected() {
        return rejected;
    }

    public List<String> trace() {
        return trace;
    }

    public int makespan() {
        return makespan;
    }

    public double averageWaiting() {
        return average(new Pick() {
            @Override
            public int value(PCB pcb) {
                return pcb.waiting();
            }
        });
    }

    public double averageCompletion() {
        return average(new Pick() {
            @Override
            public int value(PCB pcb) {
                return pcb.completion();
            }
        });
    }

    public double averageTurnaround() {
        return average(new Pick() {
            @Override
            public int value(PCB pcb) {
                return pcb.turnaround();
            }
        });
    }

    public double averageResponse() {
        return average(new Pick() {
            @Override
            public int value(PCB pcb) {
                return pcb.response();
            }
        });
    }

    /**
     * CPU utilization = busy time / makespan. With no I/O and no dispatch
     * delay the CPU stays busy until the last job finishes, so this is 100%
     * whenever at least one job runs.
     */
    public double cpuUtilization() {
        if (makespan == 0) {
            return 0;
        }
        int busy = 0;
        for (PCB pcb : finished) {
            busy += pcb.burst();
        }
        return (100.0 * busy) / makespan;
    }

    /** Processes completed per millisecond. */
    public double throughput() {
        if (makespan == 0) {
            return 0;
        }
        return finished.size() / (double) makespan;
    }

    private double average(Pick pick) {
        if (finished.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (PCB pcb : finished) {
            sum += pick.value(pcb);
        }
        return sum / finished.size();
    }

    private interface Pick {
        int value(PCB pcb);
    }
}
