package org.firstinspires.ftc.teamcode.teleop;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@TeleOp(name = "TeleOp", group = "Competition")
public class MainTeleOp extends BotOpMode {
    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            if (gamepad1.right_bumper) {
                bot.intake.intakeFull();
            } else {
                bot.intake.intake();
            }
        } else if (gamepad1.right_bumper) {
            bot.intake.outtake();
        } else {
            bot.intake.stop();
        }

        if (gamepad1.aWasPressed()) {
            bot.intake.toggleGate();
        }

        bot.drive.applyDrive(new DrivePowers(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x));

        if (gamepad1.dpadUpWasPressed()) {
            Scheduler.schedule(bot.outtake.shootAllPollen());
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
                bot.follower.setPose(new Pose(14.0 / 2, 141.5 - 13.75 / 2, Math.toRadians(-90)));
            } else if (Bot.allianceColor == Bot.AllianceColor.BLUE) {
                bot.follower.setPose(new Pose(141.5 - 14.0 / 2, 141.5 - 13.75 / 2, Math.toRadians(-90)));
            }
        }

        super.loop();
    }
}
