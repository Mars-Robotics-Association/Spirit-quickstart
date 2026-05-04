package org.firstinspires.ftc.teamcode.actions;

public class InstantAction implements Action {
    private final Runnable callback;

    public InstantAction(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public boolean Run() {
        callback.run();
        return false;
    }
}
