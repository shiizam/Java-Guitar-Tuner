package TunerManager;

import javax.sound.sampled.TargetDataLine;

import AudioManager.AudioManager;
import TunerUtils.TunerConfig;


public class Tuner {

    // Object to hold buffer & bytesRead
    private static class AudioChunk {
        byte[] buffer;
        int bytesRead;

        AudioChunk(byte[] buffer, int bytesRead) {
            this.buffer = buffer;
            this.bytesRead = bytesRead;
        }
    }

    /**
     * Main loop of the guitar tuner
     * @param soundData TargetDataLine - The raw audio data captured by the Mixer listener
     */
    public static void tuningLoop(TargetDataLine soundData) {
        while (true) {
            AudioChunk audioChunk = captureAudio(soundData);            // Set buffer & capture audio

            if (audioChunk.bytesRead > 0) {
                double detectedFrequency = processAudio(audioChunk);    // Convert raw audio to formatted audio samples

                if (detectedFrequency > 0) {
                    handleFrequencyLocking(detectedFrequency);          // Lock/unlock current string being tuned
                    provideFeedback(detectedFrequency);                 // Show tuning feedback if currently 'Locked'
                }
            }
            printDelay();                                               // Small delay to stop from flooding the console
        }
    }


    private static AudioChunk captureAudio(TargetDataLine soundData) {
        byte[] buffer = new byte[TunerConfig.BUFFER_SIZE];              // Buffer to hold audio data
        int bytesRead = soundData.read(buffer, 0, buffer.length);   // Listen for audio data
        return new AudioChunk(buffer, bytesRead);
    }


    private static double processAudio(AudioChunk audioChunk) {
        double[] audioSamples = AudioManager.convertToAudioSamples(audioChunk.buffer, audioChunk.bytesRead); // Convert audio data from raw data
        return AudioManager.detectFrequency(audioSamples);
    }


    private static void handleFrequencyLocking(double detectedFrequency) {
        if (TunerConfig.lockedStringIndex == -1) {
            TunerConfig.lockedStringIndex = AudioManager.getClosestString(detectedFrequency);
        } else {
            double lockedFreq = TunerConfig.stringFrequencies[TunerConfig.lockedStringIndex];
            if (Math.abs(detectedFrequency - lockedFreq) > TunerConfig.UNLOCK_RADIUS_HZ) {
                TunerConfig.lockedStringIndex = -1;
            }
        }
    }


    private static void provideFeedback(double detectedFrequency) {
        if (TunerConfig.lockedStringIndex != -1) {
            String feedback = AudioManager.matchStringFrequency(detectedFrequency);
            System.out.println("[" + TunerConfig.getStringName(TunerConfig.lockedStringIndex) + "] " + detectedFrequency + " Hz -> " + feedback);
        }
    }


    public static void listAvailableTunings() {
        System.out.println("Available tuning types:");
        for (int i = 0; i < TunerConfig.tuningTypes.length; i++) {
            System.out.println((i+1) + ": " + TunerConfig.tuningTypes[i]);
        }
    }


    /**
     * Get users to select a specified tuning.
     *
     * @param tuningChoice int - User selected tuning choice
     * @return allTunings[i] double[] - return the appropriate freq in the allTunings array for chosen tuning
     */
    public static double[] getUserTuning(int tuningChoice) {
        if (tuningChoice >= 0 && tuningChoice < TunerConfig.tuningTypes.length) {
            return TunerConfig.allTunings[tuningChoice];
        } else {
            System.out.println("Invalid selection. Defaulting to Standard tuning.");
            return TunerConfig.allTunings[0];
        }
    }


    static void printDelay() {
        // Small delay to stop from flooding the console
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            System.out.println("Tuning interrupted!");
            System.exit(1);
        }
    }
}
