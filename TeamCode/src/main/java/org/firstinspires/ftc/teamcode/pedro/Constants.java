package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static Follower create(HardwareMap hardwareMap) {
        return new Follower(new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                new Mecanum(hardwareMap, Constants.driveConfig),
                new Foresight(Constants.foresightConfig));
    }

    public static MecanumConfig driveConfig = new MecanumConfig(
            c -> {
                c.frontLeftName.set("fl");
                c.frontRightName.set("fr");
                c.backLeftName.set("bl");
                c.backRightName.set("br");
                c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
                c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
                c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
                c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
            }
    );

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(1.4);
        c.yPodOffset.set(-5.5);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.35866227517170834);
                Controller secondaryTranslationalForward = Controller.proportional(0.1325161270434494);
                Controller primaryTranslationalLateral = Controller.proportional(0.5436430440572215);
                Controller secondaryTranslationalLateral = Controller.proportional(0.2008615783694694);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.012097498183003774));
                c.brake.set(Controller.proportionalFeedforward(0.010282873455553208));

                c.headingFeedback.set(Controller.proportional(4.973568897957717));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.058100217949664724, 0.004736640215149072));

                c.linearBrakeCoefficients.set(Matrix.diag(0.10270474895278607, 0.0846093962465019));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0017968606907006535, 0.0016016435024204443));

                c.maxAchievableForwardVelocity.set(79.13339669109519);
                c.maxAchievableStrafeVelocity.set(61.695843463200255);
                c.naturalForwardDeceleration.set(34.82429432026769);
                c.naturalStrafeDeceleration.set(64.72097300372957);
            }
    );

}