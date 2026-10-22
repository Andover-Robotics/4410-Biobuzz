package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;

import java.util.Optional;

@TeleOp(name = "BB Test", group = "Test")
public class BBTest extends OpMode {
    private BB bottomBB;
    private Intake intake;

    private int objectsPassed = 0;
    private BB.Ball lastPassedSize = BB.Ball.NONE;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());

        DigitalChannel bottomBBBottomDigital = hardwareMap.get(DigitalChannel.class, "bottomBBBottom");
        DigitalChannel topBBBottomDigital = hardwareMap.get(DigitalChannel.class, "topBBBottom");
        bottomBB = new BB(bottomBBBottomDigital, Optional.of(topBBBottomDigital), size -> {
            lastPassedSize = size;
            if (intake.getState() == Intake.State.OUTTAKE) {
                objectsPassed--;
            } else {
                objectsPassed++;
            }
        });

        MotorEx intake1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx intake2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        intake2.setInverted(true);
        intake = new Intake(
                intake1,
                intake2,
                new ServoEx(hardwareMap, "gate"));
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

        bottomBB.periodic();
        intake.periodic();

        telemetry.addData("Bottom BB bottom", bottomBB.getBottomState());
        telemetry.addData("Bottom BB top", bottomBB.getTopState());
        telemetry.addData("Objects passed", objectsPassed);
        telemetry.addData("Last passed size", lastPassedSize);
        telemetry.update();
    }
}
