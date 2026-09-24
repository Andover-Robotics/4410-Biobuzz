package org.firstinspires.ftc.teamcode.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.subsystems.Shooter;

@TeleOp(name = "Shooter Test", group = "Test")
public class ShooterTest extends OpMode {
    private static final double STEP_RPM = 50;
    private Shooter shooter;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        VoltageSensor voltageSensor = hardwareMap.voltageSensor.iterator().next();
        shooter = new Shooter(
                new MotorEx(hardwareMap, "outtake1"),
                new MotorEx(hardwareMap, "outtake2"),
                voltageSensor);
        Shooter.manual = true;
        Shooter.manualVelocity = 0;
        telemetry.addLine("D-pad up/down: +/-50 RPM");
        telemetry.update();
    }

    @Override
    public void start() {
        shooter.enable();
    }

    @Override
    public void loop() {
        if (gamepad1.dpadUpWasPressed()) {
            Shooter.manualVelocity += STEP_RPM;
        }
        if (gamepad1.dpadDownWasPressed()) {
            Shooter.manualVelocity -= STEP_RPM;
        }
        Shooter.manualVelocity = Math.max(0, Shooter.manualVelocity);

        shooter.periodic();

        telemetry.addData("Target RPM", shooter.getTargetVelocity());
        telemetry.addData("Actual RPM", shooter.getRealVelocity());
        telemetry.addData("At speed", shooter.inTolerance());
        telemetry.update();
    }

    @Override
    public void stop() {
        shooter.disable();
        shooter.periodic();
        Shooter.manual = false;
    }
}
