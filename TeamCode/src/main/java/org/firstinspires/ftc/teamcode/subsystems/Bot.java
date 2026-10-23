package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
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

    public final Follower follower;
    private final VoltageSensor voltageSensor;
    public final Display display;
    public final Intake intake;
    public final RampSensors rampSensors;
    public final Turret turret;
    public final Shooter outtake;

    public enum AllianceColor {
        RED,
        BLUE,
        NONE
    }

    public static AllianceColor allianceColor = AllianceColor.RED;

    public Bot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = new MultipleTelemetry(
                opMode.telemetry,
                FtcDashboard.getInstance().getTelemetry());
        follower = Constants.create(hardwareMap);
        voltageSensor = opMode.hardwareMap.voltageSensor.iterator().next();
        display = new Display(List.of(
                new ServoEx(hardwareMap, "display1"),
                new ServoEx(hardwareMap, "display2"),
                new ServoEx(hardwareMap, "display3"),
                new ServoEx(hardwareMap, "display4")));
        intake = new Intake(
                new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150),
                new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150),
                new ServoEx(hardwareMap, "gate"));
        rampSensors = new RampSensors(
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                intake,
                Optional.of(display));
        intake.setRampSensors(rampSensors);
        turret = new Turret(follower,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));
        outtake = new Shooter(
                new MotorEx(hardwareMap, "outtake1", Motor.GoBILDA.BARE),
                new MotorEx(hardwareMap, "outtake2", Motor.GoBILDA.BARE),
                new ServoEx(hardwareMap, "outtakeServo"),
                voltageSensor);
    }

    public void schedulePeriodic() {
        Scheduler.schedule(Commands.infinite(() -> {
            follower.update();
            turret.periodic();
            outtake.setDistanceToGoal(turret.getDistanceToGoal());
            outtake.periodic();
        }));
    }
}
