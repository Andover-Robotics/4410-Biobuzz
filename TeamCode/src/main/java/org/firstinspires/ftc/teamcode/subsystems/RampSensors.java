package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class RampSensors {
    public static final class BallReading {
        private final BB.Ball size;
        private final ColorUtils.Color color;

        public BallReading(BB.Ball size, ColorUtils.Color color) {
            this.size = size;
            this.color = color;
        }

        public BB.Ball getSize() {
            return size;
        }

        public ColorUtils.Color getColor() {
            return color;
        }
    }
    private static final int POLLEN_SIZE = 28;
    private static final int NECTAR_SIZE = 36;
    public static int maxPushLength = 100;

    private final Telemetry telemetry;
    private final Intake intake;
    private final Color color;
    private final Optional<Display> display;

    private final Deque<BallReading> queue = new ArrayDeque<>();

    private int length = 0;
    private boolean displayDirty = true;

    public void addToQueueIntaking(BallReading ballReading) {
        if (length >= maxPushLength) {
            return;
        }

        queue.push(ballReading);

        if (ballReading.getSize() == BB.Ball.NECTAR) {
            length += NECTAR_SIZE;
        } else if (ballReading.getSize() == BB.Ball.POLLEN) {
            length += POLLEN_SIZE;
        }

        displayDirty = true;
    }

    public void removeFromQueueOuttaking() {
        BallReading ballReading;

        if (!queue.isEmpty()) {
            ballReading = queue.pollFirst();
        } else {
            return;
        }

        if (ballReading.getSize() == BB.Ball.NECTAR) {
            length -= NECTAR_SIZE;
        } else if (ballReading.getSize() == BB.Ball.POLLEN) {
            length -= POLLEN_SIZE;
        }

        displayDirty = true;
    }

    public void removeFromQueueShooting() {
        BallReading ballReading;

        if (!queue.isEmpty()) {
            ballReading = queue.pollLast();
        } else {
            return;
        }

        if (ballReading.getSize() == BB.Ball.NECTAR) {
            length -= NECTAR_SIZE;
        } else if (ballReading.getSize() == BB.Ball.POLLEN) {
            length -= POLLEN_SIZE;
        }

        displayDirty = true;
    }

    public RampSensors(Telemetry telemetry, RevColorSensorV3 colorSensor,
                       DigitalChannel bottomChannel, DigitalChannel topChannel,
                       Intake intake, Optional<Display> display) {
        this.telemetry = telemetry;
        this.intake = intake;
        this.color = new Color(
                telemetry,
                colorSensor,
                bottomChannel,
                topChannel,
                (size, color) -> {
                    if (intake.getState() == Intake.State.OUTTAKE) {
                        removeFromQueueOuttaking();
                    } else if (intake.getState() == Intake.State.INTAKE
                            || intake.getState()  == Intake.State.INTAKE_FULL) {
                        addToQueueIntaking(new BallReading(size, color));
                    }
                });

        this.display = display;
    }

    public void periodic() {
        color.periodic(intake.getState() != Intake.State.OUTTAKE);

        display.ifPresent((display) -> {
            if (!displayDirty) {
                return;
            }

            List<ColorUtils.Color> colors = new ArrayList<>();
            for (RampSensors.BallReading reading : queue) {
                colors.add(reading.getColor());
            }
            display.setColors(colors);
            display.periodic();
            displayDirty = false;
        });
    }

    public void updateTelemetry() {
        telemetry.addData("Ramp/Queue (intake to shooter)", queue.isEmpty() ? "EMPTY" : toString());
        telemetry.addData("Ramp/Ball count", getQueueSize());
        telemetry.addData("Ramp/Length", length);
        color.updateTelemetry();
    }

    public String toString() {
        StringBuilder displayString = new StringBuilder();
        for (RampSensors.BallReading reading : queue) {
            if (displayString.length() > 0) {
                displayString.append(" -> ");
            }
            displayString.append(reading.getSize()).append(" (")
                    .append(reading.getColor()).append(")");
        }
        return displayString.toString();
    }

    public Deque<BallReading> getQueue() {
        return queue;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public double getLength() {
        return length;
    }
}
