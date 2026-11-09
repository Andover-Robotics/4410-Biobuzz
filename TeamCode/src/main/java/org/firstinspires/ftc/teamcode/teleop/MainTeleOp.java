package org.firstinspires.ftc.teamcode.teleop;

import androidx.core.view.TintableBackgroundView;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@TeleOp(name = "TeleOp", group = "Competition")
public class MainTeleOp extends BotOpMode {
    @Override
    public void init(){
        super.init();
    }
    @Override
    public void loop() {
        if (!bot.shooter.isInShootingCommand()) {
            if (gamepad1.left_bumper) {
                bot.intake.intake();
            } else if (gamepad1.right_bumper) {
                bot.intake.outtake();
            } else {
                bot.intake.stop();
            }

            if (gamepad1.aWasPressed()) {
                bot.intake.toggleGate();
            }
        }

        double driveSpeed = Math.max(0, 1 - 0.75 * gamepad1.right_trigger);
        bot.drive.applyDrive(new DrivePowers(
                -gamepad1.left_stick_y * driveSpeed,
                -gamepad1.left_stick_x * driveSpeed,
                -gamepad1.right_stick_x * driveSpeed));

        if (gamepad1.dpadDownWasPressed()) {
            Scheduler.schedule(bot.shooter.shootAllPollen());
        }
        if(gamepad1.dpadLeftWasPressed()){
            bot.intake.toggleWxyz();
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
