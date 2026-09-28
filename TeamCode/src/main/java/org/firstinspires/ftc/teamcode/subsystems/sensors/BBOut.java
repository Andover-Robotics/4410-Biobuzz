package org.firstinspires.ftc.teamcode.subsystems.sensors;

import com.qualcomm.robotcore.hardware.DigitalChannel;

public class BBOut {
    private final DigitalChannel bottomChannel;

    private final OnLeave onLeave;

    private boolean broken;

    @FunctionalInterface
    public interface OnLeave {
        void onLeave();
    }

    public BBOut(DigitalChannel bottomChannel, OnLeave onLeave) {
        this.bottomChannel = bottomChannel;
        this.bottomChannel.setMode(DigitalChannel.Mode.INPUT);
        broken = this.bottomChannel.getState();

        this.onLeave = onLeave;
    }

    public void periodic() {
        boolean wasBroken = broken;
        broken = this.bottomChannel.getState();

        if (wasBroken && !broken) {
            onLeave.onLeave();
        }
    }

    public boolean isBroken() {
        return broken;
    }
}
