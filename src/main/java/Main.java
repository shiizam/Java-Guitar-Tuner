import AudioManager.AudioManager;
import DeviceManager.DeviceManager;
import TunerManager.Tuner;
import TunerUtils.TunerConfig;

import javax.sound.sampled.*;
import java.util.Scanner;




public class Main {
//    // Raw data format specs
//    private static final float SAMPLE_RATE = 48000.0f;
//    private static final int SAMPLE_SIZE = 16;
//    private static final int CHANNEL_SIZE = 1;
//    private static final int BUFFER_SIZE = 8192;


//    // Guitar String Frequencies
//    private static int lockedStringIndex = -1;
//    private static final double LOCK_RADIUS_HZ = 6.0;
//    private static final double UNLOCK_RADIUS_HZ = 10.0;

//    private static double[] stringFrequencies = new double[6];

//    private static final String[] tuningTypes = {
//            "Standard (EADGBe)",
//            "Drop D (DADGBe)",
//            "Half Step Down (D#G#C#F#A#d#)"
//    };
//
//    private static final double[][] allTunings = {
//            {82.41, 110.00, 146.83, 196.00, 246.94, 329.63}, // Standard Tuning Freq
//            {73.42, 110.00, 146.83, 196.00, 246.94, 329.63},  // Drop 'D' Tuning Freq
//            {77.78, 103.83, 138.59, 185.00, 233.08, 311.13}  // Half Step Down Tuning
//    };

//    // Get Guitar string name
//    private static String getStringName(int index) {
//        // Enhanced switch statement to return string names
//        return switch (index) {
//            case 0 -> "Low E";
//            case 1 -> "A";
//            case 2 -> "D";
//            case 3 -> "G";
//            case 4 -> "B";
//            case 5 -> "High e";
//            default -> "Unknown";
//        };
//    }


//    /**
//    * Get user to select appropriate recording device.
//    *
//    * @param scnr Scanner - Scanner to get user choice
//    * @return Mixer.Info - The information about the chose recording device
//    */
//    public static Mixer.Info getRecordingDevice(Scanner scnr) {
//
//        // Get all recordingDevices
//        Mixer.Info[] recordingDevices = AudioSystem.getMixerInfo();
//
//        // Print all available devices
//        System.out.print("Currently available recording devices:");
//        for (int i = 0; i < recordingDevices.length; i++) {
//            System.out.println((i+1) + ": " + recordingDevices[i].getName());
//        }
//
//        // Get user choice
//        System.out.print("Select a recording device (1-" + recordingDevices.length + "): ");
//        int userChoice = Integer.parseInt(scnr.nextLine()) - 1;
//
//        // Validate user choice & return based on validation
//        if (userChoice >= 0 && userChoice < recordingDevices.length) {
//            return recordingDevices[userChoice];
//        } else {
//            System.out.println("Not a valid device selection. Returning null...");
//            return null;
//        }
//    }

//    /**
//     * Get users to select a specified tuning.
//     *
//     * @param scnr Scanner - Scanner to get user tuning selection
//     * @return allTunings[i] double[] - return the appropriate freq in the allTunings array for chosen tuning
//     */
//    public static double[] getUserTuning(Scanner scnr) {
//        System.out.println("Available tuning types:");
//        for (int i = 0; i < tuningTypes.length; i++) {
//            System.out.println((i+1) + ": " + tuningTypes[i]);
//        }
//
//        // Get User Choice
//        System.out.println("Select a Tuning (1-" + tuningTypes.length + "): ");
//        int userSelection = Integer.parseInt(scnr.nextLine()) - 1;
//
//        // Validate User Selection
//        if (userSelection >= 0 && userSelection < tuningTypes.length) {
//            return allTunings[userSelection];
//        } else {
//            System.out.println("Invalid selection. Defaulting to Standard tuning.");
//            return allTunings[0];
//        }
//    }


//     /**
//     * Convert raw data to audio samples. This is done by looping through the raw data and shifting the 8 bits
//     * on the far right to the left. To ensure the correct bits are moved, mask the right side bits with '0xFF'
//     *
//     * @param buffer byte[] - The buffer holding the raw audio data
//     * @param bytesRead int - The raw data captured by the listener
//     * @return audioSamples double[] - The converted audio data
//     */
//    public static double[] convertToAudioSamples(byte[] buffer, int bytesRead) {
//
//        double[] audioSamples = new double[bytesRead / 2];
//
//        for (int i = 0, j = 0; i < bytesRead - 1; i += 2, j++) {
//            // Shift high byte to the left by 8 bits
//            // mask the right side byte w/ '0xFF' to ensure it keeps right
//            int sample = (buffer[i+1] << 8) | (buffer[i] & 0xFF);
//
//            // '32768.0' is max possible value of a signed 16-bit integer
//            // which would give us the range of [-1, 1] (Normalization)
//            audioSamples[j] = sample / 32768.0;
//        }
//
//        return audioSamples;
//    }


//    /**
//     * Detect the frequency from the collected audio samples.
//     *
//     * @param audioSamples double[] - the converted audio samples
//     * @return frequency double - the frequency of the current sample
//     */
//    public static double detectFrequency(double[] audioSamples) {
//        int size = audioSamples.length;
//        double[] autocorrelation = new double[size];
//
//        // Step 1: Hann Window
//        for (int i = 0; i < size; i++) {
//            double hann = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
//            audioSamples[i] *= hann;
//        }
//
//        // Step 2: Compute Autocorrelation
//        for (int lag = 0; lag < size; lag++) {
//            for (int i = 0; i < size - lag; i++) {
//                autocorrelation[lag] += audioSamples[i] * audioSamples[i + lag];
//            }
//        }
//
//        // Step 3: Normalize
//        for (int lag = 0; lag < size; lag++) {
//            autocorrelation[lag] /= autocorrelation[0];
//        }
//
//        // Step 4: Find the sig peak
//
//        int minLag = (int)(SAMPLE_RATE / 400);
//        int maxLag = (int)(SAMPLE_RATE / 65);
//
//        double maxVal = -1;
//        int fundamentalLag = -1;
//
//        for (int lag = minLag; lag <  maxLag; lag++) {
//            if (autocorrelation[lag] > maxVal) {
//                maxVal = autocorrelation[lag];
//                fundamentalLag = lag;
//            }
//        }
//
//        // Remove 'noise' data
//        if (maxVal < 0.2) return 0.0;
//
//        // Step 5: Convert Lag to Frequency
//        if (fundamentalLag > 0) {
//            // if a significant peak found, calculate the frequency
//            double frequency = SAMPLE_RATE / fundamentalLag;
//            // Check freq, remove if outside the range of any guitar string
//            if (frequency < 65.0 || frequency > 450.0) {
//                return 0.0;
//            }
//            return frequency;
//        } else {
//            // If no significant peak is found, return 0.0
//            return 0.0;
//        }
//    }
//
//
//    /**
//     * Get the closest string to the detected frequencies
//     *
//     * @param freq double - the post-processing frequency
//     * @return closestString int - the index of the string closest to the input frequency
//     */
//    public static int getClosestString(double freq) {
//
//        double closestFreq = stringFrequencies[0]; // Init to 'Low E' Frequency
//        int closestString = 0; // Init to String Name 'Low E'
//
//        // Loop through String Frequencies, find distance between currentDetectedFreq & currentIterFreq
//        // Set ClosetString freq & String name
//        for (int i = 1; i < stringFrequencies.length; i++) {
//            double distance = Math.abs(freq - stringFrequencies[i]);
//
//            if (distance < Math.abs(freq - closestFreq) && distance <= LOCK_RADIUS_HZ) {
//                closestFreq = stringFrequencies[i];
//                closestString = i;
//            }
//        }
//        return closestString;
//    }
//
//
//    /**
//     * Feedback loop to notify user if the string is too low, too high, or in tune
//     *
//     * @param detectedFreq double - the freq returned from the detectFrequency method
//     */
//    public static String matchStringFrequency(double detectedFreq) {
//
//        // Find the closest string based on frequency
//        double targetFreq = stringFrequencies[lockedStringIndex];
//        String targetString = getStringName(lockedStringIndex);
//
//        double difference = detectedFreq - targetFreq;
//
//        // Determine tuning status
//        if (Math.abs(difference) < 2.0) {
//            return targetString + " is in tune!";
//        } else if (difference < 0.0) {
//            return targetString + " is too low!";
//        } else {
//            return targetString + " is too high!";
//        }
//    }

    
//    /**
//     * Main loop of the guitar tuner
//     *
//     * @param soundData TargetDataLine - The raw audio data captured by the Mixer listener
//     */
//    public static void tuningLoop(TargetDataLine soundData) {
//
//        while (true) {
//
//            byte[] buffer = new byte[BUFFER_SIZE]; // Buffer to hold audio data
//            int bytesRead = soundData.read(buffer, 0, buffer.length); // Listen for audio data
//
//            if (bytesRead > 0) {
//                double[] audioSamples = convertToAudioSamples(buffer, bytesRead); // Convert audio data from raw data
//                double detectedFrequency = detectFrequency(audioSamples); //
//
//                if (detectedFrequency > 0) {
//                    if (lockedStringIndex == -1) {
//                        lockedStringIndex = getClosestString(detectedFrequency);
//                    } else {
//                        double lockedFreq = stringFrequencies[lockedStringIndex];
//                        if (Math.abs(detectedFrequency - lockedFreq) > UNLOCK_RADIUS_HZ) {
//                            lockedStringIndex = -1;
//                            continue;
//                        }
//                    }
//
//                    // Show tuning feedback if currently 'Locked'
//                    if (lockedStringIndex != -1) {
//                        String feedback = matchStringFrequency(detectedFrequency);
//                        System.out.println("[" + getStringName(lockedStringIndex) + "] " + detectedFrequency + " Hz -> " + feedback);
//                    }
//
//                }
//            }
//
//            // Small delay to stop from flooding the console
//            try {
//                Thread.sleep(300);
//            } catch (InterruptedException e) {
//                System.out.println("Tuning interrupted!");
//                System.exit(1);
//            }
//        }
//    }


    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);
        System.out.println("Welcome to Java Guitar Tuner!");

        // Allow User to select recording device
        Mixer.Info chosenRecDevice = DeviceManager.getRecordingDevice(scnr);
        if (chosenRecDevice == null) {
            System.out.println("Please try a different device");
            System.exit(1);
        }

        // Get tuning select from user, return chosen tuning's frequencies, & set to stringFrequencies variable
        TunerConfig.stringFrequencies = Tuner.getUserTuning(scnr);

        try {
            Mixer mixer = AudioSystem.getMixer(chosenRecDevice); // Get correct recording device
            AudioFormat format = new AudioFormat(TunerConfig.SAMPLE_RATE, TunerConfig.SAMPLE_SIZE, TunerConfig.CHANNEL_SIZE,true, false); // Specify/set the expected format;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // Validates correct audio line chosen with desired format

            /*
            * Verify that chosen device is supported
            * Cast the raw audio to 'TargetDataLine' type
            * Open/Start listening, Run Main Loop
            * Out of loop, stop/close the listener
            */
            if (mixer.isLineSupported(info)) {
                TargetDataLine soundData = (TargetDataLine) mixer.getLine(info); // Cast the object to the specific type needed (TargetDataLine)

                soundData.open(format); // open data stream
                soundData.start();  // start listening

                Tuner.tuningLoop(soundData);  // Tuning Process Loop

                soundData.stop();   // stop listening
                soundData.close();  // close stream

            } else {
                System.out.println("Microphone selected is not supported. Please choose a different device.");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}