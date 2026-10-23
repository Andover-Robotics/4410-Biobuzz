package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.pedro.Constants;
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
    private Mecanum drive;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());

        MotorEx motor1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx motor2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        motor2.setInverted(true);
        intake = new Intake(
                motor1, motor2,
                new ServoEx(hardwareMap, "gate"));
        intake.closeGate();

        List<ServoEx> servos = List.of(
                new ServoEx(hardwareMap, "display1"),
                new ServoEx(hardwareMap, "display2"),
                new ServoEx(hardwareMap, "display3"),
                new ServoEx(hardwareMap, "display4"));

        display = new Display(servos);

        rampSensors = new RampSensors(
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                intake,
                Optional.of(display));
        intake.setRampSensors(rampSensors);

        drive = new Mecanum(hardwareMap, Constants.driveConfig);
        drive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            intake.intakeFull();
        } else if (gamepad1.right_bumper) {
            intake.outtake();
        } else {
            intake.stop();
        }

        intake.periodic();
        rampSensors.periodic();

        if (gamepad1.aWasPressed()) {
            intake.toggleGate();
        }

        drive.applyDrive(new DrivePowers(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x));

        telemetry.addData("Ramp", rampSensors);
        telemetry.addData("Length", rampSensors.getLength());
        telemetry.update();
    }

    @Override
    public void stop() {

    }
}
