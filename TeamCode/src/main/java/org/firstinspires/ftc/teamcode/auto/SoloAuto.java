package org.firstinspires.ftc.teamcode.auto;

import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
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
    }

    public Pose getStartingPose() {
        // starts on the inner edge of the first field tile
        return poseFactory.of(141.5 / 2 - 23.1 + (13.780 / 2), 13.710 / 2, 180);
    }

    public Pose getPickupGardenPose() {
        // corner of the garden
        return poseFactory.of(2.559 + 13.780 / 2, 13.710 / 2, 180);
    }

    public Pose getFlowerPose() {
        return poseFactory.of(48, 120 + 5, 90);
    }

    public Pose getPickupAlliance() {
        // alliance pollen
        return poseFactory.of(24, 120, 180);
    }

    public Pose getFlowerPose2() {
        return poseFactory.of(18, 48, 180);
    }

    public Command autoCommand() {
        return sequential(
                instant(bot.shooter::enable),
//                bot.shooter.shootAllPollen(),
                instant(bot.intake::intake),
                follow(bot.follower, Paths.line(getStartingPose(), getPickupGardenPose()).tangent()),
                follow(bot.follower, Paths.line(getPickupGardenPose(), poseFactory.of(48, 120, 0))
                        .heading(Interpolator.piecewise()
                            .until(0.5, Interpolator.constant(Math.toRadians(270)))
                            .until(1.0, Interpolator.constant(Math.toRadians(90))))),
                instant(bot.intake::downWxyz),
//                bot.shooter.shootAllPollen(),
                instant(bot.intake::intakeFull),
                follow(bot.follower, Paths.line(poseFactory.of(48, 120, 90), getFlowerPose())
                        .tangent()),
                instant(bot.intake::openGate),
                waitMs(1500),
                follow(bot.follower, Paths.line(getFlowerPose(), poseFactory.of(48, 120, 0))
                        .reverseTangent()),
                instant(bot.intake::closeGate),
                instant(bot.intake::upWxyz),
                instant(bot.intake::intake),
                follow(bot.follower, Paths.line(poseFactory.of(48, 120, 0), getPickupAlliance())
                        .tangent()
                        .with(Constants.foresightConfig.maxPathSpeed.at(0.5))),
                waitMs(500),
                follow(bot.follower, Paths.path(
                        Paths.line(getPickupAlliance(), poseFactory.of(24, 48, 0)),
                        Paths.line(poseFactory.of(24, 48, 0), poseFactory.of(48, 24, 0))
                ).constant(Math.toRadians(180))),
                follow(bot.follower, Paths.path(
                        Paths.line(poseFactory.of(48, 24, 0), poseFactory.of(24, 48, 0)),
                        Paths.line(poseFactory.of(24, 48, 0), getFlowerPose2())
                ).constant(Math.toRadians(180))),
                follow(bot.follower, Paths.path(
                        Paths.line(getFlowerPose2(), poseFactory.of(24, 48, 0)),
                        Paths.line(poseFactory.of(24, 48, 0), poseFactory.of(48, 24, 0))
                ).constant(Math.toRadians(180))),
                follow(bot.follower, Paths.line(poseFactory.of(48, 24, 0), poseFactory.of(24, 96, 0))
                        .constant(Math.toRadians(180)))
        );
    }

    @Override
    public void start() {
        poseFactory = PoseFactory.degrees();

        if (Bot.allianceColor == Bot.AllianceColor.BLUE) {
            poseFactory = poseFactory
                    .mirrorX(141.5 / 2)
                    .mirrorY(141.5 / 2);
        }

        bot.follower.setPose(getStartingPose());

        Scheduler.schedule(autoCommand());
    }

    @Override
    public void loop() {
        super.loop();
    }
}
