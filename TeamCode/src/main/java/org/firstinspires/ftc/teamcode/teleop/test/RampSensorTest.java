package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.subsystems.sensors.BBOut;
import org.firstinspires.ftc.teamcode.subsystems.sensors.ColorIn;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayDeque;
import java.util.Deque;

@TeleOp(name = "Ramp Sensor Test", group = "Test")
public class RampSensorTest extends OpMode {
    private ColorIn colorIn;
    private BBOut bbOut;

    private final Deque<ColorUtils.Color> queue = new ArrayDeque<>();

    @Override
    public void init() {
        queue.clear();

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        bbOut = new BBOut(
                hardwareMap.get(DigitalChannel.class, "bb"),
                queue::pollFirst);
        colorIn = new ColorIn(
                hardwareMap.get(ColorSensor.class, "color"),
                "color",
                queue::addLast);
    }

    @Override
    public void start() {

    }

    private String formatRampQueue() {
        StringBuilder display = new StringBuilder();
        for (ColorUtils.Color color : queue) {
            switch (color) {
                case POLLEN:
                    display.append("🟡");
                    break;
                case NECTAR_RED:
                    display.append("🔴");
                    break;
                case NECTAR_BLUE:
                    display.append("🔵");
                    break;
                default:
                    display.append("⚪");
                    break;
            }
        }
        return display.toString();
    }

    @Override
    public void loop() {
        colorIn.periodic();
        bbOut.periodic();

        telemetry.addData("Ramp", formatRampQueue());
        telemetry.update();
    }

    @Override
    public void stop() {

    }
}
