package org.firstinspires.ftc.teamcode.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@TeleOp(name = "TeleOp", group = "Competition")
@Config
public class MainTeleOp extends BotOpMode {
    public static double turningGain = 0.8;

    public void resetToCorner() {
        switch (Bot.allianceColor) {
            case RED:
                bot.follower.setPose(new Pose(14.0 / 2, 13.75 / 2, Math.toRadians(90)));
                break;
            case BLUE:
                bot.follower.setPose(new Pose(141.5 - 14.0 / 2, 13.75 / 2, Math.toRadians(90)));
                break;
        }
    }

    @Override
    public void init() {
        super.init();
        resetToCorner();
    }

    @Override
    public void loop() {
        if (!bot.shooter.isInShootingCommand()) {
            if (gamepad1.right_bumper) {
                bot.intake.intakeFull(); // right bumper = override sensors
            } else if (gamepad1.left_bumper) {
                bot.intake.outtake(); // left bumper = outtake
            } else if (gamepad1.left_trigger > 0.2) {
                bot.intake.intake(); // left trigger = sensor intake
            } else {
                bot.intake.store(); // store
            }

            if (gamepad1.aWasPressed()) {
                bot.intake.toggleGate();
            }
        }

        ManualDrive.driveOrHold(
                bot.follower,
                -gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                gamepad1.right_stick_x * turningGain);

        if (gamepad1.dpadDownWasPressed()) {
            Scheduler.schedule(bot.shooter.shootAllPollen());
        }

        if (gamepad1.dpadLeftWasPressed()){
            bot.intake.toggleWxyz();
        }

        if (!bot.shooter.isInShootingCommand()) {
            if (gamepad1.right_trigger > 0.2) {
                bot.shooter.enable();
            } else {
                bot.shooter.disable();
            }
        }

        if (gamepad1.rightStickButtonWasPressed()) {
            if (Bot.allianceColor == Bot.AllianceColor.RED) {
                Bot.allianceColor = Bot.AllianceColor.BLUE;
            } else if (Bot.allianceColor == Bot.AllianceColor.BLUE) {
                Bot.allianceColor = Bot.AllianceColor.RED;
            }
        }

        if (gamepad1.yWasPressed()) {
            resetToCorner();
        }

        super.loop();
    }
}
