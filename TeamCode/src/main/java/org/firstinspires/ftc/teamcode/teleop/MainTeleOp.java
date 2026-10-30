package org.firstinspires.ftc.teamcode.teleop;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@TeleOp(name = "Main TeleOp", group = "Competition")
public class MainTeleOp extends BotOpMode {
    @Override
    public void loop() {
        if (gamepad1.left_trigger > 0.2) {
            bot.intake.intake();
        } else if (gamepad1.left_bumper) {
            bot.intake.outtake();
        } else if (gamepad1.right_bumper) {
            bot.intake.intakeFull();
        } else {
            bot.intake.stop();
        }

        if (gamepad2.aWasPressed()) {
            bot.intake.toggleGate();
        }

        double driveSpeed = Math.max(0, 1 - 0.75 * gamepad1.right_trigger);
        bot.drive.applyDrive(new DrivePowers(
                -gamepad1.left_stick_y * driveSpeed,
                -gamepad1.left_stick_x * driveSpeed,
                -gamepad1.right_stick_x * driveSpeed));

        if (gamepad2.dpadDownWasPressed()) {
            Scheduler.schedule(bot.shooter.shootAllPollen());
        }

        if (gamepad1.rightStickButtonWasPressed()) {
            if (Bot.allianceColor == Bot.AllianceColor.RED) {
                Bot.allianceColor = Bot.AllianceColor.BLUE;
            } else if (Bot.allianceColor == Bot.AllianceColor.BLUE) {
                Bot.allianceColor = Bot.AllianceColor.RED;
            }
        }

        if (gamepad1.yWasPressed()) {
            switch (Bot.allianceColor) {
                case RED:
                    bot.follower.setPose(new Pose(14.0 / 2, 141.5 - 13.75 / 2, Math.toRadians(-90)));
                    break;
                case BLUE:
                    bot.follower.setPose(new Pose(141.5 - 14.0 / 2, 141.5 - 13.75 / 2, Math.toRadians(-90)));
                    break;
            }
        }

        super.loop();
    }
}
