package csc227.team.task4;

import csc227.team.task1.Machine;
import csc227.team.task1.PCB;
import csc227.team.task1.ScheduleResult;

/**
 * Task 4 — Round Robin.
 *
 * <p>The ready queue is the same FIFO used by FCFS, but a job may keep the
 * CPU for at most {@code quantum} milliseconds. If burst remains, the job
 * goes to the tail of the ready queue (preemption by the timer interrupt).
 * The project asks for quantum 3 and quantum 5. A quantum of 0 is rejected.
 * Dispatch latency is 0, so the quantum is pure CPU time.
 */
public final class RoundRobinScheduler {
    private static final int STEP_LIMIT = 100000;

    private final int quantum;
    private final String label;

    public RoundRobinScheduler(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be positive");
        }
        this.quantum = quantum;
        this.label = "RR-" + quantum;
    }

    public ScheduleResult run(Machine machine) {
        int steps = 0;
        while (true) {
            if (++steps > STEP_LIMIT) {
                throw new IllegalStateException(label + " did not finish");
            }
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
}
