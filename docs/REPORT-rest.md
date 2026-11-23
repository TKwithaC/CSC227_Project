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

## 6. When memory gets in the way

`samples/job-memory.txt` is not in the handout. We added it because the handout's four jobs all fit, so a program could ignore the 8192 MB rule and still match the sample. These four jobs do not fit together:

| Job | Burst | Memory |
| --- | ---: | ---: |
| JobA | 6 | 5000 MB |
| JobB | 4 | 5000 MB |
| JobC | 5 | 3000 MB |
| JobD | 2 | 1000 MB |

JobA and JobB need 10000 MB together, so one of them has to terminate before the other can be admitted. At time 0 the loader admits JobA (5000) and JobC (3000). Used memory is 8000 MB. JobB does not fit. JobD would bring the total to 9000 MB, so JobD waits as well.

FCFS then runs the ready queue in admission order: JobA, then JobC. When JobA terminates, JobB fits and JobD still does not. When JobC terminates, JobD fits. The order is JobA, JobC, JobB, JobD.

SJF is the interesting case. JobB's burst (4) is shorter than JobA's burst (6), but JobB cannot enter memory while JobA is still resident. Among the jobs that are actually ready, SJF runs JobC (5) before JobA (6). After JobC finishes, JobD fits and is shorter than JobA, so JobD runs next. JobB enters only after JobA leaves. The order is JobC, JobD, JobA, JobB. Memory admission changed the schedule SJF would have used if memory were unlimited (that ideal order would have been JobD, JobB, JobC, JobA).

| Algorithm | Avg waiting | Avg completion | Avg response |
| --- | ---: | ---: | ---: |
| FCFS | 8.00 | 12.25 | 8.00 |
| SJF | 6.25 | 10.50 | 6.25 |
| RR-3 | 9.00 | 13.25 | 7.00 |
| RR-5 | 8.50 | 12.75 | 7.25 |

SJF still has the lowest average waiting time, but only among the jobs the loader was allowed to make ready. The trace for this file is `docs/test-runs/memory-all.txt`.

A separate file, `samples/job-toobig.txt`, contains one job that asks for 9000 MB. The program reports it and schedules the other job. The rejected job is not part of the average.

## 7. Software and hardware

**Software**

- Language: Java. The source is written for Java 11 (`javac --release 11`). We compiled and ran it on OpenJDK 21.
- No external libraries. The build is `scripts/build.sh`, which calls `javac` and then the checker.
- Input and output are text. The Gantt chart is monospace text, so it can be pasted into a report or a terminal without a graphics library.
- The checker, `tests/csc227/check/OracleTest.java`, compares the reference tree (`src/ai`) and the team tree (`src/team`) against schedules we computed by hand, including the threaded run.

**Hardware**

Any lab machine that can run Java 11 is enough. The project allows at most 30 jobs, and each decision looks at that ready queue once. There is no large array, no disk index, and no network. The CPU and memory limits that matter are the ones inside the simulation (one CPU, 8192 MB), not the limits of the computer that runs the simulator.

## 8. Strengths and weaknesses

**Strengths**

- The PCB carries the fields the handout names, and the trace shows every state change, including the memory wait.
- A job cannot enter the ready queue unless it fits. Jobs that cannot ever fit are reported instead of being silently dropped from the Gantt chart with no explanation.
- FCFS, SJF, RR-3, and RR-5 share one admission rule, so the comparison table is about the scheduling policy.
- The output has both a Gantt chart and a numeric table, plus the burst at both ends of every slice.
- The reader and the loader are real threads, and the test refuses a threaded run whose slices differ from the direct run.
- The handout file and the tight-memory file are both checked against hand calculation.

**Weaknesses**

- There is no I/O burst. WAITING never means blocked on a device, and the CPU never goes idle while a process waits for disk. Utilization is 100% on every file we tried, which would not be true in a system with I/O.
- Dispatch latency is 0. A real timer interrupt and a real context switch cost time, and a very small quantum would look worse once that cost is added. Our RR-3 versus RR-5 comparison does not include that cost.
- SJF is handed the exact burst. The chapter's hard problem, predicting the next burst, is outside the project.
- We did not implement priority scheduling, multilevel queues, or multilevel feedback queues. They are in the same chapter. They are not in the project list, and adding them would change the four-way comparison.
- Memory is a single counter. The program does not place a job at an address, and it does not model paging or fragmentation. Those topics come after CPU scheduling.
- The ready queue is scanned in full for SJF. With at most 30 jobs that cost does not matter. It would not be the structure we would keep for a much larger run.

## 9. Gantt chart or table?

We print both, and we would not drop either one.

A Gantt chart is the right picture when the question is who had the CPU, and in what order. The convoy effect on the handout file is obvious in the FCFS chart: Job3 is a short block sitting after two long ones. The RR-3 chart shows the same jobs cut into slices of 3 ms, which is the quantum in picture form. A table of averages hides that pattern. Someone reading only average waiting time 11.75 ms cannot see why.

