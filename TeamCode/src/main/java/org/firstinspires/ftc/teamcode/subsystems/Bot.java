package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;
import java.util.Optional;

public class Bot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;

    public final Mecanum drive;
    public final Follower follower;
    public final VoltageSensor voltageSensor;
    public final Display display;
    public final Intake intake;
    public final RampSensors rampSensors;
    public final Turret turret;
    public final Shooter shooter;

    public enum AllianceColor {
        RED,
        BLUE
    }

    public static AllianceColor allianceColor = AllianceColor.RED;

    public Bot(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;
        MotorEx intakeMotor1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx intakeMotor2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        intakeMotor2.setInverted(true);
        intake = new Intake(
                telemetry,
                intakeMotor1, intakeMotor2,
                new ServoEx(hardwareMap, "gate"),
                new ServoEx(hardwareMap, "wxyz"));
        intake.closeGate();

        rampSensors = new RampSensors(
                telemetry,
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                intake);
        intake.setRampSensors(rampSensors);

        drive = new Mecanum(hardwareMap, Constants.driveConfig);
        drive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        follower = Constants.create(hardwareMap);
        turret = new Turret(telemetry, follower,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));

        display = new Display(telemetry, List.of(
                new ServoEx(hardwareMap, "display1"),
                new ServoEx(hardwareMap, "display2"),
                new ServoEx(hardwareMap, "display3"),
                new ServoEx(hardwareMap, "display4")),
                Optional.of(turret), Optional.of(rampSensors));

        voltageSensor = hardwareMap.voltageSensor.iterator().next();
        shooter = new Shooter(
                telemetry,
                new MotorEx(hardwareMap, "shooter1", Motor.GoBILDA.BARE),
                new MotorEx(hardwareMap, "shooter2", Motor.GoBILDA.BARE),
                new ServoEx(hardwareMap, "shooterServo"),
                voltageSensor,
                intake,
                rampSensors,
                turret);
    }

    public void schedulePeriodic() {
        Scheduler.schedule(Commands.infinite(() -> {
            follower.update();
            rampSensors.periodic();
            intake.periodic();
            turret.periodic();
            shooter.periodic();
            display.periodic();

            updateTelemetry();
            telemetry.update();
        }));
    }

    public void updateTelemetry() {
        telemetry.addData("Bot/Alliance", allianceColor);
        Pose pose = follower.pose();
        telemetry.addData("Bot/X", pose.x());
        telemetry.addData("Bot/Y", pose.y());
        telemetry.addData("Bot/Heading", Math.toDegrees(pose.heading()));

        intake.updateTelemetry();
        rampSensors.updateTelemetry();
        display.updateTelemetry();
        turret.updateTelemetry();
        shooter.updateTelemetry();
    }
}
