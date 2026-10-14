package org.firstinspires.ftc.teamcode.subsystems.sensors;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

import java.util.Optional;

@Config
public class BB {
    public static double blockedDebounceMs = 10;
    public static double clearDebounceMs = 10;

    private final DigitalChannel bottomChannel;
    private final Optional<DigitalChannel> topChannel;

    private final OnLeave onLeave;
    private final BooleanDebouncer bottomDebouncer = new BooleanDebouncer();
    private final BooleanDebouncer topDebouncer = new BooleanDebouncer();

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
        read();
    }

    private void read() {
        boolean bottomRawBroken = !bottomChannel.getState();
        boolean topRawBroken = topChannel.map(channel -> !channel.getState()).orElse(false);
        bottomDebouncer.periodic(bottomRawBroken, blockedDebounceMs, clearDebounceMs);
        topChannel.ifPresent(channel -> topDebouncer.periodic(
                topRawBroken, blockedDebounceMs, clearDebounceMs));

        boolean bottomBroken = bottomDebouncer.getState();
        boolean topBroken = topDebouncer.getState();

        if (bottomBroken) {
            sawBottom |= bottomBroken;
            sawTop |= topBroken;
        }
    }

    private Ball getBallSize() {
        if (!sawBottom) {
            return Ball.NONE;
        } else if (!topChannel.isPresent()) {
            return Ball.BALL;
        } else if (sawTop) {
            return Ball.NECTAR;
        } else {
            return Ball.POLLEN;
        }
    }

    public void periodic() {
        boolean wasBroken = getBottomState();
        read();
        boolean broken = getBottomState();

        if (wasBroken && !broken) {
            Ball size = getBallSize();
            sawBottom = false;
            sawTop = false;
            onLeave.onLeave(size);
        }
    }

    public boolean getBottomState() {
        return bottomDebouncer.getState();
    }

    public boolean getTopState() {
        return topDebouncer.getState();
    }
}
