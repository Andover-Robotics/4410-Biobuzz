package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.telemetry.JoinedTelemetry;
import com.bylazar.telemetry.PanelsTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

public class Bot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;
    public final Intake intake;
    public final Turret turret;
    public final Shooter outtake;

    public final VoltageSensor voltageSensor;

    public final Follower follower;

    public Bot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = new JoinedTelemetry(
                opMode.telemetry,
                PanelsTelemetry.INSTANCE.getFtcTelemetry()
        );
        follower = Constants.create(hardwareMap);
        voltageSensor = opMode.hardwareMap.voltageSensor.iterator().next();
        intake = new Intake(this, "intake");
        turret = new Turret(this, "turret1", "turret2");
        outtake = new Shooter(this, "outtake1", "outtake2");
    }

    public void schedulePeriodic() {
        Scheduler.schedule(Commands.infinite(() -> {
            follower.update();
            turret.periodic();
            outtake.setDistanceToGoal(turret.getDistanceToGoal());
            outtake.periodic();
        }));
    }
}
