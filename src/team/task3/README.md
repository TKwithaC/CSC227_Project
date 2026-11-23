# Task 3 — SJF

Member 3. One class: `SjfScheduler`.

Non-preemptive shortest-job-first. All jobs arrive at time 0. Dispatch the ready job with the smallest remaining burst. Break ties by file order (the earlier job wins). Do not preempt.

On `samples/job.txt` the order is Job3, Job4, Job1, Job2. Average waiting time is 7.75 ms. Average completion time is 14.75 ms.

On `samples/job-memory.txt`, JobB is shorter than JobA but they cannot be in memory together. The order is JobC, JobD, JobA, JobB.

Keep `public ScheduleResult run(Machine machine)`.
