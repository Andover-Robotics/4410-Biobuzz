package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.Display;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.RampSensors;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Turret;

import java.util.List;
import java.util.Optional;

@TeleOp(name = "Ramp Sensor Test", group = "Test")
public class RampSensorTest extends OpMode {
    private Intake intake;
    private Display display;
    private RampSensors rampSensors;
    private Mecanum drive;
    private Shooter shooter;
    private Follower follower;
    private Turret turret;

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

        VoltageSensor voltageSensor = hardwareMap.voltageSensor.iterator().next();

        shooter = new Shooter(
                new MotorEx(hardwareMap, "shooter1", Motor.GoBILDA.BARE),
                new MotorEx(hardwareMap, "shooter2", Motor.GoBILDA.BARE),
                new ServoEx(hardwareMap, "shooterServo"),
                voltageSensor,
                intake,
                rampSensors);

        follower = Constants.create(hardwareMap);
        turret = new Turret(
                follower,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));
    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            if (gamepad1.right_bumper) {
                intake.intakeFull();
            } else {
                intake.intake();
            }
        } else if (gamepad1.right_bumper) {
            intake.outtake();
        } else {
            intake.stop();
        }

        if (gamepad1.aWasPressed()) {
            intake.toggleGate();
        }

        drive.applyDrive(new DrivePowers(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x));

        if (gamepad1.dpadUpWasPressed()) {
            Scheduler.schedule(shooter.shootAllPollen());
        }

        if (gamepad1.bWasPressed()) {
            if (Bot.allianceColor == Bot.AllianceColor.RED) {
                Bot.allianceColor = Bot.AllianceColor.BLUE;
            } else if (Bot.allianceColor == Bot.AllianceColor.BLUE) {
                Bot.allianceColor = Bot.AllianceColor.RED;
            }
        }

        if (gamepad1.xWasPressed()) {
            if (Bot.allianceColor == Bot.AllianceColor.RED) {
                follower.setPose(new Pose(14.0 / 2, 141.5 - 13.75 / 2, Math.toRadians(-90)));
            } else if (Bot.allianceColor == Bot.AllianceColor.BLUE) {
                follower.setPose(new Pose(141.5 - 14.0 / 2, 141.5 - 13.75 / 2, Math.toRadians(-90)));
            }
        }

        telemetry.addData("Ramp", rampSensors);
        telemetry.addData("Length", rampSensors.getLength());
        telemetry.addData("Top stalled", intake.isTopStalled());
        telemetry.addData("Bottom stalled", intake.isBottomStalled());

        telemetry.addData("Target RPM", shooter.getTargetVelocity());
        telemetry.addData("Actual RPM", shooter.getRealVelocity());
        telemetry.addData("At speed", shooter.inTolerance());

        telemetry.addData("Alliance", Bot.allianceColor);

        follower.update();
        intake.periodic();
        rampSensors.periodic();
        shooter.periodic();
        turret.periodic();

        Pose pose = follower.pose();
        Vector2D aimPoint = turret.getAimPoint();
        telemetry.addData("Pose", "%f %f %f",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
        telemetry.addData("Aim Point", "%f %f", aimPoint.x(), aimPoint.y());
        telemetry.update();

        Scheduler.execute();
    }

    @Override
    public void stop() {

    }
}
