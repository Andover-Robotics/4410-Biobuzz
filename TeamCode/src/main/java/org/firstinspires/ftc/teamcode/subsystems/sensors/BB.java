package org.firstinspires.ftc.teamcode.subsystems.sensors;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

import java.util.Optional;

@Config
public class BB {
    public static double BLOCKED_DEBOUNCE_MS = 20;
    public static double CLEAR_DEBOUNCE_MS = 100;

    private final DigitalChannel bottomChannel;
    private final Optional<DigitalChannel> topChannel;

    private final OnLeave onLeave;
    private final BooleanDebouncer bottomDebouncer = new BooleanDebouncer();
    private final BooleanDebouncer topDebouncer = new BooleanDebouncer();

    private boolean broken;
    private boolean bottomRawBroken;
    private boolean sawBottom;
    private boolean sawTop;

    public enum Ball {
        NECTAR,
        POLLEN,
        BALL,
        NONE
    }

    @FunctionalInterface
    public interface OnLeave {
        void onLeave(Ball size);
    }

    public BB(DigitalChannel bottomChannel, OnLeave onLeave) {
        this(bottomChannel, Optional.empty(), onLeave);
    }

    public BB(DigitalChannel bottomChannel, Optional<DigitalChannel> topChannel, OnLeave onLeave) {
        this.bottomChannel = bottomChannel;
        this.topChannel = topChannel;
        this.bottomChannel.setMode(DigitalChannel.Mode.INPUT);
        this.topChannel.ifPresent(channel -> channel.setMode(DigitalChannel.Mode.INPUT));

        this.onLeave = onLeave;
        readBeams();
    }

    private void readBeams() {
        bottomRawBroken = !bottomChannel.getState();
        boolean topRawBroken = topChannel.map(channel -> !channel.getState()).orElse(false);
        bottomDebouncer.periodic(bottomRawBroken, BLOCKED_DEBOUNCE_MS, CLEAR_DEBOUNCE_MS);
        topChannel.ifPresent(channel -> topDebouncer.periodic(
                topRawBroken, BLOCKED_DEBOUNCE_MS, CLEAR_DEBOUNCE_MS));

        boolean bottomBroken = bottomDebouncer.getState();
        boolean topBroken = topDebouncer.getState();
        broken = bottomBroken || topBroken || (broken && (bottomRawBroken || topRawBroken));

        if (broken) {
            sawBottom |= bottomBroken;
            sawTop |= topBroken;
        }
    }

    public void periodic() {
        boolean wasBroken = broken;
        readBeams();

        if (wasBroken && !broken) {
            Ball size;
            if (!sawBottom) {
                size = Ball.NONE;
            } else if (!topChannel.isPresent()) {
                size = Ball.BALL;
            } else if (sawTop) {
                size = Ball.NECTAR;
            } else {
                size = Ball.POLLEN;
            }
            sawBottom = false;
            sawTop = false;
            onLeave.onLeave(size);
        }
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean shouldUpdateColor() {
        return bottomDebouncer.getState() && bottomRawBroken;
    }
}
