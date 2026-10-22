package org.firstinspires.ftc.teamcode.subsystems;

import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Display {
    private final List<ServoEx> lights;

    private List<ColorUtils.Color> colors = Collections.emptyList();

    public Display(List<ServoEx> lights) {
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
}
