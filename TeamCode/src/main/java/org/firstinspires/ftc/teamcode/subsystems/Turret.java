package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.pedropathing.math.Velocity;
import com.pedropathing.utils.Angle;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class Turret {
    private static final double HALF_FIELD = 70.5;

    public static Pose frontTargetPoseRed = new Pose(HALF_FIELD - 12, HALF_FIELD + 12);
    public static Pose backTargetPoseRed = new Pose(HALF_FIELD - 12, HALF_FIELD - 12);

    public static Pose frontTargetPoseBlue = new Pose(HALF_FIELD + 12, HALF_FIELD + 12);
    public static Pose backTargetPoseBlue = new Pose(HALF_FIELD + 12, HALF_FIELD - 12);

    private Vector2D aimPoint;

    public static double shooterOffsetX = -2.5;
    public static double shooterOffsetY = 0;

    public static double lowLimitDegrees = -30;
    public static double highLimitDegrees = 180;

    public static double[] angles = new double[] {0, -90};
    public static double[] servoPositions = new double[] {0.50, 0.21};

    public static boolean manual = false;
    public static double manualPositionDegrees = 0;

    public static double G = 386.09;
    public static double heightDeltaInches = 0;
    public static double hoodAngle = 20;
    public static boolean velocityCompensation = false;

    private final Telemetry telemetry;
    private final Follower follower;
    private final ServoEx servo1;
    private final ServoEx servo2;

    private double distanceToGoal;
    private double angleToGoal;
    private double targetPositionDegrees;
    private double adjustableDegrees;

    public Turret(Telemetry telemetry, Follower follower, ServoEx servo1, ServoEx servo2) {
        this.telemetry = telemetry;
        this.follower = follower;
        this.servo1 = servo1;
        this.servo2 = servo2;
    }

    public void periodic() {
        aimTowardsTargetPoint();
        double servoPosition = angleToServoPosition(targetPositionDegrees);
        servo1.set(servoPosition);
        servo2.set(servoPosition);
    }

    public void updateTelemetry() {
        telemetry.addData("Turret/Mode", manual ? "MANUAL" : "AUTO");
        telemetry.addData("Turret/Target", getTargetPositionDegrees());
        if (!manual && aimPoint != null) {
            telemetry.addData("Turret/Aim X", aimPoint.x());
            telemetry.addData("Turret/Aim Y", aimPoint.y());
            telemetry.addData("Turret/Goal distance", getDistanceToGoal());
        }
    }

    private void aimTowardsTargetPoint() {
        if (manual) {
            targetPositionDegrees = clampToLimits(manualPositionDegrees);
            return;
        }

        Pose robotPose = follower.pose();
        Velocity robotVelocity = follower.velocity();
        Vector2D robotPosition = robotPose.toVector2D();
        Vector2D shooterOffset = Vector2D.cartesian(shooterOffsetX, shooterOffsetY)
                .rotate(robotPose.heading());

        aimPoint = getAimPoint(robotPose.y()).toVector2D();
        Vector2D delta = aimPoint.minus(robotPosition.plus(shooterOffset));

        if (velocityCompensation && robotVelocity.toVector2D().magnitude() > 0.1) {
            delta = applyVelocityCompensation(robotVelocity.toVector2D(), delta);
        }

        distanceToGoal = delta.magnitude();
        double fieldAngleToGoal = delta.theta();
        angleToGoal = Angle.normalizeSigned(fieldAngleToGoal - robotPose.heading());

        double targetRadians = Angle.normalizeSigned(
                -angleToGoal + Math.toRadians(adjustableDegrees));
        targetPositionDegrees = wrapAndClamp(targetRadians);
    }

    private static Pose getAimPoint(double y) {
        switch (Bot.allianceColor) {
            case RED:
                return y < HALF_FIELD ? backTargetPoseRed : frontTargetPoseRed;
            case BLUE:
                return y < HALF_FIELD ? backTargetPoseBlue : frontTargetPoseBlue;
            default:
                throw new IllegalStateException();
        }
    }

    private static Vector2D applyVelocityCompensation(
            Vector2D fieldVelocity,
            Vector2D uncompensatedDelta
    ) {
        Vector2D compensatedDelta = uncompensatedDelta;
        for (int i = 0; i < 3; i++) {
            double timeToGoal = getTimeToGoal(compensatedDelta.magnitude());
            compensatedDelta = uncompensatedDelta.minus(fieldVelocity.times(timeToGoal));
        }
        return compensatedDelta;
    }

    private static double getTimeToGoal(double horizontalDistance) {
        double timeSquared = 2.0 * (horizontalDistance * Math.tan(Math.toRadians(90 - hoodAngle)) - heightDeltaInches) / G;
        return timeSquared > 0 ? Math.sqrt(timeSquared) : 0;
    }

    private static double wrapAndClamp(double targetRadians) {
        return clampToLimits(Math.toDegrees(Angle.normalizeSigned(targetRadians)));
    }

    private static double clampToLimits(double positionDegrees) {
        double minimumAngle = Math.min(lowLimitDegrees, highLimitDegrees);
        double maximumAngle = Math.max(lowLimitDegrees, highLimitDegrees);
        return Math.max(minimumAngle, Math.min(maximumAngle, positionDegrees));
    }

    private static double angleToServoPosition(double angleDegrees) {
        double angleRange = angles[1] - angles[0];
        if (Math.abs(angleRange) < 1e-9) {
            return servoPositions[0];
        }

        double interpolation = (angleDegrees - angles[0]) / angleRange;
        return servoPositions[0]
                + interpolation * (servoPositions[1] - servoPositions[0]);
    }

    public double getTargetPositionDegrees() {
        return targetPositionDegrees;
    }

    public double getServoPosition() {
        return servo1.get();
    }

    public double getDistanceToGoal() {
        return distanceToGoal;
    }

    public double getAngleToGoal() {
        return angleToGoal;
    }

    public double getAdjustable() {
        return adjustableDegrees;
    }

    public Vector2D getAimPoint() {
        return aimPoint;
    }

    public void setAdjustable(double adjustableDegrees) {
        this.adjustableDegrees = adjustableDegrees;
    }
}
