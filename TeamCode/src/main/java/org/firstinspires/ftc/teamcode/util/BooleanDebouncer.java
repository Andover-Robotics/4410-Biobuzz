package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.ElapsedTime;

public class BooleanDebouncer {
    private final ElapsedTime stateTimer = new ElapsedTime();
    private boolean state;
    private boolean observedState;

    public void periodic(boolean input, double trueDebounceMs, double falseDebounceMs) {
        if (input != observedState) {
            observedState = input;
            stateTimer.reset();
        }

        double debounceMs = observedState ? trueDebounceMs : falseDebounceMs;
        if (observedState != state && stateTimer.milliseconds() >= debounceMs) {
            state = observedState;
        }
    }

    public boolean getState() {
        return state;
    }
}
