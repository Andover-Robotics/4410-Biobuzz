package org.firstinspires.ftc.teamcode.teleop.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Display;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@TeleOp(name = "Display Test", group = "Test")
public class DisplayTest extends LinearOpMode {
    private Display display;

    public ColorUtils.Color randomColor() {
        ColorUtils.Color[] colors = ColorUtils.Color.values();

        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    public List<ColorUtils.Color> randomColors(int n) {
        List<ColorUtils.Color> colors = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            colors.add(randomColor());
        }

        return colors;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        display = new Display(telemetry, List.of(
                new ServoEx(hardwareMap, "display1"),
                new ServoEx(hardwareMap, "display2"),
                new ServoEx(hardwareMap, "display3"),
                new ServoEx(hardwareMap, "display4")));

        waitForStart();

        while (opModeIsActive() && !isStopRequested()) {
            display.setColors(randomColors(4));
            display.periodic();
            display.updateTelemetry();
            telemetry.update();

            sleep(1000);
        }
    }
}