A table is the right picture when the question is which algorithm is better on waiting time or on response time. Computing those averages off a long Round Robin chart is slow and easy to get wrong. The RR-3 chart on the handout file has eleven slices. The table answers the comparison in one row. The project also asks for the burst at the start and at the end of each selection. That is a table, not a chart: the chart shows the job name and the clock, and the slice table shows the remaining burst.

So the chart explains the schedule, and the table evaluates it. A scheduler that printed only a chart would be easier to watch and harder to grade. A scheduler that printed only averages would be easier to grade and would hide the convoy effect the chapter wants us to recognize. The program writes the chart, the slice log, the per-process table, and the four-algorithm comparison.

## 10. From this simulator to a whole operating system

The program only does admission plus short-term scheduling. A full operating system adds the rest of the stack around those two pieces. The changes we would make, in the order the course introduces them, are these.

**Processes and the PCB.** The PCB would gain the program counter, the registers, the list of open files, and the CPU-scheduling information the chapter lists and we do not store. Creating a process would be a system call (`fork` / `exec`) rather than a line in `job.txt`. Termination would be `exit`, and a parent would `wait` for it.

**I/O and the waiting state.** Each job would carry a sequence of CPU bursts and I/O bursts. On an I/O request the running process would move to WAITING, the dispatcher would pick another ready process, and an interrupt at the end of the I/O would move the first process back to READY. The CPU could then be idle, and utilization would drop below 100%. Response time and waiting time would no longer be the same number under FCFS.

**The timer.** Round Robin would be driven by a timer interrupt at the quantum, not by a counted loop inside `runFor`. The interrupt handler would be the preemption path we already have: save the remaining burst, move the process to the tail, and call the dispatcher. Dispatch latency would be added to every switch. We would then expect a very small quantum to waste the CPU on switching, which is the warning in the Round Robin section.

**A second scheduler for memory.** When a job does not fit, a medium-term scheduler could swap a ready or waiting process out and free its memory. Our loader only waits. Swapping is the next step, and it needs a backing store. After that, paging replaces the single 8192 MB counter with a page table. The admission test becomes whether there are enough free frames, instead of whether the byte counter fits.

**Synchronization.** The ready queue is already shared by the loader and the scheduler. We protect it with one Java lock. In the kernel that lock becomes a spinlock or a semaphore, and the interrupt handler has to be careful not to sleep while it holds it. The synchronization chapter is the place those tools are defined. We stayed with one monitor so the project does not depend on material past the scheduling chapter, but the race the lock prevents is already real: the loader and the scheduler both touch the ready queue.

**More than one resource.** If processes also claimed devices or files, the admission step could deadlock. Deadlock prevention or detection would sit on top of the memory check we have now. We do not have a second resource type, so deadlock does not arise.

**The policies we left out.** Priority scheduling, with aging so a low-priority job does not starve, and a multilevel feedback queue, which is how many real systems approximate SJF without being told the burst, would be new classes next to `SjfScheduler` and `RoundRobinScheduler`. They would use the same PCB, the same loader, and the same report. That is why the team split puts the machine in one task and each policy in its own task.

None of those changes replace the four algorithms. They wrap them. The short-term scheduler in this project would still be the function that, given a ready queue, picks the next process.

## 11. How the team divided the work

The program is one simulation. Splitting it by algorithm, and putting the process model in its own task, matches the way the course separates what a process is from which policy picks the next process.

| Member | Folder | Responsibility |
| --- | --- | --- |
| 1 | `src/team/task1` | PCB, five states, `job.txt`, job queue, ready queue, 8192 MB admission, the reader and loader lock |
| 2 | `src/team/task2` | FCFS: head of the ready queue, run to completion |
| 3 | `src/team/task3` | Non-preemptive SJF, ties broken by file order |
| 4 | `src/team/task4` | RR-3 and RR-5, the menu, the Gantt chart and the tables |

`src/ai` is the reference copy of the same behavior in one package. `bash scripts/build.sh` compiles both and fails if a CPU slice differs. A member can replace their folder and rerun that script before pushing.

## 12. Conclusion

On the handout file every job fits in memory and arrives at time 0. SJF has the lowest average waiting time (7.75 ms) and the lowest average completion time (14.75 ms). Round Robin with quantum 3 has the lowest average response time (4.50 ms) and a higher waiting time than FCFS. Raising the quantum from 3 to 5 moves Round Robin toward FCFS: response time gets worse, and a quantum larger than every burst matches FCFS exactly. FCFS is the simplest policy and shows the convoy effect directly: a 3 ms job waits 18 ms.

The 8192 MB limit is not visible on the handout file, because 7800 MB fits. It is visible as soon as two jobs need 5000 MB each. SJF then cannot run the globally shortest job until a resident job terminates and frees memory. Admission and dispatch are different decisions, and the program keeps them in different threads for that reason.

The averages above are the averages printed by the program. The checker recomputes them from the per-process completion times, so a later edit that breaks FCFS, SJF, or either quantum fails `scripts/build.sh` instead of failing on the day of the presentation.
