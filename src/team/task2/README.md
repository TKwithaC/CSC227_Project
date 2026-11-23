# Member 2

Thread 2, the long-term scheduler, and shortest job first.

Keep checking free memory and move the next job from the job queue to the ready queue only when it fits in 2048 MB. Do not load a job that does not fit.

Shortest job first: when more than one ready process has the same CPU burst, run them in arrival order. Processes arrive at time 0, in the order they were read.
