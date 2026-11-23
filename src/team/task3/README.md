# Member 3

Thread 3, the medium-term scheduler, and Round Robin.

Watch memory use. Above 90%, swap out the process that occupies the most memory into the suspend queue. Below 70%, swap a suspended process back in. At startup this can be turned on or off. Each time it acts, print the time, the process, the memory use before and after, the action, and the state change.

Round Robin: each process gets five units of CPU time (q = 5 ms).
