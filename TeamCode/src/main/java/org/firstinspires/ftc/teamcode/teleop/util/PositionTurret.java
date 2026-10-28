package org.firstinspires.ftc.teamcode.teleop.util;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Turret;

@TeleOp(name = "Position Turret", group = "Util")
@Config
public class PositionTurret extends OpMode {
    private Turret turret;

    @Override
    public void init() {
        Turret.manual = true;
        Turret.angles = new double[] {0, 1};
        Turret.servoPositions = new double[] {0, 1};
        turret = new Turret(
                null,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2"));
    }

    @Override
    public void loop() {
        if (gamepad1.dpadLeftWasPressed()) {
            Turret.manualPositionDegrees -= 0.01;
        } else if (gamepad1.dpadRightWasPressed()) {
            Turret.manualPositionDegrees += 0.01;
        }
        Turret.manualPositionDegrees = Math.max(0, Math.min(Turret.manualPositionDegrees, 1));
        turret.periodic();

        telemetry.addData("Position", Turret.manualPositionDegrees);
        telemetry.update();
    }

    @Override
    public void stop() {
        Turret.manual = false;
    }
}
