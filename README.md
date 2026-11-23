# CSC 227 — CPU Scheduler

Operating Systems programming project for semester 1448-1. The program simulates the short-term scheduler. It reads `job.txt`, builds a process control block for every job, admits jobs into 8192 MB of main memory, and runs four algorithms:

1. First-Come-First-Serve (FCFS)
2. Shortest-Job-First (SJF, non-preemptive)
3. Round Robin with time quantum 3 (RR-3)
4. Round Robin with time quantum 5 (RR-5)

The repository has two source trees.

| Tree | What it is |
| --- | --- |
| `src/ai` | Reference solution. Use this to check numbers. |
| `src/team` | The same program split into four tasks, one folder per member. |

The split, the class each person owns, and the rules that keep the parts compatible are in [docs/TEAM_TASKS.md](docs/TEAM_TASKS.md). The written report is [docs/REPORT.md](docs/REPORT.md).

## Run it

Java 11 or newer is enough. No extra libraries.

```bash
bash scripts/build.sh
java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --all
java -cp out/team csc227.team.task4.SchedulerApp samples/job.txt --all
```

`scripts/build.sh` compiles both trees, runs the checks, and writes the sample output under `docs/test-runs/`.

Other ways to start the reference program:

```bash
java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --algorithm=sjf
java -cp out/ai csc227.ai.SchedulerApp samples/job.txt
```

The last form opens a menu. With no file argument the program asks for the path to `job.txt`.

## Job file

Each job is two lines. The numbers are the process id, the CPU burst in milliseconds, and the memory requirement in MB.

```text
Job1
5, 8, 800
Job2
3, 10, 2000
```

Blank lines and lines that start with `#` are ignored. A file may contain at most 30 jobs. A job that asks for more than 8192 MB is reported and is not scheduled.

`samples/job.txt` is the four-job example from the handout. `samples/job-memory.txt` is a second file in which the jobs do not all fit in memory at once.

## What the output contains

For the chosen algorithm the program prints:

- a Gantt chart
- every CPU slice, with the burst at the start of the slice and the burst at the end
- waiting time, completion time, turnaround time, and response time
- the averages the project asks for
- the state trace (NEW, WAITING, READY, RUNNING, TERMINATED)

`--all` runs FCFS, SJF, RR-3, and RR-5 and then prints one comparison table.

## Check a member's work

Keep the public method names in [docs/TEAM_TASKS.md](docs/TEAM_TASKS.md). Replace only that member's folder, then run:

```bash
bash scripts/build.sh
```

The checks compare `src/team` with `src/ai` on the sample file, the memory-pressure file, an SJF tie, and a job that does not fit in memory. Both the plain call and the reader/loader threads have to produce the same slices.

## Course mapping

The design follows the first part of the course, through CPU scheduling.

| Topics | Where they show up |
| --- | --- |
| The OS as a resource manager | The simulator allocates the CPU and 8192 MB of memory. |
| Process and PCB | `PCB` stores the id, state, burst, and memory, plus the scheduling measurements. |
| Five-state model and queues | NEW, then WAITING in the job queue, READY, RUNNING, TERMINATED. |
| Short-term scheduler and dispatcher | The algorithm picks the next ready job. Dispatch latency is 0. |
| Threads | `job-reader` parses the file, `job-loader` admits jobs, and the main thread schedules. One lock protects the queues. |
| Scheduling criteria | Waiting, turnaround, completion, response, CPU utilization, throughput. |
| FCFS, SJF, Round Robin | The four required algorithms. SJF is non-preemptive. RR uses quanta 3 and 5. |
