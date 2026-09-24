package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

public class Bot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;
    public final Intake intake;
    public final Turret turret;
    public final Shooter outtake;

    public final VoltageSensor voltageSensor;

    public final Follower follower;

    public Bot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = new MultipleTelemetry(
                opMode.telemetry,
                FtcDashboard.getInstance().getTelemetry());
        follower = Constants.create(hardwareMap);
        voltageSensor = opMode.hardwareMap.voltageSensor.iterator().next();
        intake = new Intake(new MotorEx(hardwareMap, "intake"));
        turret = new Turret(follower,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));
        outtake = new Shooter(
                new MotorEx(hardwareMap, "outtake1"),
                new MotorEx(hardwareMap, "outtake2"),
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
