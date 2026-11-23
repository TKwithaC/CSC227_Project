package csc227.ai;

import java.util.List;
import java.util.Locale;

/**
 * The four short-term schedulers from the project: FCFS, non-preemptive SJF,
 * Round Robin with quantum 3, and Round Robin with quantum 5.
 *
 * <p>Every job is given arrival time 0. FCFS and Round Robin keep the order
 * of the file (the order the jobs were read). SJF reorders by burst length.
 * That is the reading of the project notes "arrive in the order they are
 * read" and "all jobs arrive at time 0 for SJF", and it makes the average
 * waiting times comparable.
 */
public final class Algorithms {
    private static final int STEP_LIMIT = 100000;

    private Algorithms() {
    }

    public static String canonical(String raw) {
        String name = raw.trim().toUpperCase(Locale.US).replace("_", "-").replace(" ", "");
        if ("FCFS".equals(name) || "1".equals(name)) {
            return "FCFS";
        }
        if ("SJF".equals(name) || "2".equals(name)) {
            return "SJF";
        }
        if ("RR-3".equals(name) || "RR3".equals(name) || "3".equals(name)) {
            return "RR-3";
        }
        if ("RR-5".equals(name) || "RR5".equals(name) || "4".equals(name)) {
            return "RR-5";
        }
        throw new IllegalArgumentException("Choose FCFS, SJF, RR-3, or RR-5. Found: " + raw);
    }

    public static ScheduleResult run(String name, List<PCB> jobs) {
        Machine machine = new Machine();
        machine.loadJobs(jobs);
        return finish(name, machine);
    }

    /** Used by the tests to show that a large quantum collapses Round Robin to FCFS. */
    public static ScheduleResult runRoundRobin(List<PCB> jobs, int quantum) {
        Machine machine = new Machine();
        machine.loadJobs(jobs);
        try {
            return roundRobin(machine, quantum, "RR-" + quantum);
        } finally {
            machine.stopLoader();
        }
    }

    public static ScheduleResult runThreaded(String name, List<PCB> jobs) {
        final Machine machine = new Machine();
        final RuntimeException[] failure = new RuntimeException[1];
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    machine.loadJobs(jobs);
                } catch (RuntimeException ex) {
                    failure[0] = ex;
                }
            }
        }, "job-reader");
        reader.start();
        join(reader);
        if (failure[0] != null) {
            throw failure[0];
        }
        machine.startLoader();
        return finish(name, machine);
    }

    public static ScheduleResult finish(String name, Machine machine) {
        try {
            String algorithm = canonical(name);
            if ("FCFS".equals(algorithm)) {
                return fcfs(machine);
            }
            if ("SJF".equals(algorithm)) {
                return sjf(machine);
            }
            if ("RR-3".equals(algorithm)) {
                return roundRobin(machine, 3, "RR-3");
            }
            return roundRobin(machine, 5, "RR-5");
        } finally {
            machine.stopLoader();
        }
    }

    static ScheduleResult fcfs(Machine machine) {
        int steps = 0;
        while (true) {
            guard(steps++);
            machine.requestAdmission();
            if (!machine.hasReady()) {
                break;
            }
            PCB pcb = machine.pollReady();
            machine.runFor(pcb, pcb.remaining());
            machine.release(pcb);
        }
        return machine.result("FCFS");
    }

    static ScheduleResult sjf(Machine machine) {
        int steps = 0;
        while (true) {
            guard(steps++);
            machine.requestAdmission();
            PCB pcb = machine.takeShortest();
            if (pcb == null) {
                break;
            }
            machine.runFor(pcb, pcb.remaining());
            machine.release(pcb);
        }
        return machine.result("SJF");
    }

    static ScheduleResult roundRobin(Machine machine, int quantum, String label) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be positive");
        }
        int steps = 0;
        while (true) {
            guard(steps++);
            machine.requestAdmission();
            if (!machine.hasReady()) {
                break;
            }
            PCB pcb = machine.pollReady();
            int step = Math.min(quantum, pcb.remaining());
            machine.runFor(pcb, step);
            if (pcb.remaining() == 0) {
                machine.release(pcb);
            } else {
                machine.requeue(pcb);
            }
        }
        return machine.result(label);
    }

    private static void guard(int steps) {
        if (steps > STEP_LIMIT) {
            throw new IllegalStateException("The scheduler did not finish");
        }
    }

    private static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("job-reader was interrupted", ex);
        }
    }
}
