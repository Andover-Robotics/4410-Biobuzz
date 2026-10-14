package org.firstinspires.ftc.teamcode.subsystems.sensors;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.Optional;

public class Color {
    public static double distanceToleranceMm = 10;

    private final RevColorSensorV3 colorSensor;
    private final BB breakBeam;

    private final OnEnter onEnter;

    private double distance;
    private final float[] rgbValues = new float[3];

    @FunctionalInterface
    public interface OnEnter {
        void onEnter(BB.Ball size, ColorUtils.Color color);
    }

    public Color(RevColorSensorV3 colorSensor, DigitalChannel bottomChannel,
                 DigitalChannel topChannel, OnEnter onEnter) {
        this.colorSensor = colorSensor;
        this.onEnter = onEnter;
        this.breakBeam = new BB(bottomChannel, Optional.of(topChannel), this::onLeave);
        read();
    }

    private void read() {
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        float maxChannel = Math.max(colors.red, Math.max(colors.green, colors.blue));
        if (maxChannel <= 0) {
            rgbValues[0] = 0;
            rgbValues[1] = 0;
            rgbValues[2] = 0;
            return;
        }
        rgbValues[0] = colors.red / maxChannel * 255;
        rgbValues[1] = colors.green / maxChannel * 255;
        rgbValues[2] = colors.blue / maxChannel * 255;

        distance = colorSensor.getDistance(DistanceUnit.MM);
    }

    private static ColorUtils.Color correctColor(BB.Ball size, ColorUtils.Color color) {
        switch (size) {
            case POLLEN:
                return color == ColorUtils.Color.POLLEN
                        ? color : ColorUtils.Color.NONE;
            case NECTAR:
                return color == ColorUtils.Color.NECTAR_RED || color == ColorUtils.Color.NECTAR_BLUE
                        ? color : ColorUtils.Color.NONE;
            case BALL:
                return color;
            default:
                return ColorUtils.Color.NONE;
        }
    }

    private void onLeave(BB.Ball size) {
        ColorUtils.Color color = getColor();
        ColorUtils.Color correctedColor = correctColor(size, color);
        onEnter.onEnter(size, correctedColor);
    }

    public void periodic() {
        breakBeam.periodic();
        read();
    }

    public ColorUtils.Color getColor() {
        return isBroken() ? ColorUtils.Color.match(rgbValues) : ColorUtils.Color.NONE;
    }

    public double getDistance() {
        return distance;
    }

    public float[] getRgbValues() {
        return rgbValues;
    }

    public boolean isBroken() {
        return breakBeam.getBottomState() && distance < distanceToleranceMm;
    }
}
