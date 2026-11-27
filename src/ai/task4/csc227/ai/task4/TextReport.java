package csc227.ai.task4;

import csc227.ai.task1.ProcessReport;
import csc227.ai.task3.Intervention;
import csc227.ai.task3.SwapReport;

/** Puts the task 1 table, the task 3 swap lines, and the task 4 starvation line together. */
public final class TextReport {
    private TextReport() {
    }

    public static String format(ScheduleResult result) {
        StringBuilder out = new StringBuilder();
        out.append("Algorithm: ").append(result.algorithm()).append('\n');
        out.append("Medium-term scheduler: ").append(result.mediumEnabled() ? "enabled" : "disabled").append('\n');
        out.append("Main memory: ").append(Machine.MEMORY_MB).append(" MB\n");
        out.append("Context switch: 0 ms\n\n");
        if (!result.interventions().isEmpty()) {
            for (Intervention event : result.interventions()) {
                out.append(SwapReport.format(event)).append("\n\n");
            }
        }
        out.append(ProcessReport.format(result));
        if (result.showStarvation()) {
            out.append('\n');
            out.append(Starvation.line(result.finished()));
        }
        return out.toString();
    }
}
