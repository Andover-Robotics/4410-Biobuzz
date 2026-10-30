package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayDeque;
import java.util.Deque;

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
    public static int maxPushLength = 100;

    private final Telemetry telemetry;
    private final Intake intake;
    private final Color color;

    private final Deque<BallReading> queue = new ArrayDeque<>();

    private int length = 0;

    public void addToQueueIntaking(BallReading ballReading) {
        if (length >= maxPushLength) {
            return;
        }

        int diameter = ballReading.getSize().getDiameter();
        queue.addFirst(ballReading);
        length += diameter;
    }

    public void removeFromQueueOuttaking() {
        onRemoved(queue.pollFirst());
    }

    public void removeFromQueueShooting() {
        onRemoved(queue.pollLast());
    }

    private void onRemoved(BallReading ballReading) {
        if (ballReading == null) {
            return;
        }

        length -= ballReading.getSize().getDiameter();
    }

    public RampSensors(Telemetry telemetry, RevColorSensorV3 colorSensor,
                       DigitalChannel bottomChannel, DigitalChannel topChannel,
                       Intake intake) {
        this.telemetry = telemetry;
        this.intake = intake;
        this.color = new Color(
                telemetry,
                colorSensor,
                bottomChannel,
                topChannel,
                (size, color) -> {
                    switch (intake.getState()) {
                        case OUTTAKE:
                            removeFromQueueOuttaking();
                            break;
                        case INTAKE:
                        case INTAKE_FULL:
                            addToQueueIntaking(new BallReading(size, color));
                            break;
                        default:
                            break;
                    }
                });
    }

    public void periodic() {
        color.periodic(intake.getState() != Intake.State.OUTTAKE);
    }

    public void updateTelemetry() {
        telemetry.addData("Ramp/Queue (intake to shooter)", queue.isEmpty() ? "EMPTY" : toString());
        telemetry.addData("Ramp/Ball count", queue.size());
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
