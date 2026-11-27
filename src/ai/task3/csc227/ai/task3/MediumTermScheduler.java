package csc227.ai.task3;

import csc227.ai.task1.PCB;
import csc227.ai.task4.Machine;

/** Thread 3. Swaps out above 90% and swaps back in below 70%. */
public final class MediumTermScheduler {
    private static final double SWAP_OUT_ABOVE = 90.0;
    private static final double SWAP_IN_BELOW = 70.0;

    private MediumTermScheduler() {
    }

    public static boolean step(Machine machine) {
        if (!machine.mediumEnabled()) {
            return false;
        }
        double before = (100.0 * machine.usedMemory()) / Machine.MEMORY_MB;
        if (before > SWAP_OUT_ABOVE) {
            PCB victim = largestReady(machine);
            if (victim == null) {
                return false;
            }
            machine.readyQueue().remove(victim);
            machine.setUsedMemory(machine.usedMemory() - victim.memory());
            String stateBefore = victim.state().label();
            victim.suspend();
            machine.suspendQueue().addLast(victim);
            machine.record(new Intervention(machine.time(), before, utilization(machine), "Swap out",
                    victim.pid(), stateBefore, victim.state().label()));
            return true;
        }
        if (before < SWAP_IN_BELOW) {
            PCB returning = firstThatFits(machine);
            if (returning == null) {
                return false;
            }
            machine.suspendQueue().remove(returning);
            machine.setUsedMemory(machine.usedMemory() + returning.memory());
            String stateBefore = returning.state().label();
            machine.readyQueue().addLast(returning);
            returning.enterReady(machine.time(), machine.readyQueue().size());
            machine.record(new Intervention(machine.time(), before, utilization(machine), "Swap in",
                    returning.pid(), stateBefore, returning.state().label()));
            return true;
        }
        return false;
    }

    private static double utilization(Machine machine) {
        return (100.0 * machine.usedMemory()) / Machine.MEMORY_MB;
    }

    private static PCB largestReady(Machine machine) {
        PCB best = null;
        for (PCB pcb : machine.readyQueue()) {
            if (best == null || pcb.memory() > best.memory()
                    || (pcb.memory() == best.memory() && pcb.fileOrder() > best.fileOrder())) {
                best = pcb;
            }
        }
        return best;
    }

    private static PCB firstThatFits(Machine machine) {
        for (PCB pcb : machine.suspendQueue()) {
            if (machine.usedMemory() + pcb.memory() <= Machine.MEMORY_MB) {
                return pcb;
            }
        }
        return null;
    }
}
