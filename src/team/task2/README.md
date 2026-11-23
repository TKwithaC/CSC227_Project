# Task 2 — FCFS

Member 2. One class: `FcfsScheduler`.

Non-preemptive first-come-first-served. Take the head of the ready queue and run it until the burst is 0, then release its memory. Do not reorder the queue.

On `samples/job.txt` the average waiting time is 11.75 ms and the average completion time is 18.75 ms. Job3 (burst 3) waits 18 ms behind Job1 and Job2. That is the convoy effect.

Keep `public ScheduleResult run(Machine machine)`.
