package org.firstinspires.ftc.teamcode.subsystems;

import static com.pedropathing.ivy.commands.Commands.conditional;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.seattlesolvers.solverslib.controller.PIDFController;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.util.InterpLUT;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class Shooter {
    public static double kP = 0.025;
    public static double kI = 0;
    public static double kD = 0;
    public static double kF = 0.00255;
    public static double windupRange = 0;
    public static boolean voltageCompensation = true;

    public static double velocityTolerance = 100;
    public static double inToleranceTimeMs = 250;

    public static boolean manual = false;
    public static double manualVelocity = 0;

    public static double[] shootTimes = {0, 0, 0, 500};

    public static double MIN_DISTANCE = 32;
    public static double MAX_DISTANCE = 52;

    /*
    32 3100
    34 3100
    37 3100
    40 3125
    43 3125
    46 3125
    49 3250
    52 3250
     */

    private static final InterpLUT VELOCITY_LOOKUP_TABLE = new InterpLUT()
            .add(MIN_DISTANCE, 3100)
            .add(34, 3100)
            .add(37,3100)
            .add(40,3125)
            .add(43,3125)
            .add(46,3125)
            .add(49, 3250)
            .add(MAX_DISTANCE,3250)
            .createLUT();

    private final Telemetry telemetry;
    private final MotorEx motor1;
    private final MotorEx motor2;
    private final VoltageSensor voltageSensor;
    private final PIDFController controller;
    private final Intake intake;
    private final Turret turret;
    private final BooleanDebouncer toleranceDebouncer = new BooleanDebouncer();

    private boolean enabled;
    private boolean usingPrimaryEncoder = true;
    private boolean shooterMotorDisconnected;

    private double targetVelocity;
    private double realVelocity;
    private double currentDrawOne;
    private double currentDrawTwo;

    private boolean inShootingCommand = false;

    public Shooter(Telemetry telemetry, MotorEx motor1, MotorEx motor2,
                   VoltageSensor voltageSensor,
                   Intake intake, Turret turret) {
        this.telemetry = telemetry;
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.voltageSensor = voltageSensor;
        this.intake = intake;
        this.turret = turret;

        this.motor1.setRunMode(Motor.RunMode.RawPower);
        this.motor2.setRunMode(Motor.RunMode.RawPower);
        this.motor2.setInverted(true);

        controller = new PIDFController(kP, kI, kD, kF);
        controller.integrationControl.setIntegrationBounds(-windupRange, windupRange);
    }

    public void periodic() {
        targetVelocity = requestedVelocity();
        updateMotorData();
        updateTolerance();

        if (!enabled) {
            setPower(0);
            return;
        }

        controller.setPIDF(kP, kI, kD, kF);
        controller.integrationControl.setIntegrationBounds(-windupRange, windupRange);
        double outputVolts = controller.calculate(realVelocity, targetVelocity);

        if (voltageCompensation) {
            setPower(outputVolts / voltageSensor.getVoltage());
        } else {
            setPower(outputVolts);
        }
    }

    public void updateTelemetry() {
        telemetry.addData("Shooter/Enabled", enabled);
        telemetry.addData("Shooter/Target velocity", targetVelocity);
        telemetry.addData("Shooter/Actual velocity", realVelocity);
        telemetry.addData("Shooter/At speed", inTolerance());
        telemetry.addData("Shooter/Motor 1 current", currentDrawOne);
        telemetry.addData("Shooter/Motor 2 current", currentDrawTwo);
        telemetry.addData("Shooter/Encoder", usingPrimaryEncoder ? "PRIMARY" : "BACKUP");
        telemetry.addData("Shooter/Motor disconnect detected", shooterMotorDisconnected);
    }

    private double requestedVelocity() {
        if (manual) {
            return manualVelocity;
        }

        return VELOCITY_LOOKUP_TABLE.get(turret.getDistanceToGoal());
    }

    private void updateMotorData() {
        double primaryVelocity = motor1.getVelocity() / motor1.getCPR() * 60;
        double backupVelocity = motor2.getVelocity() / motor2.getCPR() * 60;
        realVelocity = usingPrimaryEncoder ? primaryVelocity : backupVelocity;

        currentDrawOne = motor1.getCurrent(CurrentUnit.AMPS);
        currentDrawTwo = motor2.getCurrent(CurrentUnit.AMPS);

        if (!enabled || Math.abs(targetVelocity) < 1e-3) {
            return;
        }

        if ((currentDrawOne < 1e-3) != (currentDrawTwo < 1e-3)) {
            shooterMotorDisconnected = true;
        }

        if (usingPrimaryEncoder && Math.abs(primaryVelocity) < 1e-3
                && Math.abs(backupVelocity) >= 1e-3) {
            usingPrimaryEncoder = false;
            realVelocity = backupVelocity;
        }
    }

    private void updateTolerance() {
        boolean currentlyInTolerance = enabled
                && Math.abs(targetVelocity - realVelocity) < velocityTolerance;

        toleranceDebouncer.periodic(currentlyInTolerance, inToleranceTimeMs, 0);
    }

    private void setPower(double power) {
        motor1.set(power);
        motor2.set(power);
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
        controller.reset();
        toleranceDebouncer.periodic(false, 0, 0);
    }

    public Command enableCommand() {
        return Commands.instant(this::enable).requiring(motor1, motor2);
    }

    public Command disableCommand() {
        return Commands.instant(this::disable).requiring(motor1, motor2);
    }

    public double getTargetVelocity() {
        return targetVelocity;
    }

    public double getRealVelocity() {
        return realVelocity;
    }

    private double getCurrentDrawOne() {
        return currentDrawOne;
    }

    private double getCurrentDrawTwo() {
        return currentDrawTwo;
    }

    public boolean inTolerance() {
        return toleranceDebouncer.getState();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isShooterMotorDisconnected() {
        return shooterMotorDisconnected;
    }

    public boolean isUsingPrimaryEncoder() {
        return usingPrimaryEncoder;
    }

    public boolean isInShootingCommand() {
        return inShootingCommand;
    }

    public Command shootPollen(double shootTime) {
        return conditional(() -> !turret.isOutOfRange(), sequential(
                Commands.instant(() -> inShootingCommand = true),
                Commands.instant(intake::intakeFull),
                Commands.instant(this::enable),
                Commands.waitUntil(this::inTolerance),
                Commands.instant(intake::openGate),
                Commands.waitMs(shootTime),
                Commands.instant(intake::closeGate),
                Commands.instant(intake::store),
                Commands.instant(() -> inShootingCommand = false)),
                Command.NOOP);
    }

    public Command shootPollen(int count) {
        return shootPollen(shootTimes[count - 1]);
    }
}
