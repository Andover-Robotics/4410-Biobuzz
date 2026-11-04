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
        c.xPodOffset.set(1.5);
        c.yPodOffset.set(-5.5);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.3613897366874942);
                Controller secondaryTranslationalForward = Controller.proportional(0.13352385119441829);
                Controller primaryTranslationalLateral = Controller.proportional(0.8283526244276272);
                Controller secondaryTranslationalLateral = Controller.proportional(0.30605416073623604);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.012850452700520031));
                c.brake.set(Controller.proportionalFeedforward(0.010922884795442026));

                c.headingFeedback.set(Controller.proportional(5.69528749844584));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.07406226076588594, 0.0032316246308411478));

                c.linearBrakeCoefficients.set(Matrix.diag(0.2356028465061876, 0.06149303110566884));
                c.quadraticBrakeCoefficients.set(Matrix.diag(-0.0014506780635534376, 0.0019217445387624502));

                c.maxAchievableForwardVelocity.set(75.3704030946175);
                c.maxAchievableStrafeVelocity.set(61.315078221703004);
                c.naturalForwardDeceleration.set(27.33240413140022);
                c.naturalStrafeDeceleration.set(59.42782557393842);
            }
    );

}