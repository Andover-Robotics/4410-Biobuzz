package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class Intake {
    public static double inPower = 1;
    public static double outPower = -0.5;
    public static double midInPower = 0.5;
    public static double storePower = 0.1;

    public static double gateOpenPosition = 0.59;
    public static double gateClosedPosition = 0.720;

    public static double wxyzUpPosition = 0.77;
    public static double wxyzDownPosition = 0.020;

    public static double inStallTimeMs = 200;

    private final Telemetry telemetry;
    private final MotorEx bottomMotor;
    private final MotorEx topMotor;
    private final ServoEx gate;
    private final ServoEx wxyz;
    private final RampSensors rampSensors;
    public final BooleanDebouncer topDebouncer = new BooleanDebouncer();
    public final BooleanDebouncer bottomDebouncer = new BooleanDebouncer();

    public enum State {
        INTAKE_FULL,
        INTAKE,
        STORE,
        OUTTAKE,
        STOP
    }

    private State state = State.STOP;

    public boolean gateClosed = true;
    public boolean wxyzUp = true;

    public Intake(Telemetry telemetry, MotorEx bottomMotor, MotorEx topMotor,
                  ServoEx gate, ServoEx wxyz, RampSensors rampSensors) {
        this.telemetry = telemetry;
        this.bottomMotor = bottomMotor;
        this.topMotor = topMotor;
        this.gate = gate;
        this.wxyz = wxyz;
        this.rampSensors = rampSensors;

        bottomMotor.setRunMode(Motor.RunMode.RawPower);
        topMotor.setRunMode(Motor.RunMode.RawPower);

        stop();
    }

    public void setPower(double power) {
        topMotor.set(power);
        bottomMotor.set(power);
    }

    public void setState(State state) {
        this.state = state;
    }

    public void intakeFull() {
        this.state = State.INTAKE_FULL;
    }

    public void intake() {
        this.state = State.INTAKE;
    }

    public void store() {
        this.state = State.STORE;
    }

    public void outtake() {
        this.state = State.OUTTAKE;
    }

    public void stop() {
        this.state = State.STOP;
    }

    public void setGatePosition(double position) {
        gateClosed = Math.abs(gateClosedPosition - position) < 0.05;
        gate.set(position);
    }

    public void setWxyzPosition(double position) {
        wxyzUp = Math.abs(wxyzUpPosition - position) < 0.05;

        wxyz.set(position);
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

    public void upWxyz() {
        setWxyzPosition(wxyzUpPosition);
    }

    public void downWxyz() {
        setWxyzPosition(wxyzDownPosition);
    }

    public void toggleGate() {
        if (gateClosed) {
            openGate();
        } else {
            closeGate();
        }
    }

    public void toggleWxyz() {
        if (wxyzUp) {
            downWxyz();
        } else {
            upWxyz();
        }
    }

    public void periodic() {
        boolean isTopFull = rampSensors.getPollenCount() == 2;
        boolean isTopMoreThanFull = rampSensors.getPollenCount() >= 2;
        boolean isBottomFull = rampSensors.getPollenCount() >= 4;

        switch (state) {
            case INTAKE_FULL:
                topMotor.set(inPower);
                bottomMotor.set(inPower);
                break;
            case INTAKE:
                topMotor.set(isTopFull ? midInPower :
                        isTopMoreThanFull ? storePower : inPower);
                bottomMotor.set(isBottomFull ? storePower : inPower);
                break;
            case OUTTAKE:
                topMotor.set(outPower);
                bottomMotor.set(outPower);
                break;
            case STORE:
                topMotor.set(storePower);
                bottomMotor.set(storePower);
                break;
            case STOP:
                topMotor.set(0);
                bottomMotor.set(0);
                break;
        }
    }

    public void updateTelemetry() {
        telemetry.addData("Intake/State", state);
        telemetry.addData("Intake/Gate", gateClosed ? "CLOSED" : "OPEN");
    }

    public State getState() {
        return state;
    }
}
