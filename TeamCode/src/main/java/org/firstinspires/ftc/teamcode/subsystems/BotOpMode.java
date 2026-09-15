package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class BotOpMode extends OpMode {
    protected Bot bot;

    @Override
    public void init() {
        Scheduler.reset();
        bot = new Bot(this);

        bot.schedulePeriodic();
    }

    @Override
    public void init_loop() {
        Scheduler.execute();
    }

    @Override
    public void loop() {
        Scheduler.execute();
        bot.telemetry.update();
    }
}
