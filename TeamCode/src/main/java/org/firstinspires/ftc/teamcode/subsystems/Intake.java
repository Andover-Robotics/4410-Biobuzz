package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

import java.util.Optional;

@Config
public class Intake {
    public static double inPower = 1;
    public static double outPower = -0.5;
    public static double stallPower = 0.3;

    public static double gateOpenPosition = 0.290;
    public static double gateClosedPosition = 0.480;

    private final Telemetry telemetry;
    private final MotorEx topMotor;
    private final MotorEx bottomMotor;
    private final ServoEx gate;

    public enum State {
        INTAKE_FULL,
        INTAKE,
        STORE,
        OUTTAKE,
        STOP
    }

    private Optional<RampSensors> rampSensors = Optional.empty();

    private State state;

    public boolean gateClosed = true;

    public static double inStallTimeMs = 200;
    public final BooleanDebouncer topDebouncer = new BooleanDebouncer();
    public final BooleanDebouncer bottomDebouncer = new BooleanDebouncer();

    public Intake(Telemetry telemetry, MotorEx topMotor, MotorEx bottomMotor, ServoEx gate) {
        this.telemetry = telemetry;
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
        topDebouncer.periodic(isTopFull(), inStallTimeMs, 0);
        bottomDebouncer.periodic(isBottomFull(), inStallTimeMs, 0);

        switch (state) {
            case INTAKE_FULL:
                topMotor.set(inPower);
                bottomMotor.set(inPower);
                break;
            case INTAKE:
                topMotor.set(isTopStalled() ? stallPower : inPower);
                bottomMotor.set(isBottomStalled() ? stallPower : inPower);
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

    public void updateTelemetry() {
        telemetry.addData("Intake/State", state);
        telemetry.addData("Intake/Gate", gateClosed ? "CLOSED" : "OPEN");
        telemetry.addData("Intake/Top stalled", isTopStalled());
        telemetry.addData("Intake/Bottom stalled", isBottomStalled());
    }

    public void setRampSensors(RampSensors rampSensors) {
        this.rampSensors = Optional.of(rampSensors);
    }

    public State getState() {
        return state;
    }

    private boolean isTopFull() {
        return rampSensors.map((rampSensors) -> rampSensors.getQueueSize() >= 2)
                .orElse(false);
    }

    private boolean isBottomFull() {
        return rampSensors.map((rampSensors) -> rampSensors.getQueueSize() == 4)
                .orElse(false);
    }

    public boolean isTopStalled() {
        return topDebouncer.getState();
    }

    public boolean isBottomStalled() {
        return bottomDebouncer.getState();
    }
}
