package csc227.ai.task4;

import csc227.ai.task2.SjfPolicy;
import csc227.ai.task3.RoundRobinPolicy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

/**
 * Asks for the job file, whether the medium-term scheduler starts enabled,
 * and which of the three algorithms to run.
 */
public final class SchedulerApp {
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
        String fileName = null;
        String algorithm = null;
        String medium = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--algorithm".equals(arg) && i + 1 < args.length) {
                algorithm = args[++i];
            } else if (arg.startsWith("--algorithm=")) {
                algorithm = arg.substring("--algorithm=".length());
            } else if ("--medium".equals(arg) && i + 1 < args.length) {
                medium = args[++i];
            } else if (arg.startsWith("--medium=")) {
                medium = arg.substring("--medium=".length());
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printUsage();
                return;
            } else if (arg.startsWith("--")) {
                throw new IllegalArgumentException("Unknown option: " + arg);
            } else if (fileName == null) {
                fileName = arg;
            } else {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
        }

        Scanner scanner = new Scanner(System.in);
        if (fileName == null) {
            System.out.print("Enter the file path (job.txt): ");
            if (!scanner.hasNextLine()) {
                printUsage();
                return;
            }
            fileName = scanner.nextLine().trim();
        }
        if (medium == null) {
            medium = askMedium(scanner);
        }
        if (algorithm == null) {
            algorithm = askAlgorithm(scanner);
            if (algorithm == null) {
                return;
            }
        }
        boolean enabled = parseMedium(medium);
        Policy policy = policy(algorithm);
        Path path = Paths.get(fileName);
        System.out.println("Job file: " + path);
        System.out.println();
        System.out.println(TextReport.format(Machine.run(path, policy, enabled)));
    }

    private static String askMedium(Scanner scanner) {
        System.out.println("Medium-term scheduler at startup:");
        System.out.println("  1  Enabled");
        System.out.println("  2  Disabled");
        System.out.print("> ");
        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Choose whether the medium-term scheduler is enabled");
        }
        return scanner.nextLine().trim();
    }

    private static String askAlgorithm(Scanner scanner) {
        System.out.println("Select a scheduling algorithm:");
        System.out.println("  1  Shortest Job First");
        System.out.println("  2  Round Robin (q = 5 ms)");
        System.out.println("  3  Priority (non-preemptive, with aging)");
        System.out.println("  0  Quit");
        System.out.print("> ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String choice = scanner.nextLine().trim();
        if ("0".equals(choice)) {
            return null;
        }
        return choice;
    }

    static boolean parseMedium(String raw) {
        String value = raw.trim().toLowerCase(Locale.US);
        if ("1".equals(value) || "on".equals(value) || "enabled".equals(value) || "true".equals(value)) {
            return true;
        }
        if ("2".equals(value) || "off".equals(value) || "disabled".equals(value) || "false".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException("Medium-term scheduler must be enabled or disabled. Found: " + raw);
    }

    static Policy policy(String raw) {
        String name = raw.trim().toLowerCase(Locale.US).replace("_", "").replace("-", "").replace(" ", "");
        if ("sjf".equals(name) || "1".equals(name) || "shortestjobfirst".equals(name)) {
            return new SjfPolicy();
        }
        if ("rr".equals(name) || "rr5".equals(name) || "2".equals(name) || "roundrobin".equals(name)) {
            return new RoundRobinPolicy();
        }
        if ("priority".equals(name) || "3".equals(name) || "pri".equals(name)) {
            return new PriorityPolicy();
        }
        throw new IllegalArgumentException("Choose SJF, Round Robin, or Priority. Found: " + raw);
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp out/ai csc227.ai.task4.SchedulerApp <job.txt> --algorithm=sjf|rr|priority --medium=on|off");
    }
}
