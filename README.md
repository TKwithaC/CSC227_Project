# CSC 227 — Multithreaded scheduling simulator

Java program for the CSC 227 project, first semester 1448. It simulates one CPU with a long-term scheduler, a short-term scheduler, and a medium-term scheduler. The reference solution is in `src/ai`. The team folders are empty on purpose: each member writes their own part.

```bash
bash scripts/build.sh
java -cp out/ai csc227.ai.task4.SchedulerApp samples/job.txt
```

`samples/job.txt` is the file from the project handout. With no flags, the program asks which algorithm to run and whether the medium-term scheduler starts enabled. The report is `docs/REPORT.pdf`.

## Task split

| Member | Folder | What to do |
| --- | --- | --- |
| 1 | `src/team/task1` | Processes and Thread 1. Build the PCB (process id, state, CPU burst, priority, memory, waiting time, turnaround time). Read `job.txt`, create the PCBs, put them in the job queue, then end the thread. Also print the Gantt chart, the process table, and the two averages. |
| 2 | `src/team/task2` | Thread 2, the long-term scheduler, and shortest job first. Load the next job into the ready queue only when it fits in 2048 MB. For SJF, equal CPU bursts are scheduled by arrival order. |
| 3 | `src/team/task3` | Thread 3, the medium-term scheduler, and Round Robin. Above 90% memory use, swap out the process with the largest memory. Below 70%, swap it back in. The mechanism can be enabled or disabled at startup. Print each intervention as specified. Round Robin gives each process 5 ms. |
| 4 | `src/team/task4` | The main thread and priority scheduling. Let the user choose the algorithm. Priority is non-preemptive, numbers 1–30, smallest number first, ties by arrival order. Mark a process starved if it stays in the ready queue longer than N × 5 ms without being dispatched, where N is fixed when it enters. Every 4 ms, decrease a waiting process's priority number by 1. Print which processes starved. |

Processes arrive at time 0, in the order they are read. Context-switch time is 0. The clock is 1 ms. The program must use threads.

The reference solution is split the same way, so each member can compare their folder with the matching reference files:

| Member | Your folder | Reference files |
| --- | --- | --- |
| 1 | `src/team/task1` | `src/ai/task1` |
| 2 | `src/team/task2` | `src/ai/task2` |
| 3 | `src/team/task3` | `src/ai/task3` |
| 4 | `src/team/task4` | `src/ai/task4` |
