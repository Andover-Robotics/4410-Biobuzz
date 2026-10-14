package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Turret;

@TeleOp(name = "Turret Test", group = "Test")
@Config
public class TurretTest extends OpMode {
    private Turret turret;

    @Override
    public void init(){
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
        Turret.manualPositionDegrees = gamepad1.left_stick_x * 0.5 + 0.5;
        turret.periodic();
    }

    @Override
    public void stop() {
        Turret.manual = false;
    }
}
