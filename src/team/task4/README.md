# Task 4 — Round Robin, the menu, and the tables

Member 4. Four classes.

| File | Role |
| --- | --- |
| `RoundRobinScheduler.java` | Preemptive RR. Constructor takes the quantum. |
| `Runner.java` | Starts the reader and the loader, then calls task 2, 3, or 4 |
| `SchedulerApp.java` | The menu |
| `TextReport.java` | Gantt chart, per-job table, averages |

Quantum 3 and quantum 5 are the two required runs. A job that still has burst left goes to the tail of the ready queue. A job that finishes releases its memory.

On `samples/job.txt`:

- RR-3 average waiting 14.75 ms, average completion 21.75 ms, average response 4.50 ms
- RR-5 average waiting 15.00 ms, average completion 22.00 ms, average response 7.00 ms

Keep `public ScheduleResult run(Machine machine)` and `public RoundRobinScheduler(int quantum)`.
