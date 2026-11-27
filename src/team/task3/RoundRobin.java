
public final class RoundRobinPolicy implements Policy {
    public static final int QUANTUM_MS = 5;

    public String name() {
        return "Round Robin (q = 5 ms)";
    }

    public PCB select(List<PCB> ready) {
        if (ready.isEmpty()) {
            return null;
        }
        return ready.get(0);
    }

    public int quantum() {
        return QUANTUM_MS;
    }

    public boolean aging() {
        return false;
    }
}
