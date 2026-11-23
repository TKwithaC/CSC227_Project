# CSC 227 Operating Systems

## Programming Project — CPU Scheduler Simulation

Semester 1448, term 1

| | |
| --- | --- |
| Course | CSC 227 — Operating Systems |
| Project | Short-term CPU scheduler |
| Team size | 4 |

| Member | University ID | Task |
| --- | --- | --- |
| Member 1 | | Process model, PCB, job file, memory admission |
| Member 2 | | First-Come-First-Serve |
| Member 3 | | Shortest-Job-First |
| Member 4 | | Round Robin, driver, and output |

Fill in the names and ids before submission. The folder each person owns is listed in `docs/TEAM_TASKS.md`.

## 1. Purpose

The project is a user-level simulation of the short-term scheduler, the part of the operating system that chooses which ready process runs on the CPU next. The program reads a job file, builds a process control block (PCB) for each job, admits a job into main memory only when there is room, and then runs one of four algorithms:

- First-Come-First-Serve (FCFS)
- Shortest-Job-First (SJF), non-preemptive
- Round Robin with time quantum 3 ms (RR-3)
- Round Robin with time quantum 5 ms (RR-5)

For every algorithm it prints a Gantt chart, the burst at the start and at the end of each slice, and the average waiting time and average completion time. The same run also reports turnaround time, response time, CPU utilization, and throughput, which are the criteria used to compare schedulers in the CPU-scheduling chapter.

The simulator is not a kernel. It is a Java program that follows the same queueing rules the course uses, so the numbers can be checked by hand.

## 2. What we took from the course

**The operating system manages resources.** The two resources in this project are the CPU and main memory. Main memory is fixed at 8192 MB. The CPU runs one process at a time.

**A process is represented by a PCB.** Each PCB stores the process id, the state, the CPU burst, and the memory requirement, which is the set of fields the handout asks for. We also store the remaining burst and the measurements needed later: first dispatch time, completion time, waiting time, turnaround time, and response time.

**The five states are visible in the trace.**

```text
NEW → WAITING → READY → RUNNING → TERMINATED
                  ↑         |
                  └─────────┘
                  quantum preemption (Round Robin only)
```

NEW is the moment the PCB is created. The process then sits in the job queue in the WAITING state until the loader can give it memory. READY means it is in memory and waiting for the CPU. RUNNING means it holds the CPU. TERMINATED means the burst is finished and the memory has been released. This project has no I/O burst, so WAITING is the wait for memory rather than a wait for a disk or a keyboard. The state is still required, and the trace shows every transition.

**Two schedulers, not one.** The loader is the admission step: it decides which jobs from the job queue may enter the ready queue. The algorithm (FCFS, SJF, or Round Robin) is the short-term scheduler: it decides which ready job the dispatcher places on the CPU. Dispatch latency is 0 ms. We state that explicitly because the scheduling chapter treats dispatch latency as a real cost, and our comparison does not include it.

**Threads.** Three threads cooperate, which is the concurrency material that sits between processes and scheduling:

- `job-reader` reads `job.txt` and creates the PCBs
- `job-loader` admits a job when `used memory + request ≤ 8192`
- the main thread runs the selected algorithm

The job queue, the ready queue, and the memory counter are shared. One lock surrounds every change. The scheduler does not spin. It sets a flag, wakes the loader, and waits until that admission pass finishes. The checks run the threaded path and the direct path and require the same slices, so the lock is not only decorative.

**Scheduling criteria.** With arrival time 0 for every admitted job:

- turnaround time = completion time − arrival time
- waiting time = turnaround time − burst
- response time = time of first dispatch − arrival time

Because arrival is 0, turnaround time and completion time are equal. We still print both labels, because the project asks for completion time and the chapter defines turnaround time.
