package org.firstinspires.ftc.teamcode.teleop;

import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys.Button;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys.Trigger;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

public class BaseTeleOp extends BotOpMode {
    private final boolean isSolo;
    private GamepadEx gamepad1;
    private GamepadEx gamepad2;

    public BaseTeleOp(boolean solo) {
        super();

        this.isSolo = solo;
    }

    public static double slowGain = 0.2;
    public static double curveGain = 3;

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
        gamepad1.readButtons();
        gamepad2.readButtons();

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

        if (!bot.shooter.isInShootingCommand()) {
            if (gamepad1.getButton(Button.RIGHT_BUMPER) || gamepad2.getButton(Button.RIGHT_BUMPER)) {
                bot.intake.intakeFull();
            } else if (gamepad1.getButton(Button.LEFT_BUMPER) || gamepad2.getButton(Button.LEFT_BUMPER)) {
                bot.intake.outtake();
            } else if (gamepad1.getTrigger(Trigger.LEFT_TRIGGER) > 0.2
                    || gamepad2.getTrigger(Trigger.LEFT_TRIGGER) > 0.2) {
                bot.intake.intake();
            } else {
                bot.intake.stop();
            }

            boolean a = (isSolo ? gamepad1 : gamepad2).wasJustPressed(Button.A);
            if (a) {
                bot.intake.toggleGate();
            }
        }

        boolean dpadDown = (isSolo ? gamepad1 : gamepad2).wasJustPressed(Button.DPAD_DOWN);
        if (dpadDown) {
            Scheduler.schedule(bot.shooter.shootPollen(4));
        }

        if (gamepad1.wasJustPressed(Button.DPAD_LEFT)) {
            bot.intake.toggleWxyz();
        }
        if (gamepad1.wasJustPressed(Button.B)){
            bot.shooter.shootPollen(2);
        }

        boolean canControlShooter = !bot.shooter.isInShootingCommand();
        if (!bot.shooter.isInShootingCommand()) {
            boolean leftStick = (isSolo ? gamepad1 : gamepad2).wasJustPressed(Button.LEFT_STICK_BUTTON);
            boolean rightStick = (isSolo ? gamepad1 : gamepad2).wasJustPressed(Button.RIGHT_STICK_BUTTON);

            if (leftStick) {
                bot.shooter.enable();
            }

            if (rightStick) {
                bot.shooter.disable();
            }
        }

        if (!(isSolo && canControlShooter) && gamepad1.wasJustPressed(Button.RIGHT_STICK_BUTTON)) {
            Bot.switchAlliance();
        }

        Pose pose = bot.follower.pose();

        if (gamepad1.wasJustPressed(Button.Y)) {
            resetToCorner(true, pose.y() < 141.5 / 2);
        }

        if (gamepad1.wasJustPressed(Button.X)) {
            resetToCorner(false, pose.y() < 141.5 / 2);
        }

        bot.follower.manual(
                gamepad1.getLeftY()
                        * (gamepad1.getTrigger(Trigger.RIGHT_TRIGGER) > 0.2 ? slowGain : 1),
                -gamepad1.getLeftX(),
                driveCurve(-gamepad1.getRightX()));

        if (bot.rampSensors.getPollenCount() == 4) {
            gamepad1.gamepad.rumble(100);
        }

        if (!bot.turret.isOutOfRange()) {
            gamepad2.gamepad.rumble(100);
        }

        super.loop();
    }
}
