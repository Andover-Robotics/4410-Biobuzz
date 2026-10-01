package org.firstinspires.ftc.teamcode.util;


import com.acmerobotics.dashboard.config.Config;

@Config
public class ColorUtils {
    public static float hTolerance = 0;
    public static float sTolerance = 0;
    public static float vTolerance = 0;

    public static boolean compareHSV(float[] a, float[] b) {
        return Math.abs(a[0] - b[0]) <= hTolerance && Math.abs(a[1] - b[1]) <= sTolerance
                && Math.abs(a[2] - b[2]) <= vTolerance;
    }

    public static float[] pollenColor = new float[] { 0, 0, 0 };
    public static float[] nectarRedColor = new float[] { 0, 0, 0 };
    public static float[] nectarBlueColor = new float[] { 0, 0, 0 };

    public enum Color {
        POLLEN(pollenColor),
        NECTAR_RED(nectarRedColor),
        NECTAR_BLUE(nectarBlueColor),
        NONE(new float[] { -999, -999, -999 });

        private float[] color;

        Color(float[] colors) {
            this.color = colors;
        }

        public void update() {
            switch (this) {
                case POLLEN:
                    color = pollenColor;
                    break;
                case NECTAR_RED:
                    color = nectarRedColor;
                    break;
                case NECTAR_BLUE:
                    color = nectarBlueColor;
                    break;
            }
        }

        public boolean compareHSV(float[] other) {
            return ColorUtils.compareHSV(color, other);
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