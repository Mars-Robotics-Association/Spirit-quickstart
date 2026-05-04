package org.firstinspires.ftc.teamcode.actions;

public class SequentialAction implements Action {
    private final Action[] actions;
    private int currentStep;

    public SequentialAction(Action ...actions){
        this.actions = actions;
        currentStep = 0;
    }

    @Override
    public boolean Run() {
        boolean isStillRunning = actions[currentStep].Run();
        if (isStillRunning) {
            return true;
        } else if (currentStep <= actions.length - 1) { // not on last step
            currentStep++;
            return true;
        } else {
            return false;
        }
    }
}
