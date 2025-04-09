package TunerUtils;

public class TunerConfig {

    // Raw data format specs
    public static final float SAMPLE_RATE = 48000.0f;
    public static final int SAMPLE_SIZE = 16;
    public static final int CHANNEL_SIZE = 1;
    public static final int BUFFER_SIZE = 8192;

    // Guitar String Frequencies
    public static int lockedStringIndex = -1;
    public static final double LOCK_RADIUS_HZ = 6.0;
    public static final double UNLOCK_RADIUS_HZ = 10.0;

    public static double[] stringFrequencies = new double[6];

    // Tuning Types
    public static final String[] tuningTypes = {
            "Standard (EADGBe)",
            "Drop D (DADGBe)",
            "Half Step Down (D#G#C#F#A#d#)"
    };

    // Get Guitar string name using 'Enhanced switch statement' to return string names
    public static String getStringName(int index) {
        return switch (index) {
            case 0 -> "Low E";
            case 1 -> "A";
            case 2 -> "D";
            case 3 -> "G";
            case 4 -> "B";
            case 5 -> "High e";
            default -> "Unknown";
        };
    }

    // Tuning Type Frequencies
    public static final double[][] allTunings = {
            {82.41, 110.00, 146.83, 196.00, 246.94, 329.63}, // Standard Tuning Freq
            {73.42, 110.00, 146.83, 196.00, 246.94, 329.63},  // Drop 'D' Tuning Freq
            {77.78, 103.83, 138.59, 185.00, 233.08, 311.13}  // Half Step Down Tuning
    };
}
