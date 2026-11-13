package org.firstinspires.ftc.teamcode.teleop.util;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.subsystems.BotOpMode;

@Utility(name = "Good Line Test")
public class GoodLineTest extends BotOpMode {
    private boolean scheduled = false;

    public Path path1() {
        return line(Pose.zero(), new Pose(48,0, 0)).constant(0);
    }

    public Path path2() {
        return line(new Pose(48,0, 0), Pose.zero()).constant(0);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void loop() {
        if (!scheduled) {
            Scheduler.schedule(
                    sequential(
                            follow(bot.follower, path1()),
                            follow(bot.follower, path2())
                    ));
            scheduled = true;
        }

        super.loop();
    }
}
