package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class Intake {
    public static double inPower = 1;
    public static double outPower = -0.5;
    public static double stallPower = 0.3;

    public static double gateOpenPosition = 0.290;
    public static double gateClosedPosition = 0.480;

    public static double inStallTimeMs = 250;

    public static double lowLowCurrent = 2300;
    public static double lowHighCurrent = 2500;
    public static double highLowCurrent = 3300;
    public static double highHighCurrent = 10000;

    private final MotorEx topMotor;
    private final MotorEx bottomMotor;
    private final ServoEx gate;
    private final BooleanDebouncer topStalledDebouncer = new BooleanDebouncer();
    private final BooleanDebouncer bottomStalledDebouncer = new BooleanDebouncer();

    public enum State {
        INTAKE_FULL,
        INTAKE,
        STORE,
        OUTTAKE,
        STOP
    }

    private State state;
    private State previousState;

    private double topCurrent;
    private double bottomCurrent;

    public boolean gateClosed = true;

    public Intake(MotorEx topMotor, MotorEx bottomMotor, ServoEx gate) {
        this.topMotor = topMotor;
        this.bottomMotor = bottomMotor;
        this.gate = gate;

        topMotor.setRunMode(Motor.RunMode.RawPower);
        bottomMotor.setRunMode(Motor.RunMode.RawPower);

        setState(State.STORE);
    }

    public void setState(State state) {
        this.state = state;
    }

    public void intakeFull() {
        setState(State.INTAKE_FULL);
    }

    public void intake() {
        setState(State.INTAKE);
    }

    public void store() {
        setState(State.STORE);
    }

    public void outtake() {
        setState(State.OUTTAKE);
    }

    public void stop() {
        setState(State.STOP);
    }

    public void setGatePosition(double position) {
        gateClosed = Math.abs(gateClosedPosition - position) < 0.02;

        gate.set(position);
    }

    public boolean isGateClosed() {
        return gateClosed;
    }

    public void openGate() {
        setGatePosition(gateOpenPosition);
    }

    public void closeGate() {
        setGatePosition(gateClosedPosition);
    }

    public void toggleGate() {
        if (gateClosed) {
            openGate();
        } else {
            closeGate();
        }
    }

    public void periodic() {
        updateMotorData();
        updateTolerance();
        updateMotors();

        previousState = state;
    }

    private void updateMotorData() {
        topCurrent = topMotor.getCurrent(CurrentUnit.MILLIAMPS);
        bottomCurrent = bottomMotor.getCurrent(CurrentUnit.MILLIAMPS);
    }

    public static boolean isCurrentlyStalled(double current) {
        return (current > lowLowCurrent && current < lowHighCurrent)
                || (current > highLowCurrent && current < highHighCurrent);
    }

    private void updateTolerance() {
        if (state != previousState) {
            topStalledDebouncer.periodic(false, inStallTimeMs, 0);
            bottomStalledDebouncer.periodic(false, inStallTimeMs, 0);
        }

        if (state == State.INTAKE || state == State.INTAKE_FULL) {
            boolean topCurrentlyStalled = topStalledDebouncer.getState() || isCurrentlyStalled(topCurrent);
            boolean bottomCurrentlyStalled = bottomStalledDebouncer.getState() || isCurrentlyStalled(bottomCurrent);

            topStalledDebouncer.periodic(topCurrentlyStalled, inStallTimeMs, 0);
            bottomStalledDebouncer.periodic(bottomCurrentlyStalled, inStallTimeMs, 0);
        }
    }

    private void updateMotors() {
        switch (state) {
            case INTAKE_FULL:
                topMotor.set(inPower);
                bottomMotor.set(inPower);
                break;
            case INTAKE:
                topMotor.set(topStalledDebouncer.getState() ? stallPower : inPower);
                bottomMotor.set(bottomStalledDebouncer.getState() ? stallPower : inPower);
                break;
            case OUTTAKE:
                topMotor.set(outPower);
                bottomMotor.set(outPower);
                break;
            case STORE:
                topMotor.set(stallPower);
                bottomMotor.set(stallPower);
                break;
            case STOP:
                topMotor.set(0);
                bottomMotor.set(0);
                break;
        }
    }

    public boolean getTopStalled() {
        return topStalledDebouncer.getState();
    }

    public boolean getBottomStalled() {
        return bottomStalledDebouncer.getState();
    }

    public double getTopCurrent() {
        return topCurrent;
    }

    public double getBottomCurrent() {
        return bottomCurrent;
    }

    public State getState() {
        return state;
    }
}
