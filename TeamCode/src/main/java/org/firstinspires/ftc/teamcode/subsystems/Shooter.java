package org.firstinspires.ftc.teamcode.subsystems;

import static com.pedropathing.ivy.groups.Groups.sequential;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.seattlesolvers.solverslib.controller.PIDFController;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;
import com.seattlesolvers.solverslib.util.InterpLUT;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class Shooter {
    public static double nectarKP = 0.02;
    public static double pollenKP = 0.02;
    public static double kI = 0;
    public static double kD = 0;
    public static double kF = 0.003;
    public static double windupRange = 0;

    public static double velocityTolerance = 100;
    public static double inToleranceTimeMs = 150;

    public static boolean manual = false;
    public static double manualVelocity = 0;

    public enum Size {
        NECTAR,
        POLLEN
    }

    public static double nectarServoPosition = 0.00;
    public static double pollenServoPosition = 0.00;

    public static double shootAllPollenTime = 0.0;

    private Size size = Size.NECTAR;

    private static final InterpLUT NECTAR_VELOCITY_LOOKUP_TABLE = new InterpLUT()
            .add(0, 0)
            .add(1, 0)
            .createLUT();

    private static final InterpLUT POLLEN_VELOCITY_LOOKUP_TABLE = new InterpLUT()
            .add(0, 0)
            .add(1, 0)
            .createLUT();

    private final MotorEx motor1;
    private final MotorEx motor2;
    private final ServoEx servo;
    private final VoltageSensor voltageSensor;
    private final PIDFController controller;
    private final Intake intake;
    private final RampSensors rampSensors;
    private final BooleanDebouncer toleranceDebouncer = new BooleanDebouncer();

    private boolean enabled;
    private boolean usingPrimaryEncoder = true;
    private boolean shooterMotorDisconnected;

    private double targetVelocity;
    private double distanceToGoal;
    private double realVelocity;
    private double currentDrawOne;
    private double currentDrawTwo;

    public Shooter(MotorEx motor1, MotorEx motor2, ServoEx servo, VoltageSensor voltageSensor,
                   Intake intake, RampSensors rampSensors) {
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.servo = servo;
        this.voltageSensor = voltageSensor;
        this.intake = intake;
        this.rampSensors = rampSensors;

        this.motor1.setRunMode(Motor.RunMode.RawPower);
        this.motor2.setRunMode(Motor.RunMode.RawPower);

        controller = new PIDFController(nectarKP, kI, kD, kF);
        controller.integrationControl.setIntegrationBounds(-windupRange, windupRange);

        NECTAR_VELOCITY_LOOKUP_TABLE.createLUT();
        POLLEN_VELOCITY_LOOKUP_TABLE.createLUT();
    }

    public void periodic() {
        if (size == Size.NECTAR) {
            servo.set(nectarServoPosition);
        } else if (size == Size.POLLEN) {
            servo.set(pollenServoPosition);
        }

        targetVelocity = requestedVelocity();
        updateMotorData();
        updateTolerance();

        if (!enabled) {
            setPower(0);
            controller.reset();
            return;
        }

        controller.setPIDF(size == Size.NECTAR ? nectarKP : pollenKP, kI, kD, kF);
        controller.integrationControl.setIntegrationBounds(-windupRange, windupRange);
        double outputVolts = controller.calculate(realVelocity, targetVelocity);
        setPower(outputVolts / voltageSensor.getVoltage());
    }

    private double requestedVelocity() {
        if (manual) {
            return manualVelocity;
        }

        if (size == Size.NECTAR) {
            return NECTAR_VELOCITY_LOOKUP_TABLE.get(distanceToGoal);
        } else if (size == Size.POLLEN) {
            return POLLEN_VELOCITY_LOOKUP_TABLE.get(distanceToGoal);
        }

        throw new IllegalStateException();
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

    public void setDistanceToGoal(double distanceToGoal) {
        this.distanceToGoal = distanceToGoal;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
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

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Command shootAllPollen() {
        return sequential(
                Commands.instant(intake::intakeFull),
                Commands.instant(this::enable),
                Commands.waitUntil(this::inTolerance),
                Commands.instant(intake::openGate),
                Commands.waitMs(shootAllPollenTime).requiring(this),
                Commands.instant(this::disable),
                Commands.instant(intake::stop),
                Commands.instant(() -> {
                    for (int i = 0; i < rampSensors.getLength(); i++) {
                        rampSensors.removeFromQueueShooting();
                    }
                })
        );
    }
}
