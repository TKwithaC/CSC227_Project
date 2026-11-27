package csc227.ai.task4;

import csc227.ai.task1.PCB;

import java.util.List;

/**
 * Non-preemptive priority. A smaller number is a higher priority.
 * Equal priorities keep arrival order.
 */
public final class PriorityPolicy implements Policy {
    @Override
    public String name() {
        return "Priority (non-preemptive, with aging)";
    }

    @Override
    public PCB select(List<PCB> ready) {
        PCB best = null;
        for (int i = 0; i < ready.size(); i++) {
            PCB pcb = ready.get(i);
            if (best == null || pcb.priority() < best.priority()
                    || (pcb.priority() == best.priority() && pcb.fileOrder() < best.fileOrder())) {
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
        return true;
    }
}
