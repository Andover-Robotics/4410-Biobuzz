package org.firstinspires.ftc.teamcode.subsystems;

import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Display {
    private final Telemetry telemetry;
    private final List<ServoEx> lights;

    private List<ColorUtils.Color> colors = Collections.emptyList();

    public Display(Telemetry telemetry, List<ServoEx> lights) {
        this.telemetry = telemetry;
        this.lights = lights;
    }

    public void setColors(List<ColorUtils.Color> colors) {
        this.colors = new ArrayList<>(colors);
    }

    public void periodic() {
        for (int i = 0; i < lights.size(); i++) {
            ColorUtils.Color color = i < colors.size() ? colors.get(i) : ColorUtils.Color.NONE;
            lights.get(i).set(color.getFloodlightColor());
        }
    }

    public void updateTelemetry() {
        telemetry.addData("Display/Colors", colors.isEmpty() ? "NONE" : colors);
    }
}
