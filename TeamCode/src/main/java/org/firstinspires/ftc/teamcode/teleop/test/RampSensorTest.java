package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Display;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;
import org.firstinspires.ftc.teamcode.subsystems.sensors.Color;
import org.firstinspires.ftc.teamcode.util.ColorUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@TeleOp(name = "Ramp Sensor Test", group = "Test")
public class RampSensorTest extends OpMode {
    private static final class BallReading {
        private final BB.Ball size;
        private final ColorUtils.Color color;

        public BallReading(BB.Ball size, ColorUtils.Color color) {
            this.size = size;
            this.color = color;
        }

        public BB.Ball getSize() {
            return size;
        }

        public ColorUtils.Color getColor() {
            return color;
        }
    }

    private Intake intake;
    private Color bottomColor;
    private Display display;
    private Mecanum drive;
    private boolean displayDirty = true;

    private final Deque<BallReading> queue = new ArrayDeque<>();

    private static final int POLLEN_SIZE = 28;
    private static final int NECTAR_SIZE = 36;

    public static int maxPushLength = 100;

    private int length = 0;

    private void addToQueue(BB.Ball size, ColorUtils.Color color) {
        if (length >= maxPushLength) {
            return;
        }

        queue.push(new BallReading(size, color));
        displayDirty = true;

        if (size == BB.Ball.NECTAR) {
            length += NECTAR_SIZE;
        } else if (size == BB.Ball.POLLEN) {
            length += POLLEN_SIZE;
        }
    }

    private void removeFromQueue() {
        BallReading reading;

        if (!queue.isEmpty()) {
            reading = queue.pollFirst();
            displayDirty = true;
        } else {
            return;
        }

        if (reading.getSize() == BB.Ball.NECTAR) {
            length -= NECTAR_SIZE;
        } else if (reading.getSize() == BB.Ball.POLLEN) {
            length -= POLLEN_SIZE;
        }
    }

    @Override
    public void init() {
        queue.clear();
        length = 0;
        displayDirty = true;

        MotorEx motor1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx motor2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        motor2.setInverted(true);
        intake = new Intake(
                motor1, motor2,
                new ServoEx(hardwareMap, "gate"));
        intake.closeGate();

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());

        bottomColor = new Color(
                hardwareMap.get(RevColorSensorV3.class, "color1"),
                hardwareMap.get(DigitalChannel.class, "bottomBBBottom"),
                hardwareMap.get(DigitalChannel.class, "topBBBottom"),
                (size, color) -> {
                    if (intake.getState() == Intake.State.OUTTAKE) {
                        removeFromQueue();
                    } else if (intake.getState() == Intake.State.INTAKE
                            || intake.getState()  == Intake.State.INTAKE_FULL) {
                        addToQueue(size, color);
                    }
                });

        drive = new Mecanum(hardwareMap, Constants.driveConfig);
        drive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        List<ServoEx> servos = List.of(
                new ServoEx(hardwareMap, "display1"),
                new ServoEx(hardwareMap, "display2"),
                new ServoEx(hardwareMap, "display3"),
                new ServoEx(hardwareMap, "display4"));

        display = new Display(servos);
    }

    private String formatRampQueue() {
        StringBuilder displayString = new StringBuilder();
        for (BallReading reading : queue) {
            if (displayString.length() > 0) {
                displayString.append(" -> ");
            }
            displayString.append(reading.getSize()).append(" (")
                    .append(reading.getColor()).append(")");
        }
        return displayString.toString();
    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            intake.intakeFull();
        } else if (gamepad1.right_bumper) {
            intake.outtake();
        } else {
            intake.stop();
        }

        intake.periodic();
        bottomColor.periodic(intake.getState() != Intake.State.OUTTAKE);

        if (gamepad1.aWasPressed()) {
            intake.toggleGate();
        }

        if (displayDirty) {
            List<ColorUtils.Color> colors = new ArrayList<>();
            for (BallReading reading : queue) {
                colors.add(reading.getColor());
            }
            display.setColors(colors);
            display.periodic();
            displayDirty = false;
        }

        drive.applyDrive(new DrivePowers(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x));

        telemetry.addData("Ramp", formatRampQueue());
        telemetry.addData("Length", length);
        telemetry.update();
    }

    @Override
    public void stop() {

    }
}
