package csc227.ai;

/**
 * The five process states from the process chapter.
 * NEW is the moment the PCB is created. WAITING here means the process is in
 * the job queue until the loader can give it memory. READY, RUNNING, and
 * TERMINATED are the usual short-term-scheduler states. This project has no
 * I/O burst, so WAITING is the memory-admission wait rather than an I/O wait.
 */
public enum ProcessState {
    NEW,
    READY,
    RUNNING,
    WAITING,
    TERMINATED
}
