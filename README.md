# CSC 227 — Multithreaded scheduling simulator

A Java simulation of the long-term, short-term, and medium-term schedulers for one CPU, as specified in the CSC 227 project for semester 1448.

The short-term scheduler runs one of three algorithms:

- Shortest job first (non-preemptive; equal bursts keep arrival order)
- Round Robin with time quantum 5 ms
- Non-preemptive priority, with aging and starvation

Main memory is 2048 MB. Every process arrives at time 0. Context-switch time is 0. The clock steps by 1 ms.

## Job file

One process per line: `Process ID: burst in ms: priority; memory in MB`.

```text
1:25:4;500
2:13:3;700
3:20:3;100
```

Priority is an integer from 1 to 30. A smaller number is a higher priority. The sample above is `samples/job.txt`.

## Run

Java 11 or newer. No libraries.

```bash
bash scripts/build.sh
java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --algorithm=sjf --medium=off
java -cp out/team csc227.team.task4.SchedulerApp samples/job.txt --algorithm=priority --medium=on
```

With no flags, the program asks for the file, whether the medium-term scheduler starts enabled, and which algorithm to run.

`src/ai` is the reference copy. `src/team` is the same program split into four folders, one member each. The split is in `docs/TEAM_TASKS.md`. The report is `docs/REPORT.pdf`.

## Output

For the selected algorithm the program prints a Gantt chart, a table (process id, burst, start, termination, waiting, turnaround), and the average waiting time and average turnaround time. Priority also names the starved processes. When the medium-term scheduler swaps, it prints the time, the process, the utilization before and after, the action, and the state change.
