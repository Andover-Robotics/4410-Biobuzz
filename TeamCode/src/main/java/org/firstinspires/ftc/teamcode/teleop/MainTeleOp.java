package org.firstinspires.ftc.teamcode.teleop;

import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@TeleOp(name = "TeleOp", group = "Competition")
public class MainTeleOp extends BotOpMode {
    private GamepadEx gp1,gp2;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void init_loop(){
        super.init_loop();
    }

//    @Override
//    public void start() {
//
//    }

    @Override
    public void loop() {
        bot.follower.manual(
                -gp1.getLeftY(),
                gp1.getLeftX(),
                gp1.getRightX());

        if(gp1.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.2) {
            bot.intake.in();
        }
        if (gp1.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.2) {
            bot.intake.out();
        }
//      uncomment when shooter fully integrated
//        if(gp2.getRightY() < -0.2) {
//            bot.outtake.enable();
//        }
//        else{
//            bot.outtake.disable();
//            }
//        if(gp2.getLeftY() > 0.2){
//            bot.intake.openGate();
//        }
//        else {
//            bot.intake.closeGate();
//        }
        super.loop();
    }
}
