package csc227.check;

import csc227.ai.Algorithms;
import csc227.ai.Slice;
import csc227.team.task1.JobFile;
import csc227.team.task4.Runner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Checks the reference solution and the four-task team build against the
 * same hand-worked schedules. Arrival time is 0. Waiting = completion - burst.
 */
public final class OracleTest {
    private static int failures;

    private OracleTest() {
    }

    public static void main(String[] args) throws Exception {
        String sample = read("samples/job.txt");
        String memory = read("samples/job-memory.txt");
        String tie = read("samples/job-tie.txt");
        String tooBig = read("samples/job-toobig.txt");

        checkCase("sample FCFS", sample, "FCFS",
                new String[]{"Job1", "Job2", "Job3", "Job4"},
                new int[][]{{0, 8, 8, 0}, {8, 18, 10, 0}, {18, 21, 3, 0}, {21, 28, 7, 0}},
                new int[]{8, 18, 21, 28}, new int[]{0, 8, 18, 21}, new int[]{0, 8, 18, 21});
        checkCase("sample SJF", sample, "SJF",
                new String[]{"Job3", "Job4", "Job1", "Job2"},
                new int[][]{{0, 3, 3, 0}, {3, 10, 7, 0}, {10, 18, 8, 0}, {18, 28, 10, 0}},
                new int[]{18, 28, 3, 10}, new int[]{10, 18, 0, 3}, new int[]{10, 18, 0, 3});
        checkCase("sample RR-3", sample, "RR-3",
                new String[]{"Job1", "Job2", "Job3", "Job4", "Job1", "Job2", "Job4", "Job1", "Job2", "Job4", "Job2"},
                new int[][]{
                        {0, 3, 8, 5}, {3, 6, 10, 7}, {6, 9, 3, 0}, {9, 12, 7, 4},
                        {12, 15, 5, 2}, {15, 18, 7, 4}, {18, 21, 4, 1}, {21, 23, 2, 0},
                        {23, 26, 4, 1}, {26, 27, 1, 0}, {27, 28, 1, 0}},
                new int[]{23, 28, 9, 27}, new int[]{15, 18, 6, 20}, new int[]{0, 3, 6, 9});
        checkCase("sample RR-5", sample, "RR-5",
                new String[]{"Job1", "Job2", "Job3", "Job4", "Job1", "Job2", "Job4"},
                new int[][]{
                        {0, 5, 8, 3}, {5, 10, 10, 5}, {10, 13, 3, 0}, {13, 18, 7, 2},
                        {18, 21, 3, 0}, {21, 26, 5, 0}, {26, 28, 2, 0}},
                new int[]{21, 26, 13, 28}, new int[]{13, 16, 10, 21}, new int[]{0, 5, 10, 13});

        checkCase("memory FCFS", memory, "FCFS",
                new String[]{"JobA", "JobC", "JobB", "JobD"},
                new int[][]{{0, 6, 6, 0}, {6, 11, 5, 0}, {11, 15, 4, 0}, {15, 17, 2, 0}},
                new int[]{6, 15, 11, 17}, new int[]{0, 11, 6, 15}, new int[]{0, 11, 6, 15});
        checkCase("memory SJF", memory, "SJF",
                new String[]{"JobC", "JobD", "JobA", "JobB"},
                new int[][]{{0, 5, 5, 0}, {5, 7, 2, 0}, {7, 13, 6, 0}, {13, 17, 4, 0}},
                new int[]{13, 17, 5, 7}, new int[]{7, 13, 0, 5}, new int[]{7, 13, 0, 5});
        checkCase("memory RR-3", memory, "RR-3",
                new String[]{"JobA", "JobC", "JobA", "JobC", "JobB", "JobD", "JobB"},
                new int[][]{
                        {0, 3, 6, 3}, {3, 6, 5, 2}, {6, 9, 3, 0}, {9, 11, 2, 0},
                        {11, 14, 4, 1}, {14, 16, 2, 0}, {16, 17, 1, 0}},
                new int[]{9, 17, 11, 16}, new int[]{3, 13, 6, 14}, new int[]{0, 11, 3, 14});
        checkCase("memory RR-5", memory, "RR-5",
                new String[]{"JobA", "JobC", "JobA", "JobD", "JobB"},
                new int[][]{
                        {0, 5, 6, 1}, {5, 10, 5, 0}, {10, 11, 1, 0}, {11, 13, 2, 0},
                        {13, 17, 4, 0}},
                new int[]{11, 17, 10, 13}, new int[]{5, 13, 5, 11}, new int[]{0, 13, 5, 11});

        checkCase("tie SJF", tie, "SJF",
                new String[]{"Job3", "Job1", "Job2"},
                new int[][]{{0, 3, 3, 0}, {3, 8, 5, 0}, {8, 13, 5, 0}},
                new int[]{8, 13, 3}, new int[]{3, 8, 0}, new int[]{3, 8, 0});

        checkCase("too big FCFS", tooBig, "FCFS",
                new String[]{"JobOk"},
                new int[][]{{0, 3, 3, 0}},
                new int[]{3}, new int[]{0}, new int[]{0});
        csc227.ai.ScheduleResult rejected = Algorithms.run("FCFS", csc227.ai.JobFile.parse(tooBig));
        if (rejected.rejected().size() != 1) {
            fail("too-big job was not rejected: " + rejected.rejected());
        }

        csc227.ai.ScheduleResult fcfs = Algorithms.run("FCFS", csc227.ai.JobFile.parse(sample));
        csc227.ai.ScheduleResult likeFcfs = Algorithms.runRoundRobin(csc227.ai.JobFile.parse(sample), 100);
        sameAi("RR quantum 100 matches FCFS", fcfs, likeFcfs);

        expectThrow("empty file", "");
        expectThrow("odd file", "Job1\n");
        expectThrow("bad name", "Hello\n1, 2, 100\n");
        expectThrow("bad burst", "Job1\n1, 0, 100\n");
        expectThrow("thirty one jobs", thirtyOne());

        if (failures > 0) {
            System.out.println(failures + " check(s) failed");
            System.exit(1);
        }
        System.out.println("All scheduler checks passed.");
    }

