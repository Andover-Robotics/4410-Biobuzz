package org.firstinspires.ftc.teamcode.teleop;

import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

public class BaseTeleOp extends BotOpMode {
    private final boolean isSolo;

    public BaseTeleOp(boolean solo) {
        super();

        this.isSolo = solo;
    }

    public static double turningGain = 0.8;

    public void resetToCorner() {
        switch (Bot.allianceColor) {
            case RED:
                bot.follower.setPose(new Pose(
                        13.710 / 2, 13.780 / 2, Math.toRadians(90)));
                break;
            case BLUE:
                bot.follower.setPose(new Pose(
                        141.5 - 13.710 / 2, 141.5 - 13.780 / 2, Math.toRadians(270)));
                break;
        }
    }
    public void resetToCornerPose(){
        switch (Bot.allianceColor) {
            case RED:
                bot.follower.setPose(new Pose(
                        13.710 / 2, 13.780 / 2, bot.follower.pose().heading()));
                break;
            case BLUE:
                bot.follower.setPose(new Pose(
                        141.5 - 13.710 / 2, 141.5 - 13.780 / 2, bot.follower.pose().heading()));
                break;
        }

    }


    @Override
    public void start(){
        super.init();
        bot.follower.setPose(Bot.storedPose);
        bot.follower.update();

    }

    @Override
    public void loop() {
        if (!bot.shooter.isInShootingCommand()) {
            if (gamepad1.right_bumper || gamepad2.right_bumper) {
                bot.intake.intakeFull();
            } else if (gamepad1.left_bumper || gamepad2.left_bumper) {
                bot.intake.outtake();
            } else if (gamepad1.left_trigger > 0.2 || gamepad2.left_trigger > 0.2) {
                bot.intake.intake();
            } else {
                bot.intake.stop();
            }

            boolean a = isSolo ? gamepad1.aWasPressed() : gamepad2.aWasPressed();
            if (a) {
                bot.intake.toggleGate();
            }
        }

        boolean dpadDown = isSolo ? gamepad1.dpadDownWasPressed() : gamepad2.dpadDownWasPressed();
        if (dpadDown) {
            Scheduler.schedule(bot.shooter.shootAllPollen());
        }

        if (gamepad1.dpadLeftWasPressed()){
            bot.intake.toggleWxyz();
        }

        if (!bot.shooter.isInShootingCommand()) {
            double rightTrigger = isSolo ? gamepad1.right_trigger : gamepad2.right_trigger;
            if (rightTrigger > 0.2) {
                bot.shooter.enable();
            } else {
                bot.shooter.disable();
            }
        }

        if (gamepad1.rightStickButtonWasPressed()) {
            Bot.switchAlliance();
        }

        if (gamepad1.yWasPressed()) {
            resetToCorner();
        }
        if (gamepad1.xWasPressed()){
            resetToCornerPose();
        }
        double slowGain = gamepad1.right_trigger >0.2 ? 0.3 : 1;

        bot.follower.manual(
                -gamepad1.left_stick_y * slowGain,
                -gamepad1.left_stick_x,
                gamepad1.right_stick_x * -turningGain);

        if (bot.rampSensors.getPollenCount() == 4) {
            gamepad1.rumble(100);
        }

        if (!bot.turret.isOutOfRange()) {
            gamepad2.rumble(100);
        }

        super.loop();
    }
}
