package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
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

    public static double minEmptyVelocity = 900;
    public static double inEmptyTimeMs = 150;

    private final MotorGroup motors;
    private final ServoEx gate;
    private final BooleanDebouncer emptyDebouncer = new BooleanDebouncer();

    private double realVelocity;

    public Intake(MotorGroup motors, ServoEx gate) {
        this.motors = motors;
        this.gate = gate;
        motors.setRunMode(Motor.RunMode.RawPower);
    }

    public void setPower(double power) {
        motors.set(power);
    }

    public void setGatePosition(double position) {
        this.gate.set(position);
    }

    public Command in() {
        return Commands.instant(() -> setPower(inPower)).requiring(motors);
    }

    public Command out() {
        return Commands.instant(() -> setPower(outPower)).requiring(motors);
    }

    public Command store() {
        return Commands.instant(() -> setPower(storePower)).requiring(motors);
    }

    public Command openGate() {
        return Commands.instant(() -> setGatePosition(gateOpenPosition)).requiring(gate);
    }

    public Command closeGate() {
        return Commands.instant(() -> setGatePosition(gateClosedPosition)).requiring(gate);
    }

    public void periodic() {
        realVelocity = motors.getVelocity() / motors.getCPR() * 60;

        updateTolerance();
    }

    public void updateTolerance() {
        if (!isOut()) {
            return;
        }

        boolean currentlyEmpty = Math.abs(realVelocity) > minEmptyVelocity;

        emptyDebouncer.periodic(currentlyEmpty, inEmptyTimeMs, 0);
    }

    private double getRealVelocity() {
        return realVelocity;
    }

    public boolean isOut() {
        return motors.getRawPower() < 0;
    }

    public boolean isEmpty() {
        return emptyDebouncer.getState();
    }
}
