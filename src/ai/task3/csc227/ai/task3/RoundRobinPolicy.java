package csc227.ai.task3;

import csc227.ai.task1.PCB;
import csc227.ai.task4.Policy;

import java.util.List;

/** Round Robin. Each process gets 5 ms. */
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
