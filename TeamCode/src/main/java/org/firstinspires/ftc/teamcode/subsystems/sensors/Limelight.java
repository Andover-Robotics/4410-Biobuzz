package org.firstinspires.ftc.teamcode.subsystems.sensors;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector;
import com.pedropathing.math.Velocity;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.pedropathing.math.Matrix;

import java.util.ArrayList;
import java.util.List;

public class Limelight {
    //get location of larges cluster of pollen
    // turn to cluster
    // turn if nothing seen
    //use pedro for turning
    //when cluster found, drive to it
    // cluser based on proximity to each other (x,y), if all (x,y) within x% of center

    private final Limelight3A ll;
    private LLResult llResult;
    private final Follower follower;
    private final double POLLEN_RADIUS = 2.8/2;
    private final double CAM_X_OFFSET = 0;
    private final double CAM_Y_OFFSET = 0;
    private final double CAM_Z_OFFSET = 0;
    private final double PITCH = 0; // pitch of cam in radians
    public static double stalenessThreshold = 100, clusterThreshold = 10, bestDist = 0;
    private List<Pose> pollenTargets = new ArrayList<>();
    private List<Pose> pollenCluster = new ArrayList<>();
    private int bestSize;
    private Pose driveTarget = null;



    public Limelight(Limelight3A ll,Follower follower) {
        this.ll = ll;
        this.follower = follower;
        ll.start();
        ll.setPollRateHz(100);
        setPipline(0);
    }

    private Vector camVector(double tx,double ty) { // unit vector representing location of the ball in spherical coordinates relative to the camera
        return new Vector(
                Math.cos(tx)*Math.cos(ty),
                Math.sin(tx)*Math.cos(ty),
                Math.sin(ty)
        );
    }

    private Matrix camToBot(double pitch) { // rotation matrix to change the reference frame of the unit vector to the bot's
        double s = Math.sin(pitch);
        double c = Math.cos(pitch);
        return new Matrix(new double[][] {
                {c,0,s},
                {0,1,0},
                {-s,0,c}
        });
    }
    private Pose findPollen(double tx, double ty, double pitch, Pose botPose, Velocity botVel) {
        //using dist(scalar) = radius-cam_z/world_z
        Vector cam = camVector(tx, ty);
       // double xyVel = botVel.toVector2D().magnitude();
        Matrix camToBot = camToBot(pitch);
        Vector camBotVector = camToBot.times(cam);
        double z = camBotVector.elements()[2];
        if(z < 1e-5){
            return null;
        }
        double dist = (POLLEN_RADIUS - CAM_Z_OFFSET) / z; //finds scalar which unit vector is multiplied by to intersect with a plane parallel to the field passing through the balls' radius
        double botX = CAM_X_OFFSET + dist * camBotVector.elements()[0]; // finds cartesian x relative to the bot
        double botY = CAM_Y_OFFSET + dist * camBotVector.elements()[1]; // find cartesian y relative to the bot
        double heading = botPose.heading();
        double fieldX = botPose.x() + (botX * Math.cos(heading) - botY * Math.sin(heading)); // tranforms the x to the field's perspective
        double fieldY = botPose.y() + (botX * Math.sin(heading) + botY * Math.cos(heading)); // transforms the y to the field's perspective
        //TODO: add compensation for balls' velocity
        return new Pose(fieldX, fieldY);
    }
    public void periodic() {
        llResult = ll.getLatestResult();
        if (llResult.isValid() && llResult != null) {
            List<LLResultTypes.DetectorResult> detections = llResult.getDetectorResults();
            if (llResult.getStaleness() > stalenessThreshold || detections.isEmpty()) {
                pollenTargets.clear();
            }
            for (LLResultTypes.DetectorResult detection : detections) {
                double tx = Math.toRadians(detection.getTargetXDegrees()), ty = Math.toRadians(detection.getTargetYDegrees());
                Pose target = findPollen(tx, ty, PITCH, follower.pose(), follower.velocity());
                if (target != null) pollenTargets.add(target);
            }
            // checks every balls' distance to every other ball and finds a grouping of balls close together,
            // then sets the target to the midpoint of the largest cluster, or closest if tied
            for (Pose p : pollenTargets) {
                pollenCluster.clear();
                pollenCluster.add(p);
                for (Pose q : pollenTargets) {
                    if (q != p && q.distance(p) < clusterThreshold) {
                        pollenCluster.add(q);
                    }
                }

                int size = pollenCluster.size();
                Pose center = getCentroid(pollenCluster);
                if (size > bestSize) {
                    bestSize = size;
                    driveTarget = center;
                } else if (bestSize == size) {
                    double dist = follower.pose().distance(center);
                    if(dist < bestDist) {
                        bestDist = dist;
                        driveTarget = center;
                    }
                }
            }
        }
    }
    public Pose getDriveTarget(){
        return driveTarget;
    }

    private Pose getCentroid(List<Pose> pollenCluster) {
        double sumX = 0, sumY = 0;
        int size = pollenCluster.size();
        for(Pose p : pollenCluster) {
            sumX += p.x();
            sumY += p.y();
        }
        double x = sumX / size;
        double y = sumY / size;

        return new Pose(x, y);
    }

    public void setPipline(int i){
        ll.pipelineSwitch(i);
    }

}
