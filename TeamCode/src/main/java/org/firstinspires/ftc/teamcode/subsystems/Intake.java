package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@Config
public class Intake {
    public static double inPower = 1.0;
    public static double outPower = -1.0;
    public static double storePower = 1.0;

    private final MotorEx motor;

    public Intake(MotorEx motor) {
        this.motor = motor;
        motor.setRunMode(Motor.RunMode.RawPower);
    }

    public void setPower(double power) { motor.set(power); }

    public Command in() { return Commands.instant(() -> setPower(inPower)).requiring(motor); }
    public Command out() { return Commands.instant(() -> setPower(outPower)).requiring(motor); }
    public Command store() { return Commands.instant(() -> setPower(storePower)).requiring(motor); }
}
