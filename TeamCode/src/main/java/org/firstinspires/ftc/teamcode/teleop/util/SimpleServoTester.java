package org.firstinspires.ftc.teamcode.teleop.util;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import java.util.Map;

@TeleOp(name = "Simple Servo Tester", group = "Test")
public class SimpleServoTester extends OpMode {
    private static final double STEP_POSITION = 0.02;

    private ServoEx servo;
    private String[] servoNames;
    private int selectedServoIndex;

    @Override
    public void init() {
        servo = null;
        selectedServoIndex = 0;
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        servoNames = hardwareMap.servo.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .toArray(String[]::new);
        updateSelectionTelemetry();
    }

    @Override
    public void init_loop() {
        if (servoNames.length > 0 && gamepad1.xWasPressed()) {
            selectedServoIndex = (selectedServoIndex + 1) % servoNames.length;
        }
        updateSelectionTelemetry();
    }

    private void updateSelectionTelemetry() {
        if (servoNames.length == 0) {
            telemetry.addLine("No servos found in hardware map.");
        } else {
            telemetry.addData("Selected servo", servoNames[selectedServoIndex]);
            telemetry.addLine("Press X to select the next servo.");
            telemetry.addLine("Press Start when ready to test.");
        }
        telemetry.update();
    }

    @Override
    public void start() {
        if (servoNames.length == 0) {
            return;
        }
        servo = new ServoEx(hardwareMap, servoNames[selectedServoIndex]);
    }

    @Override
    public void loop() {
        if (servo == null) {
            updateSelectionTelemetry();
            return;
        }

        double position = servo.getRawPosition();
        boolean increasePosition = gamepad1.dpadUpWasPressed();
        boolean decreasePosition = gamepad1.dpadDownWasPressed();
        if (increasePosition) {
            position += STEP_POSITION;
        }
        if (decreasePosition) {
            position -= STEP_POSITION;
        }
        if (increasePosition || decreasePosition) {
            servo.set(position);
        }

        telemetry.addLine("D-pad Up/Down: increase/decrease position.");
        telemetry.addData("Servo", servoNames[selectedServoIndex]);
        telemetry.addData("Step position", STEP_POSITION);
        telemetry.addData("Position", "%.4f", servo.getRawPosition());
        telemetry.update();
    }
}
