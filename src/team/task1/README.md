# Task 1 — Processes, memory, and the queues

Member 1 owns this folder. The other three tasks compile against these types, so the public method names below have to stay.

| File | Role |
| --- | --- |
| `ProcessState.java` | New, Ready, Running, Waiting, Terminated |
| `PCB.java` | Process id, state, burst, memory, and the Chapter 5 times |
| `Slice.java` | One interval of the Gantt chart |
| `ScheduleResult.java` | Averages and the trace |
| `JobFile.java` | Reads job.txt |
| `Machine.java` | Job queue, ready queue, 8192 MB, reader and loader threads |

`Machine` methods the schedulers call:

- `loadJobs`, `startLoader`, `stopLoader`, `requestAdmission`
- `hasReady`, `pollReady`, `takeShortest`, `requeue`
- `runFor`, `release`, `result`

Memory is 8192 MB. A job larger than that is rejected and left out of the averages. A job that does not fit right now stays in the job queue (state Waiting) until a running job terminates and its memory is released. Do not block the whole queue behind one job that is too big for the free memory.
