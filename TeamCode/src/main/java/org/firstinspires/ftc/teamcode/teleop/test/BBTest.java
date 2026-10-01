package org.firstinspires.ftc.teamcode.teleop.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.subsystems.sensors.BB;

@TeleOp(name = "BB Test", group = "Test")
public class BBTest extends OpMode {
    private DigitalChannel channel;
    private BB bb;
    private int objectsPassed;
    private BB.Ball lastPassedSize = BB.Ball.NONE;

    @Override
    public void init() {
        objectsPassed = 0;
        lastPassedSize = BB.Ball.NONE;
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        channel = hardwareMap.get(DigitalChannel.class, "bb");
        bb = new BB(channel, size -> {
            lastPassedSize = size;
            objectsPassed++;
        });
    }

    @Override
    public void loop() {
        bb.periodic();

        telemetry.addData("Beam raw", channel.getState());
        telemetry.addData("Beam broken (filtered)", bb.isBroken());
        telemetry.addData("Objects passed", objectsPassed);
        telemetry.addData("Last passed size", lastPassedSize);
        telemetry.update();
    }
}
