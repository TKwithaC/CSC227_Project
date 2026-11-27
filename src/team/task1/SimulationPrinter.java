package csc227.team.task1;

import java.util.Comparator;
import java.util.List;

public class SimulationPrinter {

    public static class GanttSlice {
        public final int pid;
        public final int startTime;
        public final int endTime;

        public GanttSlice(int pid, int startTime, int endTime) {
            this.pid = pid;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public static void printGanttChart(List<GanttSlice> slices) {
        System.out.println("\n==================== GANTT CHART ====================");
        if (slices == null || slices.isEmpty()) {
            System.out.println("No processes were executed.");
            return;
        }

        StringBuilder bar = new StringBuilder("|");
        StringBuilder timeline = new StringBuilder();
        timeline.append(slices.get(0).startTime);

        for (GanttSlice slice : slices) {
            String label = " P" + slice.pid + " ";
            bar.append(label).append("|");

            String endStr = String.valueOf(slice.endTime);
            int spaces = label.length() + 1 - endStr.length();
            timeline.append(" ".repeat(Math.max(1, spaces))).append(endStr);
        }

        System.out.println(bar);
        System.out.println(timeline);
        System.out.println("=====================================================\n");
    }

    public static void printProcessTableAndAverages(List<PCB> completedProcesses) {
        
        completedProcesses.sort(Comparator.comparingInt(PCB::getPid));

        System.out.println("=======================================================================================");
        System.out.printf("%-12s %-12s %-12s %-18s %-14s %-16s%n",
                "Process ID", "Burst Time", "Start Time", "Termination Time", "Waiting Time", "Turnaround Time");
        System.out.println("=======================================================================================");

        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (PCB p : completedProcesses) {
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();

            System.out.printf("%-12d %-12d %-12d %-18d %-14d %-16d%n",
                    p.getPid(),
                    p.getBurstTime(),
                    p.getStartTime(),
                    p.getTerminationTime(),
                    p.getWaitingTime(),
                    p.getTurnaroundTime());
        }

        System.out.println("=======================================================================================");
        
        int count = completedProcesses.size();
        double avgWaiting = (count > 0) ? (totalWaiting / count) : 0.0;
        double avgTurnaround = (count > 0) ? (totalTurnaround / count) : 0.0;

        System.out.printf("Average Waiting Time    : %.2f ms%n", avgWaiting);
        System.out.printf("Average Turnaround Time : %.2f ms%n", avgTurnaround);
        System.out.println("=======================================================================================\n");
    }
}
