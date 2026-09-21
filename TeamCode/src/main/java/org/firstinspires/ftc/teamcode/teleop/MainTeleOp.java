package org.firstinspires.ftc.teamcode.teleop;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@TeleOp(name = "TeleOp", group = "Competition")
public class MainTeleOp extends BotOpMode {
    @Override
    public void init() {
        super.init();
    }

    @Override
    public void start() {
        Scheduler.schedule(bot.intake.store());
    }

    @Override
    public void loop() {
        super.loop();
    }
}
