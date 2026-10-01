package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.Optional;

@TeleOp(name = "Color Test", group = "Test")
public class ColorTest extends OpMode {
    private Color color;
    private BB.Ball lastEnteredSize = BB.Ball.NONE;
    private ColorUtils.Color lastEnteredColor = ColorUtils.Color.NONE;

    @Override
    public void init() {
        lastEnteredSize = BB.Ball.NONE;
        lastEnteredColor = ColorUtils.Color.NONE;
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        color = new Color(
                hardwareMap.get(ColorSensor.class, "color"),
                hardwareMap.get(DigitalChannel.class, "bottomBB"),
                Optional.of(hardwareMap.get(DigitalChannel.class, "topBB")),
                (size, detectedColor) -> {
                    lastEnteredSize = size;
                    lastEnteredColor = detectedColor;
                });
    }

    @Override
    public void loop() {
        color.periodic();

        float[] hsvValues = color.getHsvValues();

        telemetry.addData("HSV", "%.1f, %.3f, %.3f", hsvValues[0], hsvValues[1], hsvValues[2]);
        telemetry.addData("Raw color match", ColorUtils.Color.match(hsvValues));
        telemetry.addData("Beam broken (filtered)", color.isBroken());
        telemetry.addData("Color match (current)", color.getColor());
        telemetry.addData("Last entry", "%s (%s)", lastEnteredSize, lastEnteredColor);
        telemetry.update();
    }
}
