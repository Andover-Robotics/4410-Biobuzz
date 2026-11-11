package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Display;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.RampSensors;

import java.util.List;
import java.util.Optional;

@TeleOp(name = "Ramp Sensor Test", group = "Test")
public class RampSensorTest extends OpMode {
    private Intake intake;
    private Display display;
    private RampSensors rampSensors;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());

        rampSensors = new RampSensors(
                telemetry,
                hardwareMap.get(DigitalChannel.class, "rampBB1"),
                hardwareMap.get(DigitalChannel.class, "rampBB2"),
                hardwareMap.get(DigitalChannel.class, "rampBB3"),
                hardwareMap.get(DigitalChannel.class, "rampBB4"));

        MotorEx intakeMotor1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx intakeMotor2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        intakeMotor1.setInverted(true);
        intake = new Intake(
                telemetry,
                intakeMotor1, intakeMotor2,
                new ServoEx(hardwareMap, "gate"),
                new ServoEx(hardwareMap, "wxyz"),
                rampSensors);
        intake.closeGate();
        intake.upWxyz();

        display = new Display(telemetry, List.of(
                new ServoEx(hardwareMap, "display1"),
                new ServoEx(hardwareMap, "display2"),
                new ServoEx(hardwareMap, "display3"),
                new ServoEx(hardwareMap, "display4")),
                Optional.empty(), Optional.of(rampSensors));
    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            intake.intake();
        } else if (gamepad1.right_bumper) {
            intake.outtake();
        } else if (gamepad1.left_trigger > 0.2) {
            intake.intakeFull();
        } else {
            intake.store();
        }

        if (gamepad1.aWasPressed()) {
            intake.toggleGate();
        }

        rampSensors.periodic();
        intake.periodic();
        display.periodic();

        rampSensors.updateTelemetry();
        intake.updateTelemetry();
        display.updateTelemetry();
        telemetry.update();
    }

    @Override
    public void stop() {
        intake.stop();
    }
}
