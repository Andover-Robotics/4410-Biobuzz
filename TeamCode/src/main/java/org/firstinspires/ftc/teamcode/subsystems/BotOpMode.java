package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class BotOpMode extends OpMode {
    protected Bot bot;

    @Override
    public void init() {
        Scheduler.reset();
        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry());
        bot = new Bot(hardwareMap, telemetry);
        bot.intake.stop();
        bot.intake.closeGate();
        bot.schedulePeriodic();
    }

    @Override
    public void init_loop() {
        Scheduler.execute();
    }

    @Override
    public void loop() {
        Scheduler.execute();
    }
}
