package csc227.team.task1;

public enum ProcessState {
    NEW,        // العملية جديدة في الـ Job Queue
    READY,      // العملية في الذاكرة جاهزة للتنفيذ في الـ Ready Queue
    RUNNING,    // العملية قيد التنفيذ حالياً على المعالج
    SUSPENDED,  // العملية تم إخراجها مؤقتاً من الذاكرة (Swap out)
    TERMINATED  // العملية انتهت من التنفيذ
}
