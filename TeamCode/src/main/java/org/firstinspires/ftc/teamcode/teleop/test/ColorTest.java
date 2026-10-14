package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

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
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        color = new Color(
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                (size, detectedColor) -> {
                    lastEnteredSize = size;
                    lastEnteredColor = detectedColor;
                });
    }

    @Override
    public void loop() {
        color.periodic();

        float[] rgbValues = color.getRgbValues();
        int r = Math.round(rgbValues[0]);
        int g = Math.round(rgbValues[1]);
        int b = Math.round(rgbValues[2]);

        telemetry.addData("RGB",
                "<font color='#%02X%02X%02X'>%d %d %d</font>",
                r, g, b, r, g, b);
        telemetry.addData("Distance", color.getDistance());
        telemetry.addData("Color match", ColorUtils.Color.match(rgbValues));
        telemetry.addData("Color broken", color.isBroken());
        telemetry.addData("Current color match", color.getColor());
        telemetry.addData("Last entry", "%s %s", lastEnteredSize, lastEnteredColor);
        telemetry.update();
    }
}
