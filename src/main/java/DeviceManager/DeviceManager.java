package DeviceManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Scanner;

public class DeviceManager {

    /**
     * Get user to select appropriate recording device.
     *
     * @param scnr Scanner - Scanner to get user choice
     * @return Mixer.Info - The information about the chose recording device
     */
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

}
