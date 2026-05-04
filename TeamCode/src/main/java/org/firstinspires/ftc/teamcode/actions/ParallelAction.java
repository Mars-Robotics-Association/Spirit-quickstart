package org.firstinspires.ftc.teamcode.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParallelAction implements Action {
    private List<Action> waitingActions;
    private List<Action> runningActions;

    public ParallelAction(Action... actions) {
        this.runningActions = Arrays.asList(actions);
        this.waitingActions = new ArrayList<>(runningActions.size());
    }

    @Override
    public boolean Run() {
        while (!runningActions.isEmpty()) {
            Action current = runningActions.get(runningActions.size() - 1);
            runningActions.remove(runningActions.size() - 1);
            if (current.Run()) {
                waitingActions.add(current);
            }
        }

        if (waitingActions.isEmpty()) {
            return false;
        }

        List<Action> temp = runningActions;
        runningActions = waitingActions;
        waitingActions = temp;

        return true;
    }
}
