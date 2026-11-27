package csc227.team.task1;

public class PCB {
    // البيانات الأساسية المقروءة من الملف
    private final int pid;
    private final int burstTime;
    private int priority;          // قابل للتعديل بسبب الـ Aging في المهمة الرابعة
    private final int memoryRequired;
    private final int arrivalOrder; // لحفظ ترتيب القراءة (تصل كلها في الزمن 0)

    // بيانات الحالة والتتبع أثناء المحاكاة
    private ProcessState state;
    private int remainingTime;
    private int startTime = -1;     // -1 تعني أنها لم تدخل المعالج بعد
    private int terminationTime = 0;
    private int waitingTime = 0;
    private int turnaroundTime = 0;

    // متغيرات مساعدة للمهمة الرابعة (Starvation & Aging)
    private int readyQueueEntryTime = 0;
    private int readyQueueCountAtEntry = 0;
    private boolean starved = false;

    public PCB(int pid, int burstTime, int priority, int memoryRequired, int arrivalOrder) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.memoryRequired = memoryRequired;
        this.arrivalOrder = arrivalOrder;
        this.state = ProcessState.NEW;
    }

    // --- دوال الحسابات التلقائية عند انتهاء العملية ---
    public void calculateMetrics(int currentTime) {
        this.terminationTime = currentTime;
        // بما أن زمن الوصول للجميع هو 0 حسب افتراضات المشروع:
        this.turnaroundTime = this.terminationTime; 
        this.waitingTime = this.turnaroundTime - this.burstTime;
        this.state = ProcessState.TERMINATED;
    }

    // --- Getters and Setters ---
    public int getPid() { return pid; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = Math.max(1, priority); } // الأولوية لا تقل عن 1
    public int getMemoryRequired() { return memoryRequired; }
    public int getArrivalOrder() { return arrivalOrder; }

    public ProcessState getState() { return state; }
    public void setState(ProcessState state) { this.state = state; }

    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime; }

    public int getStartTime() { return startTime; }
    public void setStartTime(int startTime) { 
        // نسجل وقت البداية فقط في أول مرة تدخل فيها العملية للمعالج
        if (this.startTime == -1) {
            this.startTime = startTime;
        }
    }

    public int getTerminationTime() { return terminationTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }

    public int getReadyQueueEntryTime() { return readyQueueEntryTime; }
    public void setReadyQueueEntryTime(int time) { this.readyQueueEntryTime = time; }

    public int getReadyQueueCountAtEntry() { return readyQueueCountAtEntry; }
    public void setReadyQueueCountAtEntry(int count) { this.readyQueueCountAtEntry = count; }

    public boolean isStarved() { return starved; }
    public void setStarved(boolean starved) { this.starved = starved; }
}
