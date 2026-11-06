package org.firstinspires.ftc.teamcode.util;

public final class ColorUtils {
    private ColorUtils() {}

    public enum Color {
        // https://www.gobilda.com/rgb-indicator-light-pwm-controlled/
        YELLOW(0.384f),
        PURPLE(0.721f),
        NONE(1.000f);

        private final float floodlightColor;

        Color(float floodlightColor) {
            this.floodlightColor = floodlightColor;
        }

        public float getFloodlightColor() {
            return floodlightColor;
        }
    }
}
