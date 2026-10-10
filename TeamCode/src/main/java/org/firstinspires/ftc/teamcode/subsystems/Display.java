package org.firstinspires.ftc.teamcode.subsystems;

import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.List;

public class Display {
    private final List<ServoEx> lights;

    private List<ColorUtils.Color> colors;

    public Display(List<ServoEx> lights) {
        this.lights = lights;
    }

    public void setColors(List<ColorUtils.Color> colors) {
        this.colors = colors;

        if (colors.size() != lights.size()) {
            throw new IllegalStateException();
        }
    }

    public void periodic() {
        for (int i = 0; i < lights.size(); i++) {
            lights.get(i).set(colors.get(i).getFloodlightColor());
        }
    }
}
