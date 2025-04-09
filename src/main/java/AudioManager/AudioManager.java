package AudioManager;

import TunerUtils.TunerConfig;

public class AudioManager {

    /**
     * Convert raw data to audio samples. This is done by looping through the raw data and shifting the 8 bits
     * on the far right to the left. To ensure the correct bits are moved, mask the right side bits with '0xFF'
     *
     * @param buffer byte[] - The buffer holding the raw audio data
     * @param bytesRead int - The raw data captured by the listener
     * @return audioSamples double[] - The converted audio data
     */
    public static double[] convertToAudioSamples(byte[] buffer, int bytesRead) {

        double[] audioSamples = new double[bytesRead / 2];

        for (int i = 0, j = 0; i < bytesRead - 1; i += 2, j++) {
            // Shift high byte to the left by 8 bits
            // mask the right side byte w/ '0xFF' to ensure it keeps right
            int sample = (buffer[i+1] << 8) | (buffer[i] & 0xFF);

            // '32768.0' is max possible value of a signed 16-bit integer
            // which would give us the range of [-1, 1] (Normalization)
            audioSamples[j] = sample / 32768.0;
        }

        return audioSamples;
    }

    /**
     * Detect the frequency from the collected audio samples.
     *
     * @param audioSamples double[] - the converted audio samples
     * @return frequency double - the frequency of the current sample
     */
    public static double detectFrequency(double[] audioSamples) {
        int size = audioSamples.length;
        double[] autocorrelation = new double[size];

        // Step 1: Hann Window
        for (int i = 0; i < size; i++) {
            double hann = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
            audioSamples[i] *= hann;
        }

        // Step 2: Compute Autocorrelation
        for (int lag = 0; lag < size; lag++) {
            for (int i = 0; i < size - lag; i++) {
                autocorrelation[lag] += audioSamples[i] * audioSamples[i + lag];
            }
        }

        // Step 3: Normalize
        for (int lag = 0; lag < size; lag++) {
            autocorrelation[lag] /= autocorrelation[0];
        }

        // Step 4: Find the sig peak

        int minLag = (int)(TunerConfig.SAMPLE_RATE / 400);
        int maxLag = (int)(TunerConfig.SAMPLE_RATE / 65);

        double maxVal = -1;
        int fundamentalLag = -1;

        for (int lag = minLag; lag <  maxLag; lag++) {
            if (autocorrelation[lag] > maxVal) {
                maxVal = autocorrelation[lag];
                fundamentalLag = lag;
            }
        }

        // Remove 'noise' data
        if (maxVal < 0.2) return 0.0;

        // Step 5: Convert Lag to Frequency
        if (fundamentalLag > 0) {
            // if a significant peak found, calculate the frequency
            double frequency = TunerConfig.SAMPLE_RATE / fundamentalLag;
            // Check freq, remove if outside the range of any guitar string
            if (frequency < 65.0 || frequency > 450.0) {
                return 0.0;
            }
            return frequency;
        } else {
            // If no significant peak is found, return 0.0
            return 0.0;
        }
    }


    /**
     * Get the closest string to the detected frequencies
     *
     * @param freq double - the post-processing frequency
     * @return closestString int - the index of the string closest to the input frequency
     */
    public static int getClosestString(double freq) {

        double closestFreq = TunerConfig.stringFrequencies[0]; // Init to 'Low E' Frequency
        int closestString = 0; // Init to String Name 'Low E'

        // Loop through String Frequencies, find distance between currentDetectedFreq & currentIterFreq
        // Set ClosetString freq & String name
        for (int i = 1; i < TunerConfig.stringFrequencies.length; i++) {
            double distance = Math.abs(freq - TunerConfig.stringFrequencies[i]);

            if (distance < Math.abs(freq - closestFreq) && distance <= TunerConfig.LOCK_RADIUS_HZ) {
                closestFreq = TunerConfig.stringFrequencies[i];
                closestString = i;
            }
        }
        return closestString;
    }


    /**
     * Feedback loop to notify user if the string is too low, too high, or in tune
     *
     * @param detectedFreq double - the freq returned from the detectFrequency method
     */
    public static String matchStringFrequency(double detectedFreq) {

        // Find the closest string based on frequency
        double targetFreq = TunerConfig.stringFrequencies[TunerConfig.lockedStringIndex];
        String targetString = TunerConfig.getStringName(TunerConfig.lockedStringIndex);

        double difference = detectedFreq - targetFreq;

        // Determine tuning status
        if (Math.abs(difference) < 2.0) {
            return targetString + " is in tune!";
        } else if (difference < 0.0) {
            return targetString + " is too low!";
        } else {
            return targetString + " is too high!";
        }
    }


}
