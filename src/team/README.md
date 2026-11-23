# Team source

Four folders, one member each. They compile as one program. The contract is in [docs/TEAM_TASKS.md](../../docs/TEAM_TASKS.md).

```text
task1/   processes, PCB, job.txt, memory, queues
task2/   FCFS
task3/   SJF
task4/   Round Robin, menu, printed tables
```

```bash
java -cp out/team csc227.team.task4.SchedulerApp samples/job.txt --all
```

Replace only your folder. Keep the public method names, then run `bash scripts/build.sh` from the repository root.
