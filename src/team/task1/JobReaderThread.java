package csc227.team.task1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class JobReaderThread extends Thread {
    private final String filePath;
    private final Queue<PCB> jobQueue;
    private final AtomicBoolean readingFinished;

    public JobReaderThread(String filePath, Queue<PCB> jobQueue, AtomicBoolean readingFinished) {
        super("Thread-1-JobReader");
        this.filePath = filePath;
        this.jobQueue = jobQueue;
        this.readingFinished = readingFinished;
    }

    @Override
    public void run() {
        int order = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split("[:;]");
                
                if (parts.length == 4) {
                    int pid = Integer.parseInt(parts[0].trim());
                    int burstTime = Integer.parseInt(parts[1].trim());
                    int priority = Integer.parseInt(parts[2].trim());
                    int memory = Integer.parseInt(parts[3].trim());

                    PCB pcb = new PCB(pid, burstTime, priority, memory, order++);
                    
                    synchronized (jobQueue) {
                        jobQueue.add(pcb);
                        jobQueue.notifyAll(); 
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading job file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in job file: " + e.getMessage());
        } finally {
            
            readingFinished.set(true);
            synchronized (jobQueue) {
                jobQueue.notifyAll();
            }
        }
    }
}
