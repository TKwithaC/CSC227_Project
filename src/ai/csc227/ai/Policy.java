package csc227.ai;

import java.util.List;

/**
 * What the short-term scheduler is allowed to decide. The clock, the memory
 * check, and the threads live in {@link Machine}. A quantum of 0 means the
 * running process is not preempted. Aging is used only by priority scheduling.
 */
public interface Policy {
    String name();

    PCB select(List<PCB> ready);

    int quantum();

    boolean aging();
}
