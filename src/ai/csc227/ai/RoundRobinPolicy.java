package csc227.ai;

import java.util.List;

/**
 * Round Robin with the handout quantum, q = 5 ms. The ready queue is a FIFO.
 * A process that still has burst left returns to the tail. Context-switch time
 * is 0, so the quantum is five milliseconds of CPU time.
 */
public final class RoundRobinPolicy implements Policy {
    public static final int QUANTUM_MS = 5;

    @Override
    public String name() {
        return "Round Robin (q = 5 ms)";
    }

    @Override
    public PCB select(List<PCB> ready) {
        if (ready.isEmpty()) {
            return null;
        }
        return ready.get(0);
    }

    @Override
    public int quantum() {
        return QUANTUM_MS;
    }

    @Override
    public boolean aging() {
        return false;
    }
}
