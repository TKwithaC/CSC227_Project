package csc227.ai;

import java.util.List;

/**
 * Non-preemptive priority. A smaller priority number is a higher priority.
 * Equal priorities keep arrival order. Aging runs on the clock: every 4 ms a
 * process spends in the ready queue, its priority number decreases by 1, and
 * it never goes below 1. A process is starved when it stays in the ready queue
 * longer than N × 5 ms without being dispatched. N is the ready-queue size at
 * the moment that process entered, and it is not recomputed later.
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
