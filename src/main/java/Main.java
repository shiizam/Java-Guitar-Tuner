import javax.sound.sampled.*;
import java.util.Scanner;


public class Main {
    // Raw data format specs
    private static final float SAMPLE_RATE = 48000.0f;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNEL_SIZE = 1;
    private static final int BUFFER_SIZE = 2048;


    // Guitar String Frequencies
    private static int lockedStringIndex = -1;
    private static final double LOCK_RADIUS_HZ = 6.0;
    private static final double UNLOCK_RADIUS_HZ = 10.0;
    private static final double[] stringFrequencies = {82.41, 110.00, 146.83, 196.00, 246.94, 329.63};

    // Get Guitar string name
    private static String getStringName(int index) {
        // Enhanced switch statement to return string names
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


    public static Mixer.Info getRecordingDevice(Scanner scnr) {

        // Get all recordingDevices
        Mixer.Info[] recordingDevices = AudioSystem.getMixerInfo();

        // Print all available devices
        System.out.print("Currently available recording devices:");
        for (int i = 0; i < recordingDevices.length; i++) {
            System.out.println((i+1) + ": " + recordingDevices[i].getName());
        }

        // Get user choice
        System.out.print("Select a recording device (1-" + recordingDevices.length + "): ");
        int userChoice = Integer.parseInt(scnr.nextLine()) - 1;

        // Validate user choice & return based on validation
        if (userChoice >= 0 && userChoice < recordingDevices.length) {
            return recordingDevices[userChoice];
        } else {
            System.out.println("Not a valid device selection. Returning null...");
            return null;
        }
    }


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

        // Step 4: Find first sig peak
        int fundamentalLag = -1;
        for (int lag = 1; lag < size - 1; lag++) {
            if (autocorrelation[lag] > autocorrelation[lag - 1] && autocorrelation[lag] > autocorrelation[lag + 1]) {
                fundamentalLag = lag;
                break;
            }
        }

        // Step 5: Convert Lag to Frequency
        if (fundamentalLag > 0) {
            // We found a significant peak, calculate the frequency
            double frequency = SAMPLE_RATE / fundamentalLag;

            // If the calculated frequency is too high (e.g., due to noise), use a fallback frequency
            if (frequency > SAMPLE_RATE / 2) { // Frequency should not exceed Nyquist limit
                return 0.0;
            }
            return frequency;
        } else {
            // If no significant peak is found, return 0.0 (or a default value)
            return 0.0;
        }
    }

    public static int getClosestString(double freq) {
        double closestFreq = stringFrequencies[0];
        int closestString = 0;

        for (int i = 1; i < stringFrequencies.length; i++) {
            double distance = Math.abs(freq - stringFrequencies[i]);

            if (distance < Math.abs(freq - closestFreq) && distance <= LOCK_RADIUS_HZ) {
                closestFreq = stringFrequencies[i];
                closestString = i;
            }
        }
        return closestString;
    }


    public static String matchStringFrequency(double detectedFreq) {

        // Find the closest string based on frequency
        double targetFreq = stringFrequencies[lockedStringIndex];
        String targetString = getStringName(lockedStringIndex);

        double difference = detectedFreq - targetFreq;


        // Determine tuning status

        if (Math.abs(difference) < 1) {
            return targetString + " is in tune!";
        } else if (difference < 0) {
            return targetString + " is too low!";
        } else {
            return targetString + " is too high!";
        }
    }

    public static void tuningLoop(TargetDataLine soundData) {
        double lastFreq = -1;

        while (true) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = soundData.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                double[] audioSamples = convertToAudioSamples(buffer, bytesRead);
                double detectedFrequency = detectFrequency(audioSamples);

                if (detectedFrequency > 0) {
                    if (lockedStringIndex == -1) {
                        lockedStringIndex = getClosestString(detectedFrequency);
                    } else {
                        double lockedFreq = stringFrequencies[lockedStringIndex];
                        if (Math.abs(detectedFrequency - lockedFreq) > UNLOCK_RADIUS_HZ) {
                            lockedStringIndex = -1;
                            continue;
                        }
                    }

                    // Show tuning feedback if currently 'Locked'
                    if (lockedStringIndex != -1) {
                        String feedback = matchStringFrequency(lockedStringIndex);
                        System.out.println("[" + getStringName(lockedStringIndex) + "] " + detectedFrequency + " Hz -> " + feedback);
                    }

                }
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println("Tuning interrupted!");
                System.exit(1);
            }
        }
    }


    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);
        System.out.println("Welcome to Java Guitar Tuner!");

        // User selects recording device
        Mixer.Info chosenRecDevice = getRecordingDevice(scnr);
        if (chosenRecDevice == null) {
            System.out.println("Please try a different device");
            System.exit(1); // For now, exit program if device == null
        }

        try {
            Mixer mixer = AudioSystem.getMixer(chosenRecDevice);
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNEL_SIZE,true, false); // Specify the expected format;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // Validates correct audio line chosen with desired format

            // Verify that chosen device is supported
            if (mixer.isLineSupported(info)) {
                TargetDataLine soundData = (TargetDataLine) mixer.getLine(info); // Cast the object to the specific type needed (TargetDataLine)

                soundData.open(format); // open data stream
                soundData.start();  // start listening

                // Tuning Process Loop
                tuningLoop(soundData);

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