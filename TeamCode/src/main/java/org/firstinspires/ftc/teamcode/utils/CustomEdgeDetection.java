package org.firstinspires.ftc.teamcode.utils;

import java.util.function.Supplier;

public class CustomEdgeDetection {
    private boolean prevCondition;
    private boolean condition;
    private boolean rawCondition;

    // Debounce Support
    private final long debounceTimeMs;
    private long lastChangeTime;

    /**
     * @param debounceTimeMs Minimum stable time before accepting a new state
     */
    public CustomEdgeDetection(long debounceTimeMs) {
        this.debounceTimeMs = debounceTimeMs;

        rawCondition = condition = prevCondition = false;
        lastChangeTime = System.currentTimeMillis();
    }

    /**
     * Call this every loop.
     */
    public void check(boolean condition) {
        prevCondition = this.condition;

        long now = System.currentTimeMillis();

        if (condition != rawCondition) {
            // raw signal changed — start debounce timer
            rawCondition = condition;
            lastChangeTime = now;
        }

        // Accept change only if stable long enough
        if ((now - lastChangeTime) >= debounceTimeMs) {
            this.condition = rawCondition;
        }
    }

    public boolean wasPressed() {
        return !prevCondition && condition;
    }

    public boolean wasReleased() {
        return prevCondition && !condition;
    }
}
