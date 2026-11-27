package csc227.ai.task2;

import csc227.ai.task1.PCB;
import csc227.ai.task4.Machine;

import java.util.List;

/** Thread 2. Loads the next job into the ready queue only when it fits in 2048 MB. */
public final class LongTermScheduler {
    private LongTermScheduler() {
    }

    public static boolean loadNext(Machine machine) {
        List<PCB> jobQueue = machine.jobQueue();
        if (jobQueue.isEmpty()) {
            return false;
        }
        PCB next = jobQueue.get(0);
        if (next.memory() > Machine.MEMORY_MB) {
            throw new IllegalArgumentException(
                    "Process " + next.pid() + " needs " + next.memory()
                            + " MB, which is more than main memory (" + Machine.MEMORY_MB + " MB)");
        }
        if (machine.usedMemory() + next.memory() > Machine.MEMORY_MB) {
            return false;
        }
        jobQueue.remove(0);
        machine.setUsedMemory(machine.usedMemory() + next.memory());
        machine.readyQueue().addLast(next);
        next.enterReady(machine.time(), machine.readyQueue().size());
        return true;
    }
}
