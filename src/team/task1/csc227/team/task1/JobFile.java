package csc227.team.task1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads job.txt. Each job is two lines:
 *
 * <pre>
 * Job1
 * 5, 8, 800
 * </pre>
 *
 * The three numbers are the process id, the CPU burst in milliseconds, and
 * the memory requirement in MB. Blank lines and lines that start with # are
 * ignored. At most 30 jobs are accepted, which is the limit stated in the
 * project.
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
        List<String> lines = new ArrayList<String>();
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 0; i < rawLines.length; i++) {
            String trimmed = rawLines[i].replace("\r", "").trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            lines.add(trimmed);
            numbers.add(Integer.valueOf(i + 1));
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("job.txt contains no jobs");
        }
        if (lines.size() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Each job needs a name line and a data line. The file ends after line "
                            + numbers.get(numbers.size() - 1) + ": " + lines.get(lines.size() - 1));
        }

        List<PCB> jobs = new ArrayList<PCB>();
        for (int i = 0; i < lines.size(); i += 2) {
            String name = lines.get(i);
            String data = lines.get(i + 1);
            int nameLine = numbers.get(i).intValue();
            int dataLine = numbers.get(i + 1).intValue();
            if (!name.startsWith("Job")) {
                throw new IllegalArgumentException(
                        "Line " + nameLine + ": job name must start with \"Job\" (example: Job1). Found: " + name);
            }
            if (name.indexOf(',') >= 0) {
                throw new IllegalArgumentException(
                        "Line " + nameLine + ": put the job name and the three numbers on separate lines.");
            }
            String[] parts = data.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException(
                        "Line " + dataLine
                                + ": expected Process ID, burst time in ms, memory required in MB. Found: " + data);
            }
            int pid = parseInt(parts[0], dataLine, "Process ID", false);
            int burst = parseInt(parts[1], dataLine, "burst time", false);
            int memory = parseInt(parts[2], dataLine, "memory", true);
            jobs.add(new PCB(name, pid, burst, memory, jobs.size()));
        }
        if (jobs.size() > 30) {
            throw new IllegalArgumentException("The project allows at most 30 jobs, found " + jobs.size());
        }
        return jobs;
    }

    private static int parseInt(String token, int line, String label, boolean allowZero) {
        try {
            int value = Integer.parseInt(token.trim());
            if (allowZero) {
                if (value < 0) {
                    throw new IllegalArgumentException(
                            "Line " + line + ": " + label + " must be zero or a positive integer. Found: " + token.trim());
                }
            } else if (value <= 0) {
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
