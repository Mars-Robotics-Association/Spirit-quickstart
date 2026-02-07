package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Subsystem that uses a REV Color/Distance Sensor V2 to detect the alliance color.
 *
 * <p>Reads the red and blue channels from the sensor named {@code "colorSensor"}
 * and returns the detected alliance via {@link #getAlliance()}.
 */
public class AllianceSensor {

    public enum Alliance { RED, BLUE }

    private final ColorSensor colorSensor;

    public AllianceSensor(HardwareMap hardwareMap) {
        colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
    }

    /** Returns the currently detected alliance color. */
    public Alliance getAlliance() {
        return colorSensor.blue() > colorSensor.red() ? Alliance.BLUE : Alliance.RED;
    }

    /** Returns the raw red channel value. */
    public int getRed() {
        return colorSensor.red();
    }

    /** Returns the raw blue channel value. */
    public int getBlue() {
        return colorSensor.blue();
    }
}
