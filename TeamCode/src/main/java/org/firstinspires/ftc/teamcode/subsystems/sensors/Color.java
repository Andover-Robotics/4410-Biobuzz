package org.firstinspires.ftc.teamcode.subsystems.sensors;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.Optional;

public class Color {
    private final RevColorSensorV3 colorSensor;
    private final BB breakBeam;

    private final OnEnter onEnter;

    private final float[] hsvValues = new float[3];
    private boolean hasSample;

    @FunctionalInterface
    public interface OnEnter {
        void onEnter(BB.Ball size, ColorUtils.Color color);
    }

    public Color(RevColorSensorV3 colorSensor, DigitalChannel bottomChannel,
                 DigitalChannel topChannel, OnEnter onEnter) {
        this.colorSensor = colorSensor;
        this.onEnter = onEnter;
        this.breakBeam = new BB(bottomChannel, Optional.of(topChannel), this::reportBall);
    }

    private void readColor() {
        android.graphics.Color.RGBToHSV(
                (int) (colorSensor.red() * 255),
                (int) (colorSensor.green() * 255),
                (int) (colorSensor.blue() * 255),
                hsvValues
        );
    }

    private ColorUtils.Color colorForSize(BB.Ball size) {
        if (!hasSample) {
            return ColorUtils.Color.NONE;
        }
        switch (size) {
            case POLLEN:
                return ColorUtils.Color.POLLEN.compareHSV(hsvValues)
                        ? ColorUtils.Color.POLLEN : ColorUtils.Color.NONE;
            case NECTAR:
                if (ColorUtils.Color.NECTAR_RED.compareHSV(hsvValues)) {
                    return ColorUtils.Color.NECTAR_RED;
                }
                return ColorUtils.Color.NECTAR_BLUE.compareHSV(hsvValues)
                        ? ColorUtils.Color.NECTAR_BLUE : ColorUtils.Color.NONE;
            case BALL:
                return ColorUtils.Color.match(hsvValues);
            default:
                return ColorUtils.Color.NONE;
        }
    }

    private void reportBall(BB.Ball size) {
        ColorUtils.Color detectedColor = colorForSize(size);
        hasSample = false;
        if (size != BB.Ball.NONE) {
            onEnter.onEnter(size, detectedColor);
        }
    }

    public void periodic() {
        breakBeam.periodic();
        if (breakBeam.shouldUpdateColor()) {
            readColor();
            hasSample = true;
        }
    }

    public ColorUtils.Color getColor() {
        return breakBeam.shouldUpdateColor()
                ? ColorUtils.Color.match(hsvValues) : ColorUtils.Color.NONE;
    }

    public float[] getHsvValues() {
        return hsvValues;
    }

    public boolean isBroken() {
        return breakBeam.isBroken();
    }
}
