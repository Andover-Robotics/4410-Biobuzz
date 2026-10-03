package org.firstinspires.ftc.teamcode.teleop.util;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import java.util.Map;

@TeleOp(name = "Simple Motor Tester", group = "Test")
public class SimpleMotorTester extends OpMode {
    private MotorEx motor;
    private String[] motorNames;
    private int selectedMotorIndex;

    @Override
    public void init() {
        motor = null;
        selectedMotorIndex = 0;
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        motorNames = hardwareMap.dcMotor.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .toArray(String[]::new);
        updateSelectionTelemetry();
    }

    @Override
    public void init_loop() {
        if (motorNames.length > 0 && gamepad1.xWasPressed()) {
            selectedMotorIndex = (selectedMotorIndex + 1) % motorNames.length;
        }
        updateSelectionTelemetry();
    }

    private void updateSelectionTelemetry() {
        if (motorNames.length == 0) {
            telemetry.addLine("No motors found in hardware map.");
        } else {
            telemetry.addData("Selected motor", motorNames[selectedMotorIndex]);
            telemetry.addLine("Press X to select the next motor.");
            telemetry.addLine("Press Start when ready to test.");
        }
        telemetry.update();
    }

    @Override
    public void start() {
        if (motorNames.length == 0) {
            return;
        }
        motor = new MotorEx(hardwareMap, motorNames[selectedMotorIndex]);
        motor.setRunMode(Motor.RunMode.RawPower);
        motor.set(0);
        motor.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public void loop() {
        if (motor == null) {
            updateSelectionTelemetry();
            return;
        }

        double power = -gamepad1.left_stick_y;
        motor.set(power);

        telemetry.addLine("Left stick Y: motor power.");
        telemetry.addData("Motor", motorNames[selectedMotorIndex]);
        telemetry.addData("Power", "%.2f", power);
        telemetry.addData("Encoder position", motor.getCurrentPosition());
        telemetry.update();
    }

    @Override
    public void stop() {
        if (motor != null) {
            motor.stopMotor();
        }
    }
}
