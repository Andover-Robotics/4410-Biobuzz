package org.firstinspires.ftc.teamcode.subsystems;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.teamcode.subsystems.Bot;
//import org.firstinspires.ftc.teamcode.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.util.LinearInterpolation;

import java.util.ArrayList;

@Configurable
public class Turret {
    private final ServoEx servo;
    private final ServoEx servo1;
    private final ElapsedTime timer = new ElapsedTime();
    public Pose pose;
    public Velocity velocity;

    public static boolean shooterActive = true, positionTracking = true;
    public static double POS_TRACK_X = 0;
    public static double POS_TRACK_Y = 0;
    public static double TURRET_OFFSET_BACK_IN = 1; // inches back from robot center
    public static double rapidFireDistanceThresholdIn = 122;
    public static double rapidFireSleepScalePerIn = 0.004;


    public static double feedforwardPower, velFFPower, accelFFPower, staticFFPower;

    private double tolerance = 1, powerMin = 0.05, degsPerTick = 360.0 / (145.1 * 104.0 / 10.0), ticksPerRev = 360 / degsPerTick;

    //private final LinearInterpolation rpmInterpolator;

    public double power, lastTime, setPoint = 0, pos = 0, highLimit = 210, lowLimit = -160, trackingTarget, runToTargetAngle;

    public static double shooterRpm = 0, trackingDistance, pureDistance;


    public static boolean velComp = true, shooterOverride = false, deadzone = false;


    public Turret(Bot bot, String name) {
        servo = new ServoEx(bot.hardwareMap, name, 0, 355);
        servo1 = new ServoEx(bot.hardwareMap, name, 0, 355);
        servo1.setInverted(true);
        //rpmInterpolator = new LinearInterpolation(SHOOTER_DISTANCE_IN, SHOOTER_RPM); TODO: get interpolation when needed :)
        setVelComp(false);
    }

    public void enableFullAuto(boolean on) {
        enableAutoAim(on);
        enableShooter(on);
    }

    public void enableAutoAim(boolean on) {
        enablePositionTracking(on);
    }

    public void enableShooter(boolean enable) {
        shooterActive = enable;
    }

    public void enablePositionTracking(boolean enable) {
        positionTracking = enable;
    }

    public boolean shooterInRange() {
        return shooterActive;
    }

    public void runToAngle(double angle) {
        if (servo.get() > 0 && ((angle + 360) % 360) > highLimit) {
            angle = angle - 360;
        } else if (angle < lowLimit) {
            angle = angle + 360;
        } else if (servo.get() > 0 && angle > lowLimit) {
            angle = (angle + 360) % 360;
        }
        angle = Math.min(Math.max(lowLimit, angle), highLimit);
        runToTargetAngle = angle;
        int t = (int) ((angle) / degsPerTick);
        runTo(t);
    }

    private void runTo(int t) { //takes in degrees
        servo.set(t);
        servo1.set(t);
    }

//    public void runManual(double manual) {
//        if (manual > powerMin || manual < -powerMin) {
//            manualPower = manual;
//        } else {
//            manualPower = 0;
//        }
//    }

