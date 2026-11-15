package org.firstinspires.ftc.teamcode.auto;

import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.repeat;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;

@Autonomous(name = "Solo Auto", group = "Auto")
public class SoloAuto extends BotOpMode {
    private PoseFactory poseFactory;

    @Override
    public void init() {
        super.init();
        bot.intake.stop();
        bot.intake.closeGate();
        bot.intake.upWxyz();
    }

    public Pose getStartingPose() {
        // starts on the inner edge of the first field tile
        return poseFactory.of(141.5 / 2 - 23.1 + (13.780 / 2), 13.710 / 2, 180);
    }

    public Pose getPickupGardenPose() {
        // corner of the garden
        return poseFactory.of(13.710 / 2 + 0.75, 2.559 + (13.780 / 2) + 0.75, 0);
    }

    public Pose getFlowerPose1() {
        return poseFactory.of(42, 127 + 1, 90);
    }

    public Pose getPickupAlliance() {
        // alliance pollen
        return poseFactory.of(24, 120, 180);
    }

    public Pose getFlowerPose2() {
        return poseFactory.of(16, 48, 180);
    }
    public Pose getParkPose(){
        return poseFactory.of(10,42,180);
    }

    public Path getGardenPath() {
        return Paths.path(
                Paths.curve(getStartingPose(),
                                poseFactory.of(33, 20,0),
                                poseFactory.of(13.710 / 2, 14, 0))
                        .linear(Math.toRadians(180), Math.toRadians(270)),
                Paths.line(poseFactory.of(13.710 / 2, 14, 0), getPickupGardenPose())
                        .constant(Math.toRadians(270)));
    }

    public Command jiggly(Pose pose1, Pose pose2) {
        return repeat(
                sequential(follow(bot.follower, Paths.line(pose1, pose2).tangent()),
                        follow(bot.follower, Paths.line(pose2, pose1).reverseTangent())), 5);
    }

    public Command autoCommand() {
        return sequential(
                instant(bot.shooter::enable),
                bot.shooter.shootAllPollen(),
                instant(bot.intake::intake),

                follow(bot.follower, getGardenPath()),

                follow(bot.follower, Paths.path(
                            Paths.line(getPickupGardenPose(), poseFactory.of(24, 96, 0)),
                            Paths.line(poseFactory.of(24, 96, 0), poseFactory.of(24, 120, 0))
                        ).heading(Interpolator.piecewise()
                            .until(0.5, Interpolator.constant(Math.toRadians(270)))
                            .until(1.0, Interpolator.constant(Math.toRadians(90))))),

                instant(bot.intake::downWxyz),
                instant(bot.intake::intakeFull),
                follow(bot.follower, Paths.line(poseFactory.of(24, 120, 0), poseFactory.of(42, 120, 0))
                        .constant(Math.toRadians(90))),
                bot.shooter.shootAllPollen(),

                instant(bot.intake::openGate),
                follow(bot.follower,
                        Paths.line(poseFactory.of(42, 120, 0), poseFactory.of(42, 127, 0)).tangent()),
                waitMs(1500),
                follow(bot.follower, Paths.line(getFlowerPose1(), poseFactory.of(46, 120, 0))
                        .reverseTangent()),

                instant(bot.intake::closeGate),
                instant(bot.intake::upWxyz),
                instant(bot.intake::intake),
                follow(bot.follower,Paths.line(poseFactory.of(46, 120, 0),getParkPose()).constant(Math.toRadians(0)))
//                follow(bot.follower, Paths.line(poseFactory.of(48, 132, 0), getPickupAlliance())
//                        .tangent()),
//                waitMs(500),
//                follow(bot.follower, Paths.path(
//                        Paths.line(getPickupAlliance(), poseFactory.of(24, 48, 0)),
//                        Paths.line(poseFactory.of(24, 48, 0), poseFactory.of(48, 24, 0))
//                ).constant(Math.toRadians(180))),
//                bot.shooter.shootAllPollen(),
//                instant(bot.intake::downWxyz),
//                follow(bot.follower, Paths.path(
//                        Paths.line(poseFactory.of(48, 24, 0), poseFactory.of(24, 48, 0)),
//                        Paths.line(poseFactory.of(24, 48, 0), getFlowerPose2())
//                ).constant(Math.toRadians(180))),
//                jiggly(getFlowerPose2(), poseFactory.of(16 - 0.5, 48, 180)),
//                follow(bot.follower, Paths.path(
//                        Paths.line(getFlowerPose2(), poseFactory.of(24, 48, 0)),
//                        Paths.line(poseFactory.of(24, 48, 0), poseFactory.of(48, 24, 0))
//                ).constant(Math.toRadians(180))),
//                instant(bot.intake::upWxyz),
//                bot.shooter.shootAllPollen()
        );
    }

    @Override
    public void start() {
        poseFactory = PoseFactory.degrees();
        bot.follower.setPose(getStartingPose());
        Scheduler.schedule(autoCommand());
    }

    @Override
    public void loop() {
        super.loop();
    }
    @Override
    public void stop(){
        Bot.storedPose = bot.follower.pose();
    }
}
