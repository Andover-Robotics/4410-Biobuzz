package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

@Config
public class Display {
    private final Telemetry telemetry;
    private final List<ServoEx> lights;
    private final Turret turret;
    private final RampSensors rampSensors;

    private List<ColorUtils.Color> colors = new ArrayList<>();

    public Display(Telemetry telemetry, List<ServoEx> lights,
                   Turret turret, RampSensors rampSensors) {
        this.telemetry = telemetry;
        this.lights = lights;
        this.turret = turret;
        this.rampSensors = rampSensors;
    }

    public void setColors(List<ColorUtils.Color> colors) {
        this.colors = new ArrayList<>(colors);
    }

    public void periodic() {
        colors.clear();

        for (int i = 0; i < rampSensors.getLength(); i++) {
            ColorUtils.Color color = ColorUtils.Color.NONE;

            if (rampSensors.hasPollen(i)) {
                color = turret.isOutOfRange() ? ColorUtils.Color.RED : ColorUtils.Color.YELLOW;
            }

            lights.get(i).set(color.getFloodlightColor());
        }
    }

    public void updateTelemetry() {}
}
