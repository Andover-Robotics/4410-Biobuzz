package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class RampSensors {
    public static double blockedDebounceMs = 10;
    public static double clearDebounceMs = 50;

    private final Telemetry telemetry;
    private final BreakBeam[] breakBeams;

    public RampSensors(Telemetry telemetry, DigitalChannel channel1, DigitalChannel channel2,
                       DigitalChannel channel3, DigitalChannel channel4) {
        this.telemetry = telemetry;
        breakBeams = new BreakBeam[] {
//                new BreakBeam(channel1),
                new BreakBeam(channel2),
                new BreakBeam(channel3),
                new BreakBeam(channel4)
        };
    }


    public void periodic() {
        for (BreakBeam breakBeam : breakBeams) {
            breakBeam.periodic();
        }
    }

    public boolean hasPollen(int position) {
        return breakBeams[position].hasPollen();
    }

    public int getPollenCount() {
        int count = 0;
        for (BreakBeam breakBeam : breakBeams) {
            if (breakBeam.hasPollen()) {
                count++;
            } else {
                break;
            }
        }
        return count == 0 ? 0 : (count + 1);
    }

    public int getLength() {
        return 4;
    }

    public void updateTelemetry() {
        for (int i = 0; i < breakBeams.length; i++) {
            telemetry.addData("Ramp/Pollen position " + i, hasPollen(i));
        }
        telemetry.addData("Ramp/Pollen count", getPollenCount());
    }

    private static final class BreakBeam {
        private final DigitalChannel channel;
        private final BooleanDebouncer debouncer = new BooleanDebouncer();

        private BreakBeam(DigitalChannel channel) {
            this.channel = channel;
            channel.setMode(DigitalChannel.Mode.INPUT);
        }

        private void periodic() {
            debouncer.periodic(!channel.getState(), blockedDebounceMs, clearDebounceMs);
        }

        private boolean hasPollen() {
            return debouncer.getState();
        }
    }
}
