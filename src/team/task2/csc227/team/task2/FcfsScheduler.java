package csc227.team.task2;

import csc227.team.task1.Machine;
import csc227.team.task1.PCB;
import csc227.team.task1.ScheduleResult;

/**
 * Task 2 — First-Come-First-Serve.
 *
 * <p>FCFS is non-preemptive. The ready queue is a FIFO, and the loader
 * enqueues jobs in the order they were read, skipping a job only when it
 * does not fit in the memory that is still free. A running job keeps the
 * CPU until its burst reaches zero. A long job at the front delays every
 * shorter job behind it (the convoy effect).
 */
public final class FcfsScheduler {
    private static final int STEP_LIMIT = 100000;

    public ScheduleResult run(Machine machine) {
        int steps = 0;
        while (true) {
            if (++steps > STEP_LIMIT) {
                throw new IllegalStateException("FCFS did not finish");
            }
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
}
