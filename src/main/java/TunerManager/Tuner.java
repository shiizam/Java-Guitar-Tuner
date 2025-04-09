package TunerManager;


import javax.sound.sampled.TargetDataLine;
import java.util.Scanner;

import AudioManager.AudioManager;
import TunerUtils.TunerConfig;

public class Tuner {

    /**
     * Get users to select a specified tuning.
     *
     * @param scnr Scanner - Scanner to get user tuning selection
     * @return allTunings[i] double[] - return the appropriate freq in the allTunings array for chosen tuning
     */
    public static double[] getUserTuning(Scanner scnr) {
        System.out.println("Available tuning types:");
        for (int i = 0; i < TunerConfig.tuningTypes.length; i++) {
            System.out.println((i+1) + ": " + TunerConfig.tuningTypes[i]);
        }

        // Get User Choice
        System.out.println("Select a Tuning (1-" + TunerConfig.tuningTypes.length + "): ");
        int userSelection = Integer.parseInt(scnr.nextLine()) - 1;

        // Validate User Selection
        if (userSelection >= 0 && userSelection < TunerConfig.tuningTypes.length) {
            return TunerConfig.allTunings[userSelection];
        } else {
            System.out.println("Invalid selection. Defaulting to Standard tuning.");
            return TunerConfig.allTunings[0];
        }
    }

    /**
     * Main loop of the guitar tuner
     *
     * @param soundData TargetDataLine - The raw audio data captured by the Mixer listener
     */
    public static void tuningLoop(TargetDataLine soundData) {

        while (true) {

            byte[] buffer = new byte[TunerConfig.BUFFER_SIZE]; // Buffer to hold audio data
            int bytesRead = soundData.read(buffer, 0, buffer.length); // Listen for audio data

            if (bytesRead > 0) {
                double[] audioSamples = AudioManager.convertToAudioSamples(buffer, bytesRead); // Convert audio data from raw data
                double detectedFrequency = AudioManager.detectFrequency(audioSamples); //

                if (detectedFrequency > 0) {
                    if (TunerConfig.lockedStringIndex == -1) {
                        TunerConfig.lockedStringIndex = AudioManager.getClosestString(detectedFrequency);
                    } else {
                        double lockedFreq = TunerConfig.stringFrequencies[TunerConfig.lockedStringIndex];
                        if (Math.abs(detectedFrequency - lockedFreq) > TunerConfig.UNLOCK_RADIUS_HZ) {
                            TunerConfig.lockedStringIndex = -1;
                            continue;
                        }
                    }

                    // Show tuning feedback if currently 'Locked'
                    if (TunerConfig.lockedStringIndex != -1) {
                        String feedback = AudioManager.matchStringFrequency(detectedFrequency);
                        System.out.println("[" + TunerConfig.getStringName(TunerConfig.lockedStringIndex) + "] " + detectedFrequency + " Hz -> " + feedback);
                    }

                }
            }

            // Small delay to stop from flooding the console
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println("Tuning interrupted!");
                System.exit(1);
            }
        }
    }


}
