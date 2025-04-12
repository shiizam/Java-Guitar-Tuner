import TunerUtils.TunerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org .junit.jupiter.api.Assertions.*;

import AudioManager.*;



public class TestAudioManager {

    // Test 'detectFrequency' method for Tuning Frequencies
    @ParameterizedTest(name= "{index} => expectedFreq={0}")
    @CsvSource({
            "82.41",    // E2 (Low 'E')
            "110.0",    // A2
            "146.83",   // D3
            "196.00",   // G3
            "246.94",   // B3
            "329.63",   // E4 (High 'e')
            // Drop D Frequencies (Only including the frequency that hasn't been tested yet)
            "73.42",    // D2
            // Half Step Down Frequencies
            "77.78",    // Eb2
            "103.83",   // Ab2
            "138.59",   // Db3
            "185.00",   // Gb3
            "233.08",   // Bb3
            "311.13"    // Eb4
    })

    public void testDetectFrequency_allTuningFrequencies(double expectedFreq) {
        double sampleRate = TunerConfig.SAMPLE_RATE;
        int bufferSize = TunerConfig.BUFFER_SIZE;
        double[] samples = new double[bufferSize];

        for (int i = 0; i < bufferSize; i++) {
            samples[i] = Math.sin(2 * Math.PI * expectedFreq * i / sampleRate);
        }

        double detected = AudioManager.detectFrequency(samples);

        assertEquals(expectedFreq, detected, 2.0, "Detected frequency should be within ±2 Hz of the expected");
    }
}
