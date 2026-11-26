package org.firstinspires.ftc.teamcode.util;

public final class ColorUtils {
    private ColorUtils() {}

    public enum Color {
        // https://www.gobilda.com/rgb-indicator-light-pwm-controlled/
        YELLOW(0.384f),
        RED(0.280f),
        NONE(0.000f);

        private final float floodlightColor;

        Color(float floodlightColor) {
            this.floodlightColor = floodlightColor;
        }

        public float getFloodlightColor() {
            return floodlightColor;
        }
    }
}
