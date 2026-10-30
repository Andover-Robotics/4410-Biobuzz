package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Config
public class Display {
    public static double FLASH_INTERVAL_MS = 500;
    public static boolean flash = true;

    private final Telemetry telemetry;
    private final List<ServoEx> lights;
    private final Optional<Turret> turret;
    private final Optional<RampSensors> rampSensors;
    private final ElapsedTime flashTimer = new ElapsedTime();

    private List<ColorUtils.Color> colors = new ArrayList<>();
    private boolean shouldFlash;

    public Display(Telemetry telemetry, List<ServoEx> lights) {
        this(telemetry, lights, Optional.empty(), Optional.empty());
    }

    public Display(Telemetry telemetry, List<ServoEx> lights,
                   Optional<Turret> turret, Optional<RampSensors> rampSensors) {
        this.telemetry = telemetry;
        this.lights = lights;
        this.turret = turret;
        this.rampSensors = rampSensors;
    }

    public void setColors(List<ColorUtils.Color> colors) {
        this.colors = new ArrayList<>(colors);
    }

    public void periodic() {
        rampSensors.ifPresent(sensors -> {
            colors.clear();
            for (RampSensors.BallReading reading : sensors.getQueue()) {
                colors.add(reading.getColor());
            }
        });

        boolean outOfRange = turret.map(Turret::isOutOfRange).orElse(false);
        boolean requestedShouldFlash = outOfRange && flash;
        if (requestedShouldFlash != shouldFlash) {
            shouldFlash = requestedShouldFlash;
            flashTimer.reset();
        }
        boolean inFlashFrame = shouldFlash && (long) (flashTimer.milliseconds() / FLASH_INTERVAL_MS) % 2 == 0;

        for (int i = 0; i < lights.size(); i++) {
            ColorUtils.Color color = i < colors.size() ? colors.get(i) : ColorUtils.Color.NONE;

            double position = inFlashFrame
                    ? ColorUtils.Color.PURPLE.getFloodlightColor() : color.getFloodlightColor();

            lights.get(i).set(position);
        }
    }

    public void updateTelemetry() {
        telemetry.addData("Display/Colors", colors.isEmpty() ? "NONE" : colors);
        telemetry.addData("Display/Flash", flash);
    }
}
