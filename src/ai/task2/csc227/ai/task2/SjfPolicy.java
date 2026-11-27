package csc227.ai.task2;

import csc227.ai.task1.PCB;
import csc227.ai.task4.Policy;

import java.util.List;

/** Shortest job first. Equal CPU bursts keep the order the processes were read. */
public final class SjfPolicy implements Policy {
    @Override
    public String name() {
        return "Shortest Job First";
    }

    @Override
    public PCB select(List<PCB> ready) {
        PCB best = null;
        for (int i = 0; i < ready.size(); i++) {
            PCB pcb = ready.get(i);
            if (best == null || pcb.burst() < best.burst()
                    || (pcb.burst() == best.burst() && pcb.fileOrder() < best.fileOrder())) {
                best = pcb;
            }
        }
        return best;
    }

    @Override
    public int quantum() {
        return 0;
    }

    @Override
    public boolean aging() {
        return false;
    }
}
