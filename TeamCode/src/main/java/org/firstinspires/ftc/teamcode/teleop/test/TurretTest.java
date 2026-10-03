package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Turret;

@Config
public class TurretTest extends OpMode {
    private Turret turret;
    public static double testAngle = 0;

    @Override
    public void init(){
        Turret.manual = true;
        turret = new Turret(
                null,
                new ServoEx(hardwareMap, "turret1"),
                new ServoEx(hardwareMap, "turret2")
        );
    }

    @Override
    public void loop() {
        Turret.manualPositionDegrees = testAngle;
        turret.periodic();
    }

    @Override
    public void stop() {
        Turret.manual = false;
        turret.periodic();
    }
}
