package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.util.BooleanDebouncer;

@Config
public class Limelight {
    public static int tipSideDebounceTimeMs = 250;

    private final Limelight3A limelight;
    private final BooleanDebouncer tipSideDebouncer = new BooleanDebouncer();

    public enum TipSide {
        AUDIENCE,
        AUDIENCE_OPPOSITE,
        NONE;

        //-        AprilTag ID’s 30, 31, 32, 33 on the red CELL on the side of the FIELD opposite of the audience.
        //-        AprilTag ID’s 34, 35, 36, 37 on the red CELL on the audience side.
        //-        AprilTag ID’s 38, 39, 40, 41 on the blue CELL on the audience side.
        //-        AprilTag ID’s 42, 43, 44, 45 on the blue CELL on the side of the FIELD opposite of the audience.

        public static TipSide getTipSideFromId(int id) {
            if ((id >= 30 && id <= 33) || (id >= 42 && id <= 45)) {
                return AUDIENCE_OPPOSITE;
            }

            if (id >= 34 && id <= 41) {
                return AUDIENCE;
            }

            return NONE;
        }
    }

    private TipSide tipSide = TipSide.NONE;

    public enum Pipeline {
        PIPELINE_APRILTAG(0);

        private final int id;

        Pipeline(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public Limelight(Limelight3A limelight) {
        this.limelight = limelight;

        limelight.start();
        limelight.setPollRateHz(100);
        setPipeline(Pipeline.PIPELINE_APRILTAG);
    }

    public void setPipeline(Pipeline pipeline) {
        limelight.pipelineSwitch(pipeline.getId());
    }

    public void periodic() {
        LLResult llResult = limelight.getLatestResult();

        TipSide observedTipSide = TipSide.NONE;
        if (llResult != null && llResult.isValid()) {
            observedTipSide = TipSide.getTipSideFromId(llResult.getFiducialResults().get(0).getFiducialId());
        }

        tipSideDebouncer.periodic(observedTipSide != TipSide.NONE, 0, tipSideDebounceTimeMs);

        if (tipSideDebouncer.getState()) {
            if (observedTipSide != TipSide.NONE) {
                tipSide = observedTipSide;
            }
        } else {
            tipSide = TipSide.NONE;
        }
    }

    public TipSide getTipSide() {
        return tipSide;
    }
}
