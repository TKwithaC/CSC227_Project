package team.task1;

import java.nio.file.Path;
import java.util.List;

public final class JobReader {
    private JobReader() {
    }

    public static void read(final Path file, final List<PCB> jobQueue, final List<PCB> all, final Object lock) {
        final RuntimeException[] failure = new RuntimeException[1];
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PCB> jobs = JobFile.parse(file);
                    synchronized (lock) {
                        if (!all.isEmpty()) {
                            throw new IllegalStateException("Processes were already loaded");
                        }
                        for (int i = 0; i < jobs.size(); i++) {
                            PCB copy = jobs.get(i).copy();
                            jobQueue.add(copy);
                            all.add(copy);
                        }
                    }
                } catch (RuntimeException ex) {
                    failure[0] = ex;
                }
            }
        }, "job-reader");
        reader.start();
        try {
            reader.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("job-reader was interrupted", ex);
        }
        if (failure[0] != null) {
            throw failure[0];
        }
        synchronized (lock) {
            if (all.isEmpty()) {
                throw new IllegalStateException("job-reader did not finish");
            }
        }
    }
}
