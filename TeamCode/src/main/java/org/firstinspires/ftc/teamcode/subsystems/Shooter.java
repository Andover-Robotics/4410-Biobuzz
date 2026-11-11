package org.firstinspires.ftc.teamcode.subsystems;

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
    public static double kP = 0.02;
    public static double kI = 0;
    public static double kD = 0;
    public static double kF = 0.003;
    public static double windupRange = 0;

    public static double velocityTolerance = 100;
    public static double inToleranceTimeMs = 150;

    public static boolean manual = false;
    public static double manualVelocity = 0;

    public static double shootAllPollenTime = 1000.0;

    private static final InterpLUT VELOCITY_LOOKUP_TABLE = new InterpLUT()
            .add(0, 2300)
            .add(50, 2301)
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
        this.motor1.setInverted(true);

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
        setPower(outputVolts / voltageSensor.getVoltage());
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
        double primaryVelocity = -motor1.getVelocity() / motor1.getCPR() * 60;
        double backupVelocity = -motor2.getVelocity() / motor2.getCPR() * 60;
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
        motor1.set(-power);
        motor2.set(-power);
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

    public Command shootAllPollen() {
        return sequential(
                Commands.instant(() -> inShootingCommand = true),
                Commands.instant(intake::intakeFull),
                Commands.instant(this::enable),
                Commands.waitUntil(this::inTolerance),
                Commands.instant(intake::openGate),
                Commands.waitMs(shootAllPollenTime),
                Commands.instant(this::disable),
                Commands.instant(intake::stop),
                Commands.instant(intake::closeGate),
                Commands.instant(() -> inShootingCommand = false)
        );
    }
}
