package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public class FTCCoordinatesPoseFactory {
    public static PoseFactory ftcCoordinates(boolean useDegrees) {
        // field size is 141.5in x 141.5in
        final double HALF_FIELD_WIDTH = 141.5 / 2;

        return new PoseFactory(p -> new Pose(
                HALF_FIELD_WIDTH + p.y(),
                HALF_FIELD_WIDTH - p.x(),
                p.heading() - Math.PI / 2
        ), useDegrees);
    }

    public static PoseFactory degrees() {
        return ftcCoordinates(true);
    }

    public static PoseFactory radians() {
        return ftcCoordinates(false);
    }
}
