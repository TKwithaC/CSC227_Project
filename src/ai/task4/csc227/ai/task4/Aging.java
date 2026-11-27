package csc227.ai.task4;

import csc227.ai.task1.PCB;

import java.util.Collection;

/** Every 4 ms in the ready queue, decrease the priority number by 1, down to 1. */
public final class Aging {
    private Aging() {
    }

    public static void apply(Collection<PCB> ready, int time) {
        for (PCB pcb : ready) {
            int due = (time - pcb.readyEnteredAt()) / 4;
            while (pcb.agedSteps() < due) {
                pcb.ageOneStep();
            }
        }
    }
}
