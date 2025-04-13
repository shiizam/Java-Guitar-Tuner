package DeviceManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Scanner;

public class DeviceManager {

    public static Mixer.Info[] getAvailableDevices() {
        return AudioSystem.getMixerInfo();
    }

    /**
     * List all available devices
     *
     * @param availableDevices Mixer.Info[] - An array of device info available on the computer
     */
    public static void listAvailableDevices(Mixer.Info[] availableDevices) {
        System.out.print("Currently available recording devices:");
        for (int i = 0; i < availableDevices.length; i++) {
            System.out.println((i+1) + ": " + availableDevices[i].getName());
        }
    }

    /**
     * Validate user choice & get selected recording device.
     *
     * @param userChoice int -
     * @param recordingDevices Mixer.Info[] -
     *
     * @return Mixer.Info - The information about the chosen recording device
     */
    public static Mixer.Info getUserSelectedRecordingDevice(int userChoice, Mixer.Info[] recordingDevices) {

        // Validate user choice & return based on validation
        if (userChoice >= 0 && userChoice < recordingDevices.length) {
            return recordingDevices[userChoice];
        } else {
            System.out.println("Not a valid device selection. Returning null...");
            return null;
        }
    }
}
