package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.telemetry.JoinedTelemetry;
import com.bylazar.telemetry.PanelsTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Bot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;
    public final Intake intake;

    public static Pose pose;

    public static Pose targetPose;

    public static Follower follower;

    public Bot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = new JoinedTelemetry(
                opMode.telemetry,
                PanelsTelemetry.INSTANCE.getFtcTelemetry()
        );
        intake = new Intake(this, "intake");
    }

    public void schedulePeriodic() {
        Scheduler.schedule();
    }
    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

}
