package org.firstinspires.ftc.teamcode.teleop.test;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Intake;

@TeleOp(name = "Intake Test", group = "Test")
public class IntakeTest extends OpMode {
    private Intake intake;

    @Override
    public void init() {
        Scheduler.reset();
        MotorEx motor1 = new MotorEx(hardwareMap, "intake1", Motor.GoBILDA.RPM_1150);
        MotorEx motor2 = new MotorEx(hardwareMap, "intake2", Motor.GoBILDA.RPM_1150);
        motor2.setInverted(true);
        intake = new Intake(
                motor2, motor1,
                new ServoEx(hardwareMap, "gate"));
        intake.closeGate();
    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper) {
            intake.intake();
        } else if (gamepad1.right_bumper) {
            intake.outtake();
        } else {
            intake.store();
        }

        if (gamepad1.aWasPressed()) {
            intake.toggleGate();
        }

        intake.periodic();

        telemetry.addData("Top stalled", intake.getTopStalled());
        telemetry.addData("Bottom stalled", intake.getBottomStalled());
        telemetry.addData("Top current", intake.getTopCurrent());
        telemetry.addData("Bottom current", intake.getBottomCurrent());
        telemetry.update();
    }

    @Override
    public void stop() {

    }
}