    private static void checkCase(String label, String text, String algorithm, String[] sliceNames,
                                  int[][] slices, int[] completion, int[] waiting, int[] response) {
        csc227.ai.ScheduleResult direct = Algorithms.run(algorithm, csc227.ai.JobFile.parse(text));
        csc227.ai.ScheduleResult threaded = Algorithms.runThreaded(algorithm, csc227.ai.JobFile.parse(text));
        csc227.team.task1.ScheduleResult team = Runner.run(algorithm, JobFile.parse(text));
        csc227.team.task1.ScheduleResult teamThreaded = Runner.runThreaded(algorithm, JobFile.parse(text));
        expectAi(label + " ai", direct, sliceNames, slices, completion, waiting, response);
        sameAi(label + " threaded", direct, threaded);
        expectTeam(label + " team", team, sliceNames, slices, completion, waiting, response);
        sameTeam(label + " team threaded", team, teamThreaded);
    }

    private static void expectAi(String label, csc227.ai.ScheduleResult result, String[] sliceNames,
                                 int[][] slices, int[] completion, int[] waiting, int[] response) {
        if (result.slices().size() != slices.length) {
            fail(label + " slice count " + result.slices().size() + " != " + slices.length);
            return;
        }
        for (int i = 0; i < slices.length; i++) {
            Slice slice = result.slices().get(i);
            if (!slice.jobName.equals(sliceNames[i])
                    || slice.start != slices[i][0]
                    || slice.end != slices[i][1]
                    || slice.burstStart != slices[i][2]
                    || slice.burstEnd != slices[i][3]) {
                fail(label + " slice " + i + " was " + slice.jobName + " "
                        + slice.start + "-" + slice.end + " burst " + slice.burstStart
                        + "->" + slice.burstEnd);
            }
        }
        if (result.finished().size() != completion.length) {
            fail(label + " finished " + result.finished().size());
            return;
        }
        double waitSum = 0;
        double completionSum = 0;
        double responseSum = 0;
        for (int i = 0; i < completion.length; i++) {
            csc227.ai.PCB pcb = result.finished().get(i);
            waitSum += waiting[i];
            completionSum += completion[i];
            responseSum += response[i];
            if (pcb.completion() != completion[i] || pcb.waiting() != waiting[i]
                    || pcb.response() != response[i] || pcb.turnaround() != completion[i]
                    || pcb.firstStart() != response[i]) {
                fail(label + " " + pcb.jobName() + " completion " + pcb.completion()
                        + " waiting " + pcb.waiting() + " response " + pcb.response()
                        + " start " + pcb.firstStart());
            }
        }
        int n = completion.length;
        near(label + " avg waiting", result.averageWaiting(), waitSum / n);
        near(label + " avg completion", result.averageCompletion(), completionSum / n);
        near(label + " avg turnaround", result.averageTurnaround(), completionSum / n);
        near(label + " avg response", result.averageResponse(), responseSum / n);
        if (result.makespan() > 0) {
            near(label + " util", result.cpuUtilization(), 100.0);
        }
    }

