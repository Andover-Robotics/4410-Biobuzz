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

import org.firstinspires.ftc.robotcore.external.Telemetry;
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
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        color = new Color(
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                this::onEnter);

        MotorEx intake1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx intake2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        intake2.setInverted(true);
        intake = new Intake(
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

        float[] hsvValues = color.getHsvValues();
        float[] detectedHsvValues = color.getDetectedHsvValues();

        int colorValue = android.graphics.Color.HSVToColor(hsvValues) & 0xFFFFFF;

        telemetry.addData("HSV",
                "<font color='#%06X'>%.1f %.3f %.3f</font>",
                colorValue, hsvValues[0], hsvValues[1], hsvValues[2]);
        telemetry.addData("H", hsvValues[0]);
        telemetry.addData("S", hsvValues[1]);
        telemetry.addData("V", hsvValues[2]);
        telemetry.addData("Detected HSV", "%.1f %.3f %.3f",
                detectedHsvValues[0], detectedHsvValues[1], detectedHsvValues[2]);
        telemetry.addData("Color match", ColorUtils.Color.match(hsvValues));
        telemetry.addData("Current color match", color.getColor());
        telemetry.addData("Raw optical", color.getRawOptical());
        telemetry.addData("Color broken", color.isBroken());
        telemetry.addData("Objects passed", objectsPassed);
        telemetry.addData("Last entry", "%s %s", lastEnteredSize, lastEnteredColor);
        telemetry.update();
    }
}
