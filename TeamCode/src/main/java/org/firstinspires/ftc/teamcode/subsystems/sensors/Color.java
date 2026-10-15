package org.firstinspires.ftc.teamcode.subsystems.sensors;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.broadcom.BroadcomColorSensor;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.Optional;

@Config
public class Color {
    public static double rawOpticalTolerance = 100;

    private final RevColorSensorV3 colorSensor;
    private final BB breakBeam;
    private final OnEnter onEnter;

    private double rawOptical;
    private final float[] hsvValues = new float[3];
    private final float[] detectedHsvValues = new float[3];

    @FunctionalInterface
    public interface OnEnter {
        void onEnter(BB.Ball size, ColorUtils.Color color);
    }

    public Color(RevColorSensorV3 colorSensor, DigitalChannel bottomChannel,
                 DigitalChannel topChannel, OnEnter onEnter) {
        this.colorSensor = colorSensor;

        BroadcomColorSensor.Parameters params = colorSensor.getParameters().clone();
        params.lightSensorMeasRate = BroadcomColorSensor.LSMeasurementRate.R25ms;
        params.proximityMeasRate = BroadcomColorSensor.PSMeasurementRate.R12_5ms;
        colorSensor.initialize(params);

        this.onEnter = onEnter;
        this.breakBeam = new BB(bottomChannel, Optional.of(topChannel), this::onLeave);
        read();
    }

    private void read() {
        rawOptical = colorSensor.rawOptical();

        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        android.graphics.Color.colorToHSV(colors.toColor(), hsvValues);
    }

    private static ColorUtils.Color correctColor(BB.Ball size, ColorUtils.Color color) {
        switch (size) {
            case POLLEN:
                return ColorUtils.Color.POLLEN;
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
        ColorUtils.Color detectedColor = ColorUtils.Color.match(detectedHsvValues);
        ColorUtils.Color correctedColor = correctColor(size, detectedColor);
        onEnter.onEnter(size, correctedColor);
    }

    public void periodic() {
        read();
        breakBeam.periodic();

        if (isBroken()) {
            detectedHsvValues[0] = hsvValues[0];
            detectedHsvValues[1] = hsvValues[1];
            detectedHsvValues[2] = hsvValues[2];
        }
    }

    public ColorUtils.Color getColor() {
        return isBroken() ? ColorUtils.Color.match(hsvValues) : ColorUtils.Color.NONE;
    }

    public double getRawOptical() {
        return rawOptical;
    }

    public float[] getHsvValues() {
        return hsvValues;
    }

    public float[] getDetectedHsvValues() {
        return detectedHsvValues;
    }

    public boolean isBroken() {
        return breakBeam.getBottomState() && rawOptical > rawOpticalTolerance;
    }
}
