package csc227.team.task4;

import csc227.team.task1.Machine;
import csc227.team.task1.PCB;
import csc227.team.task1.ScheduleResult;
import csc227.team.task2.FcfsScheduler;
import csc227.team.task3.SjfScheduler;

import java.util.List;
import java.util.Locale;

/**
 * Connects the four tasks. Task 1 owns the PCB, the file, and memory
 * admission. Task 2 owns FCFS. Task 3 owns SJF. Task 4 owns Round Robin and
 * this driver. The public method names are the contract the tests compile
 * against, so a member can replace only the body of their scheduler.
 */
public final class Runner {
    private Runner() {
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
            return dispatch(canonical(name), machine);
        } finally {
            machine.stopLoader();
        }
    }

    private static ScheduleResult dispatch(String name, Machine machine) {
        if ("FCFS".equals(name)) {
            return new FcfsScheduler().run(machine);
        }
        if ("SJF".equals(name)) {
            return new SjfScheduler().run(machine);
        }
        if ("RR-3".equals(name)) {
            return new RoundRobinScheduler(3).run(machine);
        }
        return new RoundRobinScheduler(5).run(machine);
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