    // Normalize to [-180, 180)
    private static double normDeg(double a) {
        return ((a + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    /**
     * Aim the turret at a fixed field point using the robot's live pose.
     * - Field/robot headings are CCW positive.
     * - Turret encoder angles are CW positive.
     * - Turret zero is 180° (backwards) from robot-forward.
     * <p>
     * steps:
     * 1) fieldAngleCCW = atan2(dy, dx)
     * 2) relToRobotCCW = fieldAngleCCW - robotHeadingCCW
     * 3) turretTargetCW = -relToRobotCCW + TURRET_ZERO_CW_OFFSET
     */
    private double aimAtGlobalPoint(double targetX, double targetY) {
        pose = Bot.pose;

        // Robot heading in radians (CCW+)
        double headingRad = pose.heading();

        // Turret position in field frame:
        // "back" = negative X in robot frame, rotated into field frame
        double turretX = pose.x() - TURRET_OFFSET_BACK_IN * Math.cos(headingRad);
        double turretY = pose.y() - TURRET_OFFSET_BACK_IN * Math.sin(headingRad);

        // Vector from turret to target in field frame
        double dx = targetX - turretX;
        double dy = targetY - turretY;

        velocityCompensation(dx, dy);

        // Field-bearing CCW to target
        double fieldAngleCCW = Math.toDegrees(Math.atan2(POS_TRACK_Y, POS_TRACK_X)); // CCW+

        // Robot heading CCW
        double robotHeadingCCW = Math.toDegrees(headingRad); // CCW+

        // Robot-relative CCW angle to target
        double relToRobotCCW = normDeg(fieldAngleCCW - robotHeadingCCW);

        // Turret is CW-positive and zero is backwards, so:
        // 0° turret = 180° robot-relative CCW
//        double turretTargetCW = normDeg(-relToRobotCCW + 180);

        double turretTargetCW = normDeg(-relToRobotCCW);

        pureDistance = Math.sqrt(dx * dx + dy * dy);

        // Tracking distance from turret to goal
        trackingDistance = Math.sqrt(POS_TRACK_X * POS_TRACK_X + POS_TRACK_Y * POS_TRACK_Y);

        return turretTargetCW;
    }

    public void velocityCompensation(double dx, double dy) {
        double currentTurretDegs = servo.get();
        if (currentTurretDegs < highLimit - 5 && currentTurretDegs > lowLimit + 5) {
            double time = calculateTime(dx, dy);
            velocity = Bot.follower.velocity();
//        double dispX = velocity.linearVel.x * time;
//        double dispY = velocity.linearVel.y * time;
//        POS_TRACK_X = dx + dispX;
//        POS_TRACK_Y = dy + dispY;
            double heading = pose.heading();

            // Convert robot-centric velocity to field frame
            double velocityXField = velocity.vx * Math.cos(heading) - velocity.vy * Math.sin(heading);
            double velocityYField = velocity.vx * Math.sin(heading) + velocity.vy * Math.cos(heading);

            // Offset the target opposite the robot's drift so that the added launch
            // velocity from the robot's motion lands on the goal.
            double dispX = velocityXField * time;
            double dispY = velocityYField * time;
            if (velComp) {
                POS_TRACK_X = dx - dispX;
                POS_TRACK_Y = dy - dispY;
            } else {
                POS_TRACK_X = dx;
                POS_TRACK_Y = dy;
            }
        }
    }

    public void setVelComp(boolean i) {
        velComp = i;
    }

    public double calculateTime(double dx, double dy) {
        // Constants
        double G = 386.09;                 // in/s^2 (gravity in inches)
        double heightDisplacement = 26.0;  // inches (Δz)
        double launchAngleAboveHorizDeg = 90 - 63;  // (90 degrees - actual shooter angle) -> makes the angle relative to horizontal plane
        double launchAngleRad = Math.toRadians(launchAngleAboveHorizDeg);

        // Horizontal distance (XY plane)
        double R = Math.sqrt(dx * dx + dy * dy);

        // t^2 = (2/g) * (R * tan(theta) - Δz)
        double term = R * Math.tan(launchAngleRad) - heightDisplacement;
        double tSquared = (2.0 / G) * term;

        if (tSquared <= 0) {
            // No ballistic solution (target too low for given angle)
            return 0;  // or any fallback (0 means “no adjustment”)
        }

        double t = Math.sqrt(tSquared);
        return t;
    }


    public void periodic() {
        if (positionTracking) {
            double target = aimAtGlobalPoint(Bot.targetPose.x(), Bot.targetPose.y());
            runToAngle(target);
        }


    }

//    public void setShooterVelocity(double rpm) {
//        shooter.setVelocity(rpm);
//    }

    public void setShooterOverride(boolean override) {
        shooterOverride = override;
    }


    public static double getRapidShootSleep(double baseSleepSeconds) {
        double extraDistance = Math.max(0.0, trackingDistance - rapidFireDistanceThresholdIn);
        return baseSleepSeconds + (extraDistance * rapidFireSleepScalePerIn);
    }


    public double getTargetTicks() {
        return setPoint;
    }

    public double getTargetDegs() {
        return setPoint * degsPerTick;
    }

    public double getPower() {
        return power;
    }
}




