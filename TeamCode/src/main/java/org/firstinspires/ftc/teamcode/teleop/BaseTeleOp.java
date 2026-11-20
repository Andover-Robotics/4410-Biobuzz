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

    public static double slowGain = 0.2;
    public static double curveGain = 3;

    private int resetState = 0;

    public void resetToCorner(boolean heading, boolean right) {
        if (right) {
            bot.follower.setPose(new Pose(
                    13.710 / 2, 13.780 / 2,
                    heading ? Math.toRadians(90) : bot.follower.pose().heading()));
        } else {
            bot.follower.setPose(new Pose(
                    13.710 / 2, 141.5 - 13.780 / 2,
                    heading ? Math.toRadians(90) : bot.follower.pose().heading()));
        }

        bot.follower.update();
    }

    public static double driveCurve(double input) {
        return Math.pow(curveGain, Math.abs(input) - 1) * input;
    }

    @Override
    public void init_loop() {
        super.init_loop();

        String startingSide = "";

        switch (resetState) {
            case 0:
                startingSide = "No Reset";
                break;
            case 1:
                startingSide = "Left";
                break;
            case 2:
                startingSide = "Right";
                break;
        }

        telemetry.addData("Starting Side", startingSide);

        if (gamepad1.aWasPressed()) {
            resetState++;
        }

        if (resetState == 3) {
            resetState = 0;
        }
    }

    @Override
    public void start() {
        super.start();

        if (resetState == 1) {
            resetToCorner(true, false);
        } else if (resetState == 2) {
            resetToCorner(true, true);
        }
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
            Scheduler.schedule(bot.shooter.shootPollen(4));
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

        Pose pose = bot.follower.pose();

        if (gamepad1.yWasPressed()) {
            resetToCorner(true, pose.y() < 141.5 / 2);
        }

        if (gamepad1.xWasPressed()) {
            resetToCorner(false, pose.y() < 141.5 / 2);
        }

        bot.follower.manual(
                -gamepad1.left_stick_y
                        * (gamepad1.right_trigger > 0.2 ? slowGain : 1),
                -gamepad1.left_stick_x,
                driveCurve(-gamepad1.right_stick_x));

        if (bot.rampSensors.getPollenCount() == 4) {
            gamepad1.rumble(100);
        }

        if (!bot.turret.isOutOfRange()) {
            gamepad2.rumble(100);
        }

        super.loop();
    }
}
