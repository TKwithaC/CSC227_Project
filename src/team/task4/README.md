# Member 4

The main thread and priority scheduling.

The main thread runs the algorithm the user picks. Priority is non-preemptive. Priority numbers are 1 to 30, and a smaller number runs first. Equal priorities follow arrival order.

A process is starved if it remains in the ready queue for more than N × 5 ms without being dispatched. N is the number of processes in the ready queue when that process enters, and it stays fixed. Every 4 ms, decrease the priority number by 1. Say which processes starved.
