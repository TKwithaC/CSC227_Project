# Reference solution

Package `csc227.ai`. This tree is the answer key for the four-person split in `src/team`.

Run it with:

```bash
java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --all
```

The classes are the whole program in one package: the PCB and the file reader, memory admission, FCFS, SJF, Round Robin, and the text report. The team tree splits those same responsibilities across `task1` through `task4`. `tests/csc227/check/OracleTest.java` runs both and compares every CPU slice.
