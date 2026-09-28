package org.firstinspires.ftc.teamcode.util;


import com.acmerobotics.dashboard.config.Config;

@Config
public class ColorUtils {
    public static float H_TOLERANCE = 0;
    public static float S_TOLERANCE = 0;
    public static float V_TOLERANCE = 0;

    public static boolean compareHSV(float[] a, float[] b) {
        return Math.abs(a[0] - b[0]) <= H_TOLERANCE && Math.abs(a[1] - b[1]) <= S_TOLERANCE
                && Math.abs(a[2] - b[2]) <= V_TOLERANCE;
    }

    public enum Color {
        POLLEN(new float[] { 0, 0, 0 }),
        NECTAR_RED(new float[] { 0, 0, 0}),
        NECTAR_BLUE(new float[] { 0, 0, 0 }),
        NONE(new float[] {-999,-999,-999});

        private final float[] colors;

        Color(float[] colors) {
            this.colors = colors;
        }

        public boolean compareHSV(float[] other) {
            return ColorUtils.compareHSV(colors, other);
        }

        public static Color match(float[] color) {
            for (Color c : Color.values()) {
                if (c.compareHSV(color)) {
                    return c;
                }
            }

            return Color.NONE;
        }
    }
}