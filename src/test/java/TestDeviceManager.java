import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;

import javax.sound.sampled.Mixer;
import DeviceManager.*;


public class TestDeviceManager {

    @Test
    public void testValidGetUserSelectedRecordingDevice() {
        // Setup mock data
        Mixer.Info mock1 = Mockito.mock(Mixer.Info.class);
        Mockito.when(mock1.getName()).thenReturn("Mock1"); // Mock #1 Device

        Mixer.Info mock2 = Mockito.mock(Mixer.Info.class);
        Mockito.when(mock2.getName()).thenReturn("Mock2"); // Mock #2 Device

        Mixer.Info[] fakeDevices = new Mixer.Info[] { mock1, mock2 }; // Build array with mock devices

        Mixer.Info selected = DeviceManager.getUserSelectedRecordingDevice(1, fakeDevices);

        assertEquals(mock2, selected);
    }


    @Test
    public void testInvalidGetUserSelectedRecordingDevices() {
        Mixer.Info mock1 = Mockito.mock(Mixer.Info.class);
        Mixer.Info[] fakeDevices = new Mixer.Info[] { mock1};

        Mixer.Info selected = DeviceManager.getUserSelectedRecordingDevice(3, fakeDevices);

        assertNull(selected);
    }
}
