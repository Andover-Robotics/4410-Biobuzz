package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

@TeleOp(name = "Color Test", group = "Test")
public class ColorTest extends OpMode {
    private Color color;
    private int objectsPassed = 0;
    private BB.Ball lastEnteredSize = BB.Ball.NONE;
    private ColorUtils.Color lastEnteredColor = ColorUtils.Color.NONE;

    private Intake intake;

    public void onEnter(BB.Ball size, ColorUtils.Color color) {
        lastEnteredSize = size;
        lastEnteredColor = color;
        objectsPassed++;
    }

    @Override
    public void init() {
        objectsPassed = 0;
        lastEnteredSize = BB.Ball.NONE;
        lastEnteredColor = ColorUtils.Color.NONE;
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        color = new Color(
                telemetry,
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                this::onEnter);

        MotorEx intake1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx intake2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        intake2.setInverted(true);
        intake = new Intake(
                telemetry,
                intake1,
                intake2,
                new ServoEx(hardwareMap, "gate"));
    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            intake.intakeFull();
        } else if (gamepad1.right_bumper) {
            intake.outtake();
        } else {
            intake.stop();
        }

        color.periodic();
        intake.periodic();

        color.updateTelemetry();
        intake.updateTelemetry();
        telemetry.addData("Color test/Balls passed", objectsPassed);
        telemetry.addData("Color test/Last entry", "%s %s", lastEnteredSize, lastEnteredColor);
        telemetry.update();
    }
}
