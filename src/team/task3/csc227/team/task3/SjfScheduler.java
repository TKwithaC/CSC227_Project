package csc227.team.task3;

import csc227.team.task1.Machine;
import csc227.team.task1.PCB;
import csc227.team.task1.ScheduleResult;

/**
 * Task 3 — Shortest-Job-First, non-preemptive.
 *
 * <p>Every job arrives at time 0. When the CPU is free, the ready job with
 * the smallest remaining burst is dispatched. Ties keep the earlier job in
 * the file. SJF does not preempt: once a job starts, it runs to completion.
 * The preemptive form (shortest remaining time first) is a different
 * algorithm and is not part of this project.
 */
public final class SjfScheduler {
    private static final int STEP_LIMIT = 100000;

    public ScheduleResult run(Machine machine) {
        int steps = 0;
        while (true) {
            if (++steps > STEP_LIMIT) {
                throw new IllegalStateException("SJF did not finish");
            }
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
}
