package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.motors.MotorGroup;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class Intake {
    public static double inPower = 1;
    public static double outPower = -0.5;
    public static double storePower = 0.5;

    public static double gateOpenPosition = 0.290;
    public static double gateClosedPosition = 0.480;

    public static double wxyzUpPosition = 0.000;
    public static double wxyzDownPosition = 0.000;

    private final Telemetry telemetry;
    private final MotorGroup motors;
    private final ServoEx gate;
    private final ServoEx wxyz;

    public boolean gateClosed = true;
    public boolean wxyzUp = true;

    public Intake(Telemetry telemetry, MotorEx motor1, MotorEx motor2,
                  ServoEx gate, ServoEx wxyz) {
        this.telemetry = telemetry;
        this.motors = new MotorGroup(motor1, motor2);
        this.gate = gate;
        this.wxyz = wxyz;

        motor1.setRunMode(Motor.RunMode.RawPower);
        motor2.setRunMode(Motor.RunMode.RawPower);

        stop();
    }

    public void setPower(double power) {
        motors.set(power);
    }

    public double getPower() {
        return motors.get();
    }

    public void intake() {
        setPower(inPower);
    }

    public void store() {
        setPower(storePower);
    }

    public void outtake() {
        setPower(outPower);
    }

    public void stop() {
        setPower(0);
    }

    public void setGatePosition(double position) {
        gateClosed = Math.abs(gateClosedPosition - position) < 0.02;

        gate.set(position);
    }

    public void setWxyzPosition(double position) {
        wxyzUp = Math.abs(wxyzUpPosition - position) < 0.02;

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

    public void updateTelemetry() {
        telemetry.addData("Intake/Power", getPower());
        telemetry.addData("Intake/Gate", gateClosed ? "CLOSED" : "OPEN");
    }
}
