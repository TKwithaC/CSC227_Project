package csc227.ai.task3;

import java.util.Locale;

/** The lines the project asks for whenever the medium-term scheduler intervenes. */
public final class SwapReport {
    private SwapReport() {
    }

    public static String format(Intervention event) {
        StringBuilder out = new StringBuilder();
        out.append("[Time ").append(event.time).append(" ms]\n");
        out.append("Medium-Term Scheduler Intervention\n");
        out.append(String.format(Locale.US, "Memory utilization: %.2f%%%n",
                Double.valueOf(event.utilizationBefore)));
        out.append("Action: ").append(event.action).append(" Process ").append(event.pid).append('\n');
        out.append("Process ").append(event.pid).append(": ")
                .append(event.stateBefore).append(" \u2192 ").append(event.stateAfter).append('\n');
        out.append(String.format(Locale.US, "Memory utilization after intervention: %.2f%%",
                Double.valueOf(event.utilizationAfter)));
        return out.toString();
    }
}
