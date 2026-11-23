# Four tasks for a four-person team

The project is one program. The work is split so each person owns a folder, the four folders compile together, and `bash scripts/build.sh` tells you whether the result still matches the reference in `src/ai`.

Put your name and university id in the table before you submit. Do not rename the public methods. The tests call those methods.

| Member | Folder | Owns |
| --- | --- | --- |
| Member 1 | `src/team/task1` | Process model, PCB, `job.txt`, memory admission, both threads' shared data |
| Member 2 | `src/team/task2` | FCFS only |
| Member 3 | `src/team/task3` | SJF only |
| Member 4 | `src/team/task4` | Round Robin (RR-3 and RR-5), the menu, and the printed report |

`src/ai` is not a fifth task. It is the finished reference. After you replace your folder, the checks must still pass.

## Rules every task follows

These are the decisions that keep the four parts comparable. They come from the project statement and from the scheduling definitions used in the course.

1. A job line is `Process ID, burst time in ms, memory in MB`. The job name is the line above it and starts with `Job`.
2. At most 30 jobs.
3. Main memory is 8192 MB.
4. Every admitted job has arrival time 0. FCFS and Round Robin keep file order. SJF sorts by burst. That is how the project can compare average waiting time.
5. The loader admits every waiting job that fits, scanning in file order. It does not leave a small job stuck behind a large job that does not fit right now.
6. A job larger than 8192 MB is rejected and is left out of the averages.
7. Memory is released only when the process terminates.
8. SJF is non-preemptive. If two bursts are equal, the earlier job in the file runs first.
9. Round Robin puts a preempted job at the tail of the ready queue. A job admitted at the same decision point also joins the tail, so it does not jump ahead of work that was already ready.
10. Dispatch latency is 0. There is no I/O burst.
11. State order: NEW when the PCB is created, WAITING in the job queue (waiting for memory), READY, RUNNING, then READY again if the quantum expires, or TERMINATED when the burst hits 0.

Waiting time = completion − arrival − burst. Turnaround time = completion − arrival. Response time = first dispatch − arrival. With arrival 0, turnaround equals completion.

## Task 1 — processes, the file, and memory

**Course material:** the operating system manages CPU and memory; a process is more than a program; the PCB holds the process identity and state; the five-state model; the job queue versus the ready queue.

**Files**

- `ProcessState.java` — NEW, READY, RUNNING, WAITING, TERMINATED
- `PCB.java` — id, state, burst, remaining burst, memory, and the Chapter 5 times
- `Slice.java` — one interval on the CPU (start, end, burst at start, burst at end)
- `ScheduleResult.java` — averages for waiting, completion, turnaround, response, utilization, throughput
- `JobFile.java` — parser for `job.txt`
- `Machine.java` — job queue, ready queue, 8192 MB counter, reader/loader handshake

**What the loader does**

`job-reader` parses the file and creates the PCBs. `job-loader` moves a process from WAITING to READY only when `used + request <= 8192`. The scheduler thread never admits a job itself when the loader is running; it calls `requestAdmission()` and waits until the loader finishes that pass. One lock covers the queues.

**Leave these methods public.** Tasks 2, 3, and 4 call them.

```text
Machine.MEMORY_CAPACITY_MB
loadJobs(List<PCB>)
startLoader()
requestAdmission()
stopLoader()
hasReady()
pollReady()
takeShortest()
requeue(PCB)
runFor(PCB, int)
release(PCB)
result(String)
```

## Task 2 — FCFS

**Course material:** non-preemptive FCFS, FIFO ready queue, convoy effect.

**File:** `FcfsScheduler.java`

```java
public ScheduleResult run(Machine machine)
```

The method must:

- call `requestAdmission()` before it looks at the ready queue
- take the process at the head (`pollReady`)
- run it for its whole remaining burst (`runFor`)
- `release` its memory when it finishes
- return `machine.result("FCFS")`

Do not sort the queue. A long job at the front is supposed to delay the short jobs behind it. On `samples/job.txt`, Job3 has a burst of 3 ms but waits 18 ms, because Job1 (8 ms) and Job2 (10 ms) run first.

**Check:** average waiting time on `samples/job.txt` is 11.75 ms. Average completion time is 18.75 ms.

## Task 3 — SJF

**Course material:** non-preemptive SJF, the next CPU burst as the priority, tie breaking, the fact that SJF minimizes average waiting time when every job is available and memory does not block a shorter job.

**File:** `SjfScheduler.java`

```java
public ScheduleResult run(Machine machine)
```

The method must:

- call `requestAdmission()`
- take the ready job with the smallest remaining burst (`takeShortest`)
- run that job to completion
- release its memory
- return `machine.result("SJF")`

Do not preempt. Shortest-remaining-time-first is a different algorithm and is not in this project. On `samples/job.txt` the order is Job3 (3), Job4 (7), Job1 (8), Job2 (10). Average waiting time is 7.75 ms. Average completion time is 14.75 ms.

On `samples/job-memory.txt`, JobB has a shorter burst than JobA but they cannot both sit in memory (5000 + 5000 > 8192). SJF has to respect that. The order is JobC, JobD, JobA, JobB, and the average waiting time is 6.25 ms.

## Task 4 — Round Robin, menu, and printing

**Course material:** time quantum, timer preemption, response time, and why a larger quantum starts to look like FCFS. Also the thread that runs the scheduler beside the reader and the loader.

**Files**

- `RoundRobinScheduler.java` — quantum 3 and quantum 5
- `Runner.java` — calls task 2, task 3, or Round Robin
- `SchedulerApp.java` — menu and command line
- `TextReport.java` — Gantt chart, slice table, averages, comparison

```java
public RoundRobinScheduler(int quantum)
public ScheduleResult run(Machine machine)
```

The method must:

- reject a quantum that is not positive
- run `min(quantum, remaining)`
- if the burst hits 0, `release` the memory
- otherwise `requeue` the process (state goes back to READY)
- return `machine.result("RR-3")` or `machine.result("RR-5")`

On `samples/job.txt`:

| Algorithm | Average waiting | Average completion | Average response |
| --- | ---: | ---: | ---: |
| RR-3 | 14.75 ms | 21.75 ms | 4.50 ms |
| RR-5 | 15.00 ms | 22.00 ms | 7.00 ms |

RR-3 has the best response time of the four algorithms on this file. A quantum of 100 ms on the same file must match FCFS, because no job is preempted. The test checks that.

The command line is:

```bash
java -cp out/team csc227.team.task4.SchedulerApp samples/job.txt --all
java -cp out/team csc227.team.task4.SchedulerApp samples/job.txt --algorithm=rr3
```

## How to check yourself before you push your folder

From the repository root:

```bash
bash scripts/build.sh
```

A passing run prints `All scheduler checks passed.` The checks cover:

- `samples/job.txt` for all four algorithms
- `samples/job-memory.txt` for all four algorithms
- an SJF tie (`samples/job-tie.txt`)
- a job that asks for 9000 MB (`samples/job-toobig.txt`)
- the reader thread plus the loader thread, which must match the single-threaded result
- Round Robin with quantum 100, which must match FCFS

Saved output for the report is regenerated in `docs/test-runs/`.

## What not to change

- Do not give the four algorithms different arrival times. The comparison in the project would no longer be fair.
- Do not free memory in the middle of a quantum. Memory comes back when the process terminates.
- Do not implement priority scheduling or multilevel queues here. They are in the same chapter, but they are not in the project list.
- Do not add a library. `javac` on Java 11 is the whole build.
