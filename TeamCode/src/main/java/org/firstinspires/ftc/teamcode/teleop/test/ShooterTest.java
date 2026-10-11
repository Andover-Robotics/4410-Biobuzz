package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Shooter;

@TeleOp(name = "Shooter Test", group = "Test")
@Config
public class ShooterTest extends OpMode {
    public static double STEP_RPM = 50;
    private Shooter outtake;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        VoltageSensor voltageSensor = hardwareMap.voltageSensor.iterator().next();
        outtake = new Shooter(
                new MotorEx(hardwareMap, "shooter1", Motor.GoBILDA.BARE),
                new MotorEx(hardwareMap, "shooter2", Motor.GoBILDA.BARE),
                new ServoEx(hardwareMap, "shooterServo"),
                voltageSensor);
        Shooter.manual = true;
        Shooter.manualVelocity = 0;
        telemetry.addData("Step RPM", STEP_RPM);
        telemetry.update();
    }

    @Override
    public void start() {
        outtake.enable();
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

        if (gamepad1.aWasPressed()) {
            outtake.setSize(outtake.getSize() == Shooter.Size.NECTAR
                    ? Shooter.Size.POLLEN : Shooter.Size.NECTAR);
        }

        outtake.periodic();

        telemetry.addData("Target RPM", outtake.getTargetVelocity());
        telemetry.addData("Actual RPM", outtake.getRealVelocity());
        telemetry.addData("At speed", outtake.inTolerance());
        telemetry.update();
    }

    @Override
    public void stop() {
        outtake.disable();
        outtake.periodic();
        Shooter.manual = false;
    }
}
