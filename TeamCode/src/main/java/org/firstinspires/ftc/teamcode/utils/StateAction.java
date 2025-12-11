package org.firstinspires.ftc.teamcode.utils;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import java.util.function.Function;

public class StateAction<T> implements Action {
    private final Function<T, Action> createAction;
    private final T state;
    private boolean initialized = false;
    private Action action;

    public StateAction(T state, Function<T, Action> createAction) {
        this.state = state;
        this.createAction = createAction;
    }

    private Action getAction() {
        if (!initialized) {
            action = createAction.apply(state);
            initialized = true;
        }
        return action;
    }

    @Override
    public void preview(@NonNull Canvas fieldOverlay) {
        getAction().preview(fieldOverlay);
    }

    @Override
    public boolean run(@NonNull TelemetryPacket telemetryPacket) {
        return getAction().run(telemetryPacket);
    }
}
