package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

@TeleOp(name = "Ramp Sensor Test", group = "Test")
public class RampSensorTest extends OpMode {
    private Color color;
    private BB topBeam;

    private final Deque<BallReading> queue = new ArrayDeque<>();

    private static class BallReading {
        private final BB.Ball size;
        private final ColorUtils.Color color;

        private BallReading(BB.Ball size, ColorUtils.Color color) {
            this.size = size;
            this.color = color;
        }
    }

    @Override
    public void init() {
        queue.clear();

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        topBeam = new BB(
                hardwareMap.get(DigitalChannel.class, "topBB"),
                departedSize -> queue.pollFirst());
        color = new Color(
                hardwareMap.get(ColorSensor.class, "color"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                Optional.of(hardwareMap.get(DigitalChannel.class, "bottomBBTop")),
                (size, detectedColor) -> queue.addLast(new BallReading(size, detectedColor)));
    }

    private String formatRampQueue() {
        StringBuilder display = new StringBuilder();
        for (BallReading reading : queue) {
            if (display.length() > 0) {
                display.append(" -> ");
            }
            display.append(reading.size).append(" (").append(reading.color).append(")");
        }
        return display.toString();
    }

    @Override
    public void loop() {
        color.periodic();
        topBeam.periodic();

        telemetry.addData("Bottom color", color.getColor());
        float[] hsvValues = color.getHsvValues();
        telemetry.addData("Bottom HSV", "%.1f, %.3f, %.3f", hsvValues[0], hsvValues[1], hsvValues[2]);
        telemetry.addData("Top blocked", topBeam.isBroken());
        telemetry.addData("Ramp", formatRampQueue());
        telemetry.update();
    }
}
