import javax.sound.sampled.*;
import java.util.Scanner;

import org.jtransforms.fft.DoubleFFT_1D;


public class Main {
    // Raw data format specs
    private static final float SAMPLE_RATE = 48000.0f;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNEL_SIZE = 1;
    private static final int BUFFER_SIZE = 2048;

    private static final int NUM_SAMPLES_TO_AVG = 5;

    // Guitar String Frequencies
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
        //
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


//    public static double detectFrequency(double[] audioSamples) {
//        int size = audioSamples.length; // Determines the amount of calculations
//        double[] autocorrelation = new double[size]; // Will contain the lag values
//
//        // Step 1: Apply Hann window to reduce spectral leakage
//        for (int i = 0; i < size; i++) {
//            double hann = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
//            audioSamples[i] *= hann;
//        }
//
//        // Step 2: Compute the autocorrelation
//        for (int lag = 0; lag < size; lag++) {
//            for (int i = 0; i < size - lag; i++) {
//                autocorrelation[lag] += audioSamples[i] * audioSamples[i + lag];
//            }
//        }
//
//        // Step 3: Normalize the autocorrelation values
//        for (int lag = 0; lag < size; lag++) {
//            autocorrelation[lag] /= autocorrelation[0];
//        }
//
//        // Step 4: Find the first significant peak
//        int fundamentalLag = -1;
//        for (int lag = 100; lag < size - 1; lag++) {
//            if (autocorrelation[lag] > autocorrelation[lag - 1] && autocorrelation[lag] > autocorrelation[lag + 1]) {
//                fundamentalLag = lag;
//                break;
//            }
//        }
//
//        // Step 5: Convert lag to frequency
//        double frequency = SAMPLE_RATE / fundamentalLag;
//
//        if (frequency < 50 || frequency > 500) {
//            System.out.println("Discarding invalid frequency: " + frequency);
//            return -1;
//        }
//
//
//        // Step 6: Return frequency
//        return  frequency;
//    }

    public static double detectFrequency(double[] audioSamples) {
        int audioArrayLength = audioSamples.length;
        double[] fftBuffer = new double[audioArrayLength * 2];

        for (int i = 0; i < audioArrayLength; i++) {
            fftBuffer[i*2] = audioSamples[i];
        }

        DoubleFFT_1D fft = new DoubleFFT_1D(audioArrayLength);
        fft.realForwardFull(fftBuffer);

        // Compute Magnitude Spectrum
        double[] magnitudes = new double[audioArrayLength / 2];
        for (int i = 0; i < audioArrayLength / 2; i++) {
            double real = fftBuffer[2 * i];
            double imag = fftBuffer[2 * i + 1];
            magnitudes[i] = Math.sqrt(real * real * imag * imag);
        }

        return applyHPS(magnitudes);
    }

    public static double applyHPS(double[] magnitudes) {
        int maxIndex = magnitudes.length / 5;
        double[] hps = new double[maxIndex];

        for (int i = 0; i < maxIndex; i++) {
            hps[i] = magnitudes[i] * magnitudes[i * 2] * magnitudes[i * 3];
        }

        int bestIndex = 0;
        for (int i = 1; i < maxIndex; i++) {
            if (hps[i] > hps[bestIndex]) {
                bestIndex = i;
            }
        }

        return SAMPLE_RATE * bestIndex / magnitudes.length;
    }


    public static String matchStringFrequency(double detectedFreq) {
        double closestFreq = stringFrequencies[0];
        String closestString = "Low E";

        // Find the closest string based on frequency
        for (int i = 1; i < stringFrequencies.length; i++) {
            if (Math.abs(detectedFreq - stringFrequencies[i]) < Math.abs(detectedFreq - closestFreq)) {
                closestFreq = stringFrequencies[i];
                closestString = getStringName(i);
            }
        }

        // Determine tuning status
        if (Math.abs(detectedFreq - closestFreq) < 2) {
            return closestString + " is in tune!";
        } else if (detectedFreq < closestFreq) {
            return closestString + " is too low!";
        } else {
            return closestString + " is too high!";
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

                if (detectedFrequency > 0 && Math.abs(detectedFrequency - lastFreq) > 2) {
                    String userFeedback = matchStringFrequency(detectedFrequency);
                    System.out.println("Fundamental Frequency: " + detectedFrequency + " Hz -> " + userFeedback);
                    lastFreq += detectedFrequency;
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