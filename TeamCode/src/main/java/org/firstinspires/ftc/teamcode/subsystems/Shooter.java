package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.seattlesolvers.solverslib.controller.PIDFController;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.util.InterpLUT;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

@Configurable
public class Shooter {
    public static double kP = 0;
    public static double kI = 0;
    public static double kD = 0;
    public static double kF = 0;
    public static double windupRange = 0;

    public static double velocityTolerance = 100;
    public static double inToleranceTimeSeconds = 0.150;

    public static boolean manual = false;
    public static double manualVelocity = 0;

    private static final double ENCODER_RPM_PER_TICK_PER_SECOND = 60.0 / 28.0;

    private static final InterpLUT VELOCITY_LOOKUP_TABLE = new InterpLUT()
            .add(0,0)
            .add(1,0)
            .createLUT();

    private final VoltageSensor voltageSensor;
    private final MotorEx motor1;
    private final MotorEx motor2;
    private final PIDFController controller;

    private boolean enabled;
    private boolean inTolerance;
    private boolean usingPrimaryEncoder = true;
    private boolean shooterMotorDisconnected;

    private long toleranceStartNanos = -1;
    private double targetVelocity;
    private double distanceToGoal;
    private double realVelocity;
    private double currentDrawOne;
    private double currentDrawTwo;

    public Shooter(MotorEx motor1, MotorEx motor2, VoltageSensor voltageSensor) {
        this.voltageSensor = voltageSensor;
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.motor1.setRunMode(Motor.RunMode.RawPower);
        this.motor2.setRunMode(Motor.RunMode.RawPower);

        controller = new PIDFController(kP, kI, kD, kF);
        controller.integrationControl.setIntegrationBounds(-windupRange, windupRange);
    }

    public void periodic() {
        targetVelocity = requestedVelocity();
        updateMotorData();
        updateTolerance();

        if (!enabled) {
            setPower(0);
            controller.reset();
            return;
        }

        controller.setPIDF(kP, kI, kD, kF);
        controller.integrationControl.setIntegrationBounds(-windupRange, windupRange);
        double outputVolts = controller.calculate(realVelocity, targetVelocity);
        setPower(outputVolts / voltageSensor.getVoltage());
    }

    private double requestedVelocity() {
        return manual ? manualVelocity : VELOCITY_LOOKUP_TABLE.get(distanceToGoal);
    }

    private void updateMotorData() {
        double primaryVelocity = motor1.getVelocity() * ENCODER_RPM_PER_TICK_PER_SECOND;
        double backupVelocity = motor2.getVelocity() * ENCODER_RPM_PER_TICK_PER_SECOND;
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

        if (!currentlyInTolerance) {
            toleranceStartNanos = -1;
            inTolerance = false;
            return;
        }

        if (toleranceStartNanos < 0) {
            toleranceStartNanos = System.nanoTime();
        }
        inTolerance = (System.nanoTime() - toleranceStartNanos) / 1e9
                >= inToleranceTimeSeconds;
    }

    private void setPower(double power) {
        motor1.set(power);
        motor2.set(power);
    }

    public void setDistanceToGoal(double distanceToGoal) {
        this.distanceToGoal = distanceToGoal;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
        inTolerance = false;
        toleranceStartNanos = -1;
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

    public boolean inTolerance() {
        return inTolerance;
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
}
