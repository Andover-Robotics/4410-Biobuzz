package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.motors.MotorGroup;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class Intake {
    public static double inPower = 1.0;
    public static double outPower = -1.0;
    public static double storePower = 1.0;

    public static double gateOpenPosition = 0.290;
    public static double gateClosedPosition = 0.440;
    public static double flowerUpPosition = 0;
    public static double flowerDownPosition = 0;

    public static double minEmptyVelocity = 900;
    public static double inEmptyTimeMs = 150;

    private final MotorEx motor1;
    private final MotorEx motor2;
    private final ServoEx gate;
    private final ServoEx flower;
    private final BooleanDebouncer emptyDebouncer = new BooleanDebouncer();

    private double realBottomVelocity, realTopVelocity;

    public Intake(MotorEx motor1, MotorEx motor2, ServoEx gate, ServoEx flower) {
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.gate = gate;
        this.flower = flower;
        motor1.setRunMode(Motor.RunMode.RawPower);
        motor2.setRunMode(Motor.RunMode.RawPower);
    }

    public void setPower(double power) {
        motor1.set(power);
        motor2.set(power);
    }

    public void setGatePosition(double position) {
        this.gate.set(position);
    }
    public void setFlowerPosition(double position) {
        this.flower.set(position);
    }

    public Command in() {
        return Commands.instant(() -> setPower(inPower)).requiring(motor1,motor2);
    }

    public Command out() {
        return Commands.instant(() -> setPower(outPower)).requiring(motor1,motor2);
    }

    public Command store() {
        return Commands.instant(() -> setPower(storePower)).requiring(motor1,motor2);
    }

    public Command openGate() {
        return Commands.instant(() -> setGatePosition(gateOpenPosition)).requiring(gate);
    }

    public Command closeGate() {
        return Commands.instant(() -> setGatePosition(gateClosedPosition)).requiring(gate);
    }

    public Command flowerUp() {
        return Commands.instant(() -> setFlowerPosition(flowerUpPosition)).requiring(flower);
    }
    public Command flowerDown() {
        return Commands.instant(() -> setFlowerPosition(flowerDownPosition)).requiring(flower);
    }

    public void periodic() {
        realTopVelocity = motor1.getVelocity() / motor1.getCPR() * 60;
        realBottomVelocity  = motor2.getVelocity() / motor2.getCPR() * 60;
        updateTolerance();
    }

    public void updateTolerance() {
        if (!isIn() && !isOut()) {
            return;
        }

        boolean currentlyEmpty = Math.abs(realTopVelocity) > minEmptyVelocity;

        emptyDebouncer.periodic(currentlyEmpty, inEmptyTimeMs, 0);
    }

    private double getRealBottomVelocity() {
        return realBottomVelocity;
    }

    private double getRealTopVelocity() {
        return realTopVelocity;
    }

    public boolean isIn() {
        return motor1.getRawPower() > 0;
    }

    public boolean isOut() {
        return motor1.getRawPower() < 0;
    }

    public boolean isEmpty() {
        return emptyDebouncer.getState();
    }
}
