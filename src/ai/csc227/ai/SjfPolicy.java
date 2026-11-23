package csc227.ai;

import java.util.List;

/**
 * Non-preemptive shortest job first. The CPU burst is known from the file.
 * Equal bursts keep the order the processes were read, which is their arrival
 * order because every process arrives at time 0.
 */
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
