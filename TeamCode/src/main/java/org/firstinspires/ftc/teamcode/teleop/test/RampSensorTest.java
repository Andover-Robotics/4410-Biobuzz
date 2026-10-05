package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayDeque;
import java.util.Deque;

@TeleOp(name = "Ramp Sensor Test", group = "Test")
public class RampSensorTest extends OpMode {
    private Intake intake;
    private Color color;
    private BB topBB;

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
        intake = new Intake(new MotorEx(hardwareMap, "intake"));

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        topBB = new BB(
                hardwareMap.get(DigitalChannel.class, "topBB"),
                departedSize -> {
                    if (!intake.isOut()) {
                        queue.pollFirst();
                    }
                });
        color = new Color(
                hardwareMap.get(RevColorSensorV3.class, "color"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "bottomBBTop"),
                (size, detectedColor) -> {
                    if (intake.isOut()) {
                        queue.pollLast();
                    } else {
                        queue.addLast(new BallReading(size, detectedColor));
                    }
                });
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
        if (gamepad1.dpad_up) {
            intake.setPower(Intake.inPower);
        } else if (gamepad1.dpad_down) {
            intake.setPower(Intake.outPower);
        } else {
            intake.setPower(Intake.storePower);
        }

        color.periodic();
        topBB.periodic();

        telemetry.addData("Intake reversing", intake.isOut());
        telemetry.addData("Bottom color", color.getColor());
        float[] hsvValues = color.getHsvValues();
        telemetry.addData("Bottom HSV", "%.1f, %.3f, %.3f", hsvValues[0], hsvValues[1], hsvValues[2]);
        telemetry.addData("Top blocked", topBB.isBroken());
        telemetry.addData("Ramp", formatRampQueue());
        telemetry.update();
    }

    @Override
    public void stop() {
        intake.setPower(0);
    }
}
