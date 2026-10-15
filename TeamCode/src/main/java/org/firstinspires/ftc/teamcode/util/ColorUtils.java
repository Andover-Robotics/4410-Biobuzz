package org.firstinspires.ftc.teamcode.util;


import com.acmerobotics.dashboard.config.Config;

@Config
public class ColorUtils {
    public static float hTolerance = 0;
    public static float sTolerance = 0;
    public static float vTolerance = 0;

    public static boolean hsvMatches(float[] a, float[] b) {
        float deltaH = Math.abs(a[0] - b[0]) % 360;
        deltaH = Math.min(deltaH, 360 - deltaH);
        float deltaS = Math.abs(a[1] - b[1]);
        float deltaV = Math.abs(a[2] - b[2]);
        return deltaH < hTolerance && deltaS < sTolerance && deltaV < vTolerance;
    }

    public static float[] pollenColor = new float[] { 0, 0, 0 };
    public static float[] nectarRedColor = new float[] { 0, 0, 0 };
    public static float[] nectarBlueColor = new float[] { 0, 0, 0 };

    public enum Color {
        // https://www.gobilda.com/rgb-indicator-light-pwm-controlled/
        POLLEN(pollenColor, 0.384f),
        NECTAR_RED(nectarRedColor, 0.280f),
        NECTAR_BLUE(nectarBlueColor, 0.611f),
        NONE(new float[] { -999, -999, -999 }, 0.000f);

        private float[] color;
        private float floodlightColor;

        Color(float[] colors, float floodlightColor) {
            this.color = colors;
            this.floodlightColor = floodlightColor;
        }

        private void update() {
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

        public boolean hsvMatches(float[] other) {
            update();
            return this != NONE && ColorUtils.hsvMatches(color, other);
        }

        public static Color match(float[] color) {
            for (Color c : Color.values()) {
                if (c.hsvMatches(color)) {
                    return c;
                }
            }

            return Color.NONE;
        }

        public float[] getColor() {
            update();
            return color;
        }

        public float getFloodlightColor() {
            return floodlightColor;
        }
    }
}