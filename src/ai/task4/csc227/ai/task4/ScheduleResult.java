package csc227.ai.task4;

import csc227.ai.task1.PCB;
import csc227.ai.task1.Slice;
import csc227.ai.task3.Intervention;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** One run: the slices, the processes, and any medium-term interventions. */
public final class ScheduleResult {
    private final String algorithm;
    private final boolean mediumEnabled;
    private final boolean showStarvation;
    private final List<PCB> finished;
    private final List<Slice> slices;
    private final List<Intervention> interventions;

    public ScheduleResult(String algorithm, boolean mediumEnabled, boolean showStarvation,
                          List<PCB> finished, List<Slice> slices, List<Intervention> interventions) {
        this.algorithm = algorithm;
        this.mediumEnabled = mediumEnabled;
        this.showStarvation = showStarvation;
        List<PCB> ordered = new ArrayList<PCB>(finished);
        Collections.sort(ordered, new Comparator<PCB>() {
            @Override
            public int compare(PCB left, PCB right) {
                return left.fileOrder() - right.fileOrder();
            }
        });
        this.finished = Collections.unmodifiableList(ordered);
        this.slices = Collections.unmodifiableList(new ArrayList<Slice>(slices));
        this.interventions = Collections.unmodifiableList(new ArrayList<Intervention>(interventions));
    }

    public String algorithm() {
        return algorithm;
    }

    public boolean mediumEnabled() {
        return mediumEnabled;
    }

    public boolean showStarvation() {
        return showStarvation;
    }

    public List<PCB> finished() {
        return finished;
    }

    public List<Slice> slices() {
        return slices;
    }

    public List<Intervention> interventions() {
        return interventions;
    }

    public double averageWaiting() {
        return average(true);
    }

    public double averageTurnaround() {
        return average(false);
    }

    private double average(boolean waiting) {
        if (finished.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (PCB pcb : finished) {
            sum += waiting ? pcb.waiting() : pcb.turnaround();
        }
        return sum / finished.size();
    }
}
