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
    private static final double HALF_FIELD = 141.5 / 2;

    public static Pose frontTargetPoseRed = new Pose(HALF_FIELD - 12, HALF_FIELD + 12);
    public static Pose backTargetPoseRed = new Pose(HALF_FIELD - 12, HALF_FIELD - 12);

    public static Pose frontTargetPoseBlue = new Pose(HALF_FIELD + 12, HALF_FIELD + 12);
    public static Pose backTargetPoseBlue = new Pose(HALF_FIELD + 12, HALF_FIELD - 12);

    private Vector2D aimPoint;

    public static double shooterOffsetX = -0.726;
    public static double shooterOffsetY = 0;

    public static double lowLimitDegrees = 5;
    public static double highLimitDegrees = 310;

    public static double[] angles = new double[] {0, 90};
    public static double[] servoPositions = new double[] {0.01, 0.285};

    public static boolean manual = false;
    public static double manualPositionDegrees = 0;

    public static boolean velocityCompensation = true;

    private final Telemetry telemetry;
    private final Follower follower;
    private final ServoEx servo1;
    private final ServoEx servo2;

    private double distanceToGoal;
    private double angleToGoal;
    private double targetPositionDegrees;
    private boolean outOfRange;
    private double adjustableDegrees;
    private double robotVelocityMagnitude;

    public Turret(Telemetry telemetry, Follower follower, ServoEx servo1, ServoEx servo2) {
        this.telemetry = telemetry;
        this.follower = follower;
        this.servo1 = servo1;
        this.servo2 = servo2;
    }

    public void periodic() {
        outOfRange = false;

        double requestedPositionDegrees = calculateTargetPositionDegrees();

        if (!manual) {
            requestedPositionDegrees = targetPositionDegrees + Math.IEEEremainder(
                    requestedPositionDegrees - targetPositionDegrees, 360.0);
            targetPositionDegrees = clampToLimits(requestedPositionDegrees);
        } else {
            targetPositionDegrees = requestedPositionDegrees;
        }

        double servoPosition = angleToServoPosition(targetPositionDegrees);

        outOfRange = outOfRange
                || (Math.abs(Math.IEEEremainder(requestedPositionDegrees - targetPositionDegrees, 360.0)) > 1e-6)
                || (servoPosition < 0 || servoPosition > 1)
                || (distanceToGoal < Shooter.MIN_DISTANCE || distanceToGoal > Shooter.MAX_DISTANCE);

        servo1.set(servoPosition);
        servo2.set(servoPosition);
    }

    public void updateTelemetry() {
        telemetry.addData("Turret/Out of range", outOfRange);
        if (!manual && aimPoint != null) {
            telemetry.addData("Turret/Goal distance", distanceToGoal);
            telemetry.addData("Turret/Robot velocity", robotVelocityMagnitude);
        }
    }

    private double calculateTargetPositionDegrees() {
        if (manual) {
            return manualPositionDegrees;
        }

        Pose robotPose = follower.pose();
        Velocity robotVelocity = follower.velocity();
        Vector2D robotPosition = robotPose.toVector2D();
        Vector2D shooterOffset = Vector2D.cartesian(shooterOffsetX, shooterOffsetY)
                .rotate(robotPose.heading());

        aimPoint = getAimPoint(robotPose.y()).toVector2D();
        Vector2D delta = aimPoint.minus(robotPosition.plus(shooterOffset));

        robotVelocityMagnitude = robotVelocity.toVector2D().magnitude();

        if (velocityCompensation && robotVelocityMagnitude > 0.1) {
            delta = applyVelocityCompensation(robotVelocity.toVector2D(), delta);
        }

        distanceToGoal = delta.magnitude();
        double fieldAngleToGoal = delta.theta();
        angleToGoal = Angle.normalizeSigned(fieldAngleToGoal - robotPose.heading());

        double targetRadians = Angle.normalizeSigned(
                -angleToGoal + Math.toRadians(adjustableDegrees));
        return Math.toDegrees(Angle.normalizeSigned(targetRadians));
    }

    private Pose getAimPoint(double y) {
        if (y > HALF_FIELD - 24 && y < HALF_FIELD + 24) {
            outOfRange = true;
        }

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
        for (int i = 0; i < 10; i++) {
            double flightTime = Shooter.getFlightTime(compensatedDelta.magnitude());
            compensatedDelta = uncompensatedDelta.minus(fieldVelocity.times(flightTime));
        }
        return compensatedDelta;
    }

    private static double clampToLimits(double positionDegrees) {
        double wrappedPosition = positionDegrees
                + 360.0 * Math.ceil((lowLimitDegrees - positionDegrees) / 360.0);

        if (wrappedPosition <= highLimitDegrees) {
            return wrappedPosition;
        }

        return Math.max(lowLimitDegrees, Math.min(highLimitDegrees, positionDegrees));
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

    public boolean isOutOfRange() {
        return outOfRange;
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

    public double getRobotVelocityMagnitude() {
        return robotVelocityMagnitude;
    }

    public Vector2D getAimPoint() {
        return aimPoint;
    }

    public void setAdjustable(double adjustableDegrees) {
        this.adjustableDegrees = adjustableDegrees;
    }
}
