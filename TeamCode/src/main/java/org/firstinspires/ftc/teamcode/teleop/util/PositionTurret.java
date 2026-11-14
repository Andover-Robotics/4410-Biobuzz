package org.firstinspires.ftc.teamcode.teleop.util;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Turret;

@Utility(name = "Position Turret")
public class PositionTurret extends OpMode {
    private Turret turret;

    private boolean previousManual;
    private double previousManualPositionDegrees;
    private double[] previousAngles;
    private double[] previousServoPositions;

    @Override
    public void init() {
        previousManual = Turret.manual;
        previousManualPositionDegrees = Turret.manualPositionDegrees;
        previousAngles = Turret.angles;
        previousServoPositions = Turret.servoPositions;
        Turret.manual = true;
        Turret.manualPositionDegrees = 0;
        Turret.angles = new double[] {0, 1};
        Turret.servoPositions = new double[] {0, 1};
        turret = new Turret(
                telemetry,
                null,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));
    }

    @Override
    public void loop() {
        if (gamepad1.dpadDownWasPressed()) {
            Turret.manualPositionDegrees -= 0.005;
        } else if (gamepad1.dpadUpWasPressed()) {
            Turret.manualPositionDegrees += 0.005;
        }
        Turret.manualPositionDegrees = Math.max(0, Math.min(Turret.manualPositionDegrees, 1));
        turret.periodic();
        turret.updateTelemetry();

        telemetry.update();
    }

    @Override
    public void stop() {
        Turret.manual = previousManual;
        Turret.manualPositionDegrees = previousManualPositionDegrees;
        Turret.angles = previousAngles;
        Turret.servoPositions = previousServoPositions;
    }
}
