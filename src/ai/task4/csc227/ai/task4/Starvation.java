package csc227.ai.task4;

import csc227.ai.task1.PCB;

import java.util.Collection;

/**
 * A process is starved when this ready stay lasts more than N × 5 ms.
 * N was fixed when the process entered the ready queue.
 */
public final class Starvation {
    private Starvation() {
    }

    public static void mark(Collection<PCB> ready, int time) {
        for (PCB pcb : ready) {
            int waited = time - pcb.readyEnteredAt();
            if (waited > pcb.readyCohort() * 5) {
                pcb.noteStarved();
            }
        }
    }

    public static String line(Iterable<PCB> finished) {
        StringBuilder out = new StringBuilder();
        out.append("Starved processes: ");
        boolean any = false;
        for (PCB pcb : finished) {
            if (!pcb.starved()) {
                continue;
            }
            if (any) {
                out.append(", ");
            }
            out.append("Process ").append(pcb.pid());
            any = true;
        }
        if (!any) {
            out.append("none");
        }
        out.append('\n');
        return out.toString();
    }
}
