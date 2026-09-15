package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@Configurable
public class Intake {
    public static double IN_POWER = 1.0;
    public static double OUT_POWER = -1.0;
    public static double STORE_POWER = 1.0;

    private final DcMotorEx motor;

    public Intake(Bot bot, String name) {
        motor = bot.hardwareMap.get(DcMotorEx.class, name);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public void setPower(double power) { motor.setPower(power); }

    public Command in() { return Commands.instant(() -> setPower(IN_POWER)).requiring(motor); }
    public Command out() { return Commands.instant(() -> setPower(OUT_POWER)).requiring(motor); }
    public Command store() { return Commands.instant(() -> setPower(STORE_POWER)).requiring(motor); }
}
