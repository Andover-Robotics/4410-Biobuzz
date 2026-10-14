package org.firstinspires.ftc.teamcode.teleop.util;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Turret;

@TeleOp(name = "Zero Turret", group = "Util")
@Config
public class ZeroTurret extends OpMode {
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
        Turret.manualPositionDegrees = 0;
        turret.periodic();
    }

    @Override
    public void stop() {
        Turret.manual = false;
    }
}
