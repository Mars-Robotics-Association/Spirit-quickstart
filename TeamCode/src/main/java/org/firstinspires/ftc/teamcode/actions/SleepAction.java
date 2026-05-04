package org.firstinspires.ftc.teamcode.actions;

public class SleepAction implements Action {
    private final double startTime;
    private final double sleepTime;

    public SleepAction(double seconds) {
        startTime = System.nanoTime() * 1e-9;
        sleepTime = seconds;
    }

    @Override
    public boolean Run() {
        double current = System.nanoTime() * 1e-9;
        double runtime = current - startTime;
        return runtime < sleepTime;
    }
}
