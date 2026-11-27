package csc227.ai.task1;

import csc227.ai.task4.ScheduleResult;

import java.util.List;
import java.util.Locale;

/** Gantt chart, process table, average waiting time, and average turnaround time. */
public final class ProcessReport {
    private ProcessReport() {
    }

    public static String format(ScheduleResult result) {
        StringBuilder out = new StringBuilder();
        out.append("Gantt chart\n");
        out.append(gantt(result.slices()));
        out.append('\n');
        out.append("Process ID  Burst  Start  Termination  Waiting  Turnaround\n");
        for (PCB pcb : result.finished()) {
            out.append(String.format(Locale.US, "%-11d %-6d %-6d %-12d %-8d %d%n",
                    Integer.valueOf(pcb.pid()), Integer.valueOf(pcb.burst()),
                    Integer.valueOf(pcb.firstStart()), Integer.valueOf(pcb.completion()),
                    Integer.valueOf(pcb.waiting()), Integer.valueOf(pcb.turnaround())));
        }
        out.append('\n');
        out.append(String.format(Locale.US, "Average waiting time    : %.2f ms%n",
                Double.valueOf(result.averageWaiting())));
        out.append(String.format(Locale.US, "Average turnaround time : %.2f ms%n",
                Double.valueOf(result.averageTurnaround())));
        return out.toString();
    }

    private static String gantt(List<Slice> slices) {
        if (slices.isEmpty()) {
            return "(no process ran)\n";
        }
        int[] width = new int[slices.size()];
        for (int i = 0; i < slices.size(); i++) {
            width[i] = Math.max(("P" + slices.get(i).pid).length() + 2, 4);
        }
        StringBuilder names = new StringBuilder();
        names.append('|');
        for (int i = 0; i < slices.size(); i++) {
            names.append(center("P" + slices.get(i).pid, width[i]));
            names.append('|');
        }
        StringBuilder times = new StringBuilder();
        int column = 0;
        place(times, column, Integer.toString(slices.get(0).start));
        for (int i = 0; i < slices.size(); i++) {
            column += width[i] + 1;
            place(times, column, Integer.toString(slices.get(i).end));
        }
        return names.toString() + "\n" + times.toString() + "\n";
    }

    private static void place(StringBuilder target, int column, String text) {
        while (target.length() < column) {
            target.append(' ');
        }
        if (target.length() > column) {
            target.append(' ');
        }
        target.append(text);
    }

    private static String center(String text, int width) {
        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        StringBuilder padded = new StringBuilder();
        for (int i = 0; i < left; i++) {
            padded.append(' ');
        }
        padded.append(text);
        for (int i = 0; i < right; i++) {
            padded.append(' ');
        }
        return padded.toString();
    }
}
