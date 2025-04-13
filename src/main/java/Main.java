
import DeviceManager.DeviceManager;
import TunerManager.Tuner;
import TunerUtils.TunerConfig;

import javax.sound.sampled.*;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);
        System.out.println("Welcome to Java Guitar Tuner!");

        // Get all available devices
        Mixer.Info[] availableDevices = DeviceManager.getAvailableDevices();

        // Print all available devices
        DeviceManager.listAvailableDevices(availableDevices);

        // Get user choice
        System.out.print("Select a recording device (1-" + availableDevices.length + "): ");
        int userChoice = Integer.parseInt(scnr.nextLine()) - 1;

        // Get Mixer info of user chosen device
        Mixer.Info chosenRecDevice = DeviceManager.getUserSelectedRecordingDevice(userChoice, availableDevices);

        // Validate user selection of recording device
        if (chosenRecDevice == null) {
            System.out.println("Please try a different device");
            System.exit(1);
        }

        // Get tuning select from user, return chosen tuning's frequencies, & set to stringFrequencies variable
        Tuner.listAvailableTunings();
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

                soundData.open(format);         // open data stream
                soundData.start();              // start listening

                // Main Tuner Loop
                Tuner.tuningLoop(soundData);

                soundData.stop();               // stop listening
                soundData.close();              // close stream
            } else {
                System.out.println("Microphone selected is not supported. Please choose a different device.");
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        scnr.close();
    }
}