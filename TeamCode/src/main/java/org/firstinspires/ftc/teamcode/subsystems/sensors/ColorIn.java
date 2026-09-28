package org.firstinspires.ftc.teamcode.subsystems.sensors;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.ColorSensor;

import org.firstinspires.ftc.teamcode.util.ColorUtils;

public class ColorIn {
    private final ColorSensor colorSensor;

    private final OnEnter onEnter;

    private ColorUtils.Color color;
    private boolean previousBallPresent;

    @FunctionalInterface
    public interface OnEnter {
        void onEnter(ColorUtils.Color color);
    }

    public ColorIn(ColorSensor colorSensor, String colorSensorName, OnEnter onEnter) {
        this.colorSensor = colorSensor;
        this.onEnter = onEnter;
    }

    private ColorUtils.Color readHSV() {
        float[] hsvValues = new float[3];

        Color.RGBToHSV(
                (int) (colorSensor.red() * 255),
                (int) (colorSensor.green() * 255),
                (int) (colorSensor.blue() * 255),
                hsvValues
        );

        return ColorUtils.Color.match(hsvValues);
    }

    public void periodic() {
        color = readHSV();
        boolean ballPresent = color != ColorUtils.Color.NONE;

        if (ballPresent && !previousBallPresent) {
            onEnter.onEnter(color);
        }

        previousBallPresent = ballPresent;
    }

    public ColorUtils.Color getColor() {
        return color;
    }
}
