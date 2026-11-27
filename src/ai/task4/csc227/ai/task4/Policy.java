package csc227.ai.task4;

import csc227.ai.task1.PCB;

import java.util.List;

/** The choice the main thread makes among the three algorithms. */
public interface Policy {
    String name();

    PCB select(List<PCB> ready);

    int quantum();

    boolean aging();
}
