package csc227.team.task4;

import csc227.team.task1.Machine;
import csc227.team.task1.PCB;
import csc227.team.task1.ScheduleResult;
import csc227.team.task1.Slice;

import java.util.List;
import java.util.Locale;

/** Text tables and a Gantt chart. Both are printed because the project asks for both. */
public final class TextReport {
    private TextReport() {
    }

    public static String format(ScheduleResult result) {
        StringBuilder out = new StringBuilder();
        out.append("Algorithm: ").append(result.algorithm()).append('\n');
        out.append("Main memory: ").append(Machine.MEMORY_CAPACITY_MB).append(" MB\n");
        out.append("Dispatch latency: 0 ms\n");
        out.append("Arrival time of every admitted job: 0\n\n");

        if (!result.rejected().isEmpty()) {
            out.append("Not admitted:\n");
            for (String reason : result.rejected()) {
                out.append("  - ").append(reason).append('\n');
            }
            out.append('\n');
        }

        out.append("Gantt chart\n");
        out.append(gantt(result.slices()));
        out.append('\n');

        out.append("CPU slices (starting and stopping burst)\n");
        out.append(String.format(Locale.US, "%-8s %-8s %-10s %-6s %-14s %-12s %s%n",
                "Start", "End", "Job", "PID", "Burst start", "Burst end", "After slice"));
        for (Slice slice : result.slices()) {
            String after = slice.burstEnd == 0 ? "TERMINATED" : "READY";
            out.append(String.format(Locale.US, "%-8d %-8d %-10s %-6d %-14d %-12d %s%n",
                    Integer.valueOf(slice.start), Integer.valueOf(slice.end), slice.jobName,
                    Integer.valueOf(slice.pid), Integer.valueOf(slice.burstStart),
                    Integer.valueOf(slice.burstEnd), after));
        }
        out.append('\n');

        out.append("Per-process results\n");
        out.append(String.format(Locale.US,
                "%-8s %-6s %-8s %-8s %-8s %-12s %-10s %-12s %s%n",
                "Job", "PID", "Burst", "Memory", "Start", "Completion", "Waiting",
                "Turnaround", "Response"));
        for (PCB pcb : result.finished()) {
            out.append(String.format(Locale.US,
                    "%-8s %-6d %-8d %-8d %-8d %-12d %-10d %-12d %d%n",
                    pcb.jobName(), Integer.valueOf(pcb.pid()), Integer.valueOf(pcb.burst()),
                    Integer.valueOf(pcb.memory()), Integer.valueOf(pcb.firstStart()),
                    Integer.valueOf(pcb.completion()), Integer.valueOf(pcb.waiting()),
                    Integer.valueOf(pcb.turnaround()), Integer.valueOf(pcb.response())));
        }
        if (result.finished().isEmpty()) {
            out.append("(no process ran)\n");
        }
        out.append('\n');
        out.append(String.format(Locale.US, "Average waiting time    : %.2f ms%n",
                Double.valueOf(result.averageWaiting())));
        out.append(String.format(Locale.US, "Average completion time : %.2f ms%n",
                Double.valueOf(result.averageCompletion())));
        out.append(String.format(Locale.US, "Average turnaround time : %.2f ms%n",
                Double.valueOf(result.averageTurnaround())));
        out.append(String.format(Locale.US, "Average response time   : %.2f ms%n",
                Double.valueOf(result.averageResponse())));
        out.append(String.format(Locale.US, "CPU utilization         : %.2f%%%n",
                Double.valueOf(result.cpuUtilization())));
        out.append(String.format(Locale.US, "Throughput              : %.4f processes/ms%n",
                Double.valueOf(result.throughput())));
        out.append(String.format(Locale.US, "Makespan                : %d ms%n",
                Integer.valueOf(result.makespan())));
        out.append('\n');
        out.append("Execution trace\n");
        for (String line : result.trace()) {
            out.append("  ").append(line).append('\n');
        }
        return out.toString();
    }

    public static String comparison(List<ScheduleResult> results) {
        StringBuilder out = new StringBuilder();
        out.append("Comparison\n");
        out.append(String.format(Locale.US, "%-12s %-14s %-16s %-14s %-14s %-12s %s%n",
                "Algorithm", "Avg waiting", "Avg completion", "Avg turnaround", "Avg response",
                "CPU util", "Throughput"));
        for (ScheduleResult result : results) {
            String util = String.format(Locale.US, "%.2f%%", Double.valueOf(result.cpuUtilization()));
            out.append(String.format(Locale.US, "%-12s %-14.2f %-16.2f %-14.2f %-14.2f %-12s %.4f%n",
                    result.algorithm(), Double.valueOf(result.averageWaiting()),
                    Double.valueOf(result.averageCompletion()), Double.valueOf(result.averageTurnaround()),
                    Double.valueOf(result.averageResponse()), util,
                    Double.valueOf(result.throughput())));
        }
        out.append('\n');
        out.append("Waiting time    = completion - arrival - burst\n");
        out.append("Turnaround time = completion - arrival\n");
        out.append("Response time   = first dispatch - arrival\n");
        out.append("With arrival 0, turnaround time equals completion time.\n");
        return out.toString();
    }

    static String gantt(List<Slice> slices) {
        if (slices.isEmpty()) {
            return "(CPU was idle \u2014 nothing was admitted)\n";
        }
        int[] width = new int[slices.size()];
        for (int i = 0; i < slices.size(); i++) {
            width[i] = Math.max(slices.get(i).jobName.length() + 2, 4);
        }
        StringBuilder names = new StringBuilder();
        names.append('|');
        for (int i = 0; i < slices.size(); i++) {
            names.append(center(slices.get(i).jobName, width[i]));
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
        if (text.length() >= width) {
            return text;
        }
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
