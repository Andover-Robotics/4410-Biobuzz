package org.firstinspires.ftc.teamcode.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys.Button;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys.Trigger;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@Config
public class BaseTeleOp extends BotOpMode {
    private final boolean isSolo;
    private GamepadEx gamepad1;
    private GamepadEx gamepad2;

    public BaseTeleOp(boolean solo) {
        super();

        this.isSolo = solo;
    }

    public static double driveCurveGain = 3.5;
    public static double maxSotmGoalRate = 15;

    private boolean sotmEnabled = true;
    private boolean autoShootEnabled = true;

    private int resetState = 0;

    @Override
    public void init() {
        super.init();
        gamepad1 = new GamepadEx(super.gamepad1);
        gamepad2 = new GamepadEx(super.gamepad2);
    }

    public void resetToCorner(boolean heading, boolean right) {
        if (right) {
            bot.follower.setPose(new Pose(
                    13.710 / 2, (13.780 / 2 + 2.559),
                    heading ? Math.toRadians(270) : bot.follower.pose().heading()));
        } else {
            bot.follower.setPose(new Pose(
                    13.710 / 2, 141.5 - (13.780 / 2 + 2.559),
                    heading ? Math.toRadians(90) : bot.follower.pose().heading()));
        }

        bot.follower.update();
    }

    public static double driveCurve(double input) {
        return Math.pow(driveCurveGain, Math.abs(input) - 1) * input;
    }

    @Override
    public void init_loop() {
        super.init_loop();
        gamepad1.readButtons();
        gamepad2.readButtons();

        String startingSide = "";

        switch (resetState) {
            case 0:
                startingSide = "NO RESET";
                break;
            case 1:
                startingSide = "LEFT";
                break;
            case 2:
                startingSide = "RIGHT";
                break;
        }

        telemetry.addData("Starting side", startingSide);

        if (gamepad1.wasJustPressed(Button.A)) {
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
        gamepad1.readButtons();
        gamepad2.readButtons();

        GamepadEx operator = isSolo ? gamepad1 : gamepad2;

        if (operator.wasJustPressed(Button.LEFT_STICK_BUTTON)) {
            sotmEnabled = !sotmEnabled;
        }

        if (gamepad1.wasJustPressed(Button.RIGHT_BUMPER)) {
            autoShootEnabled = !autoShootEnabled;
        }

        boolean hasBall = bot.rampSensors.getPollenCount() >= 1;
        boolean full = bot.rampSensors.getPollenCount() == 4;
        boolean inRange = !bot.turret.isOutOfRange();

        boolean canControlShooter = !bot.shooter.isInShootingCommand();
        if (canControlShooter) {
            boolean outtake = isSolo ? gamepad1.isDown(Button.LEFT_BUMPER) :
                    (gamepad1.getTrigger(Trigger.RIGHT_TRIGGER) > 0.2 || gamepad2.getTrigger(Trigger.RIGHT_TRIGGER) > 0.2);

            if (outtake) {
                bot.intake.outtake();
            } else if (gamepad1.getTrigger(Trigger.LEFT_TRIGGER) > 0.2
                    || gamepad2.getTrigger(Trigger.LEFT_TRIGGER) > 0.2) {
                bot.intake.intake();
            } else {
                bot.intake.stop();
            }

            if (gamepad1.wasJustPressed(Button.DPAD_DOWN)) {
                bot.intake.toggleGate();
            }
        }

        if (gamepad1.wasJustPressed(Button.DPAD_LEFT)) {
            bot.intake.toggleWxyz();
        }

        boolean autoShoot = sotmEnabled
                && autoShootEnabled
                && !(gamepad1.getTrigger(Trigger.RIGHT_TRIGGER) > 0.5)
                && canControlShooter
                && hasBall
                && inRange
                && Math.abs(bot.turret.getDistanceToGoalRate()) < maxSotmGoalRate;

        if (operator.wasJustPressed(Button.X)) {
            Scheduler.schedule(bot.shooter.shootPollen(1));
        } else if (operator.wasJustPressed(Button.Y)) {
            Scheduler.schedule(bot.shooter.shootPollen(2));
        } else if (operator.wasJustPressed(Button.B)) {
            Scheduler.schedule(bot.shooter.shootPollen(3));
        } else if (operator.wasJustPressed(Button.A) || autoShoot) {
            Scheduler.schedule(bot.shooter.shootPollen(4));
        }

        if (canControlShooter) {
            boolean rightStick = isSolo ? gamepad1.wasJustPressed(Button.RIGHT_STICK_BUTTON)
                    : gamepad2.wasJustPressed(Button.RIGHT_BUMPER);

            if (rightStick) {
                bot.shooter.enable();
            }

            boolean leftStick = isSolo ? gamepad1.wasJustPressed(Button.LEFT_STICK_BUTTON)
                    : gamepad2.wasJustPressed(Button.LEFT_BUMPER);

            if (leftStick) {
                bot.shooter.disable();
            }
        }

        if (!(isSolo && canControlShooter) && gamepad1.wasJustPressed(Button.RIGHT_STICK_BUTTON)) {
            Bot.switchAlliance();
        }

        Pose pose = bot.follower.pose();

        if (gamepad1.wasJustPressed(Button.DPAD_UP)) {
            resetToCorner(true, pose.y() < 141.5 / 2);
        }

        if (gamepad1.wasJustPressed(Button.DPAD_RIGHT)) {
            resetToCorner(false, pose.y() < 141.5 / 2);
        }

        bot.follower.manual(
                gamepad1.getLeftY(),
                -gamepad1.getLeftX(),
                driveCurve(-gamepad1.getRightX()));

        if (full) {
            gamepad1.gamepad.rumble(100);
        }

        if (inRange) {
            gamepad2.gamepad.rumble(100);
        }

        super.loop();
    }
}