    private static void expectTeam(String label, csc227.team.task1.ScheduleResult result, String[] sliceNames,
                                   int[][] slices, int[] completion, int[] waiting, int[] response) {
        if (result.slices().size() != slices.length) {
            fail(label + " slice count " + result.slices().size() + " != " + slices.length);
            return;
        }
        for (int i = 0; i < slices.length; i++) {
            csc227.team.task1.Slice slice = result.slices().get(i);
            if (!slice.jobName.equals(sliceNames[i])
                    || slice.start != slices[i][0]
                    || slice.end != slices[i][1]
                    || slice.burstStart != slices[i][2]
                    || slice.burstEnd != slices[i][3]) {
                fail(label + " slice " + i + " was " + slice.jobName + " "
                        + slice.start + "-" + slice.end + " burst " + slice.burstStart
                        + "->" + slice.burstEnd);
            }
        }
        for (int i = 0; i < completion.length; i++) {
            csc227.team.task1.PCB pcb = result.finished().get(i);
            if (pcb.completion() != completion[i] || pcb.waiting() != waiting[i]
                    || pcb.response() != response[i]) {
                fail(label + " " + pcb.jobName() + " completion " + pcb.completion()
                        + " waiting " + pcb.waiting() + " response " + pcb.response());
            }
        }
    }

    private static void sameAi(String label, csc227.ai.ScheduleResult left, csc227.ai.ScheduleResult right) {
        if (left.slices().size() != right.slices().size()
                || left.finished().size() != right.finished().size()) {
            fail(label + " size mismatch");
            return;
        }
        for (int i = 0; i < left.slices().size(); i++) {
            Slice a = left.slices().get(i);
            Slice b = right.slices().get(i);
            if (!a.jobName.equals(b.jobName) || a.start != b.start || a.end != b.end
                    || a.burstStart != b.burstStart || a.burstEnd != b.burstEnd) {
                fail(label + " slice " + i);
            }
        }
        near(label + " waiting", left.averageWaiting(), right.averageWaiting());
    }

    private static void sameTeam(String label, csc227.team.task1.ScheduleResult left,
                                 csc227.team.task1.ScheduleResult right) {
        if (left.slices().size() != right.slices().size()) {
            fail(label + " size mismatch");
            return;
        }
        for (int i = 0; i < left.slices().size(); i++) {
            csc227.team.task1.Slice a = left.slices().get(i);
            csc227.team.task1.Slice b = right.slices().get(i);
            if (!a.jobName.equals(b.jobName) || a.start != b.start || a.end != b.end) {
                fail(label + " slice " + i);
            }
        }
    }

    private static void expectThrow(String label, String text) {
        boolean aiThrew = false;
        boolean teamThrew = false;
        try {
            csc227.ai.JobFile.parse(text);
        } catch (IllegalArgumentException ex) {
            aiThrew = true;
        }
        try {
            JobFile.parse(text);
        } catch (IllegalArgumentException ex) {
            teamThrew = true;
        }
        if (!aiThrew || !teamThrew) {
            fail(label + " should have been rejected");
        }
    }

    private static String thirtyOne() {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= 31; i++) {
            text.append("Job").append(i).append("\n1, 1, 10\n");
        }
        return text.toString();
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void near(String label, double actual, double expected) {
        if (Math.abs(actual - expected) > 1e-9) {
            fail(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void fail(String message) {
        failures++;
        System.out.println("FAIL " + message);
    }
}
