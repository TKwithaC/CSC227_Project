package csc227.ai.task1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads job.txt. Each job is one line:
 *
 * <pre>
 * Process ID: burst time in ms: priority; memory required in MB
 * 1:25:4;500
 * </pre>
 *
 * Blank lines are ignored. Priority must be an integer from 1 to 30.
 */
public final class JobFile {
    private JobFile() {
    }

    public static List<PCB> parse(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return parse(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read " + path + ": " + ex.getMessage(), ex);
        }
    }

    public static List<PCB> parse(String text) {
        String[] rawLines = text.split("\n", -1);
        List<PCB> jobs = new ArrayList<PCB>();
        Set<Integer> seen = new HashSet<Integer>();
        for (int i = 0; i < rawLines.length; i++) {
            String line = rawLines[i].replace("\r", "").trim();
            if (line.isEmpty()) {
                continue;
            }
            int lineNumber = i + 1;
            int semi = line.indexOf(';');
            if (semi < 0) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": expected Process ID: burst: priority; memory. Found: " + line);
            }
            String left = line.substring(0, semi).trim();
            String memoryToken = line.substring(semi + 1).trim();
            String[] parts = left.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": expected Process ID: burst: priority; memory. Found: " + line);
            }
            int pid = positive(parts[0], lineNumber, "Process ID");
            int burst = positive(parts[1], lineNumber, "burst time");
            int priority = positive(parts[2], lineNumber, "priority");
            int memory = positive(memoryToken, lineNumber, "memory");
            if (priority > 30) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": priority must be in the range 1 to 30. Found: " + priority);
            }
            if (!seen.add(Integer.valueOf(pid))) {
                throw new IllegalArgumentException("Line " + lineNumber + ": duplicate process ID " + pid);
            }
            jobs.add(new PCB(pid, burst, priority, memory, jobs.size()));
        }
        if (jobs.isEmpty()) {
            throw new IllegalArgumentException("job.txt contains no processes");
        }
        return jobs;
    }

    private static int positive(String token, int line, String label) {
        try {
            int value = Integer.parseInt(token.trim());
            if (value <= 0) {
                throw new IllegalArgumentException(
                        "Line " + line + ": " + label + " must be a positive integer. Found: " + token.trim());
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Line " + line + ": " + label + " must be an integer. Found: " + token.trim());
        }
    }
}
