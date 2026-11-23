package csc227.ai;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Command line for the reference solution.
 *
 * <pre>
 * java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --all
 * java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --algorithm=sjf
 * java -cp out/ai csc227.ai.SchedulerApp samples/job.txt
 * </pre>
 *
 * A file read with no flag opens the menu. The reader thread parses the file
 * and the loader thread admits jobs while the main thread schedules.
 */
public final class SchedulerApp {
    private static final String[] ORDER = {"FCFS", "SJF", "RR-3", "RR-5"};

    private SchedulerApp() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    static void run(String[] args) {
        boolean compareAll = false;
        String selected = null;
        List<String> positional = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--all".equals(arg)) {
                compareAll = true;
            } else if ("--algorithm".equals(arg) && i + 1 < args.length) {
                selected = args[++i];
            } else if (arg.startsWith("--algorithm=")) {
                selected = arg.substring("--algorithm=".length());
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printUsage();
                return;
            } else if (arg.startsWith("--")) {
                throw new IllegalArgumentException("Unknown option: " + arg);
            } else {
                positional.add(arg);
            }
        }

        Scanner scanner = new Scanner(System.in);
        String fileName;
        if (positional.isEmpty()) {
            System.out.print("Enter the file path (job.txt): ");
            if (!scanner.hasNextLine()) {
                printUsage();
                return;
            }
            fileName = scanner.nextLine().trim();
        } else {
            fileName = positional.get(0);
        }
        Path path = Paths.get(fileName);
        List<PCB> preview = JobFile.parse(path);
        printJobs(path, preview);

        if (compareAll) {
            printAll(path);
            return;
        }
        if (selected != null) {
            String name = Algorithms.canonical(selected);
            System.out.println(TextReport.format(execute(path, name)));
            return;
        }
        menu(scanner, path);
    }

    private static void menu(Scanner scanner, Path path) {
        while (true) {
            System.out.println("Select a scheduling algorithm:");
            System.out.println("  1  First-Come-First-Serve (FCFS)");
            System.out.println("  2  Shortest-Job-First (SJF, non-preemptive)");
            System.out.println("  3  Round-Robin, time quantum 3 (RR-3)");
            System.out.println("  4  Round-Robin, time quantum 5 (RR-5)");
            System.out.println("  5  Compare all four");
            System.out.println("  0  Quit");
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String choice = scanner.nextLine().trim().toUpperCase(Locale.US);
            if ("0".equals(choice) || "QUIT".equals(choice)) {
                return;
            }
            if ("5".equals(choice) || "ALL".equals(choice)) {
                printAll(path);
                continue;
            }
            try {
                String name = Algorithms.canonical(choice);
                System.out.println(TextReport.format(execute(path, name)));
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void printAll(Path path) {
        List<ScheduleResult> results = new ArrayList<ScheduleResult>();
        for (int i = 0; i < ORDER.length; i++) {
            ScheduleResult result = execute(path, ORDER[i]);
            results.add(result);
            System.out.println(TextReport.format(result));
            System.out.println();
        }
        System.out.println(TextReport.comparison(results));
    }

    static ScheduleResult execute(Path path, String name) {
        final Machine machine = new Machine();
        final RuntimeException[] failure = new RuntimeException[1];
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    machine.loadJobs(JobFile.parse(path));
                } catch (RuntimeException ex) {
                    failure[0] = ex;
                }
            }
        }, "job-reader");
        reader.start();
        try {
            reader.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("job-reader was interrupted", ex);
        }
        if (failure[0] != null) {
            throw failure[0];
        }
        machine.startLoader();
        return Algorithms.finish(name, machine);
    }

    private static void printJobs(Path path, List<PCB> jobs) {
        System.out.println("CSC 227 Operating Systems");
        System.out.println("CPU scheduler simulation (short-term scheduler)");
        System.out.println();
        System.out.println("Job file: " + path);
        System.out.println("Main memory: " + Machine.MEMORY_CAPACITY_MB + " MB");
        System.out.println("Jobs read: " + jobs.size());
        for (PCB pcb : jobs) {
            System.out.println("  " + pcb.jobName()
                    + "  pid " + pcb.pid()
                    + "  burst " + pcb.burst() + " ms"
                    + "  memory " + pcb.memory() + " MB");
        }
        System.out.println();
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp out/ai csc227.ai.SchedulerApp <job.txt> [--all]");
        System.out.println("       java -cp out/ai csc227.ai.SchedulerApp <job.txt> --algorithm=fcfs|sjf|rr3|rr5");
    }
}
