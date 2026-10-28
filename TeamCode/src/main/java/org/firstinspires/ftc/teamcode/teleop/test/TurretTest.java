package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Turret;

@TeleOp(name = "Turret Test", group = "Test")
@Config
public class TurretTest extends OpMode {
    private Follower follower;
    private Turret turret;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        turret = new Turret(
                follower,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));
        follower.setPose(new Pose(0, 0, 0));
    }

    @Override
    public void loop() {
        follower.update();
        Pose pose = follower.pose();

        telemetry.addData("Pose", "%f %f %f",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));

        turret.periodic();
    }

    @Override
    public void stop() {
        Turret.manual = false;
    }
}
