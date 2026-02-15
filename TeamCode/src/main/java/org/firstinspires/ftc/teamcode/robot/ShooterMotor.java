package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Encapsulates per-motor state and the velocity control loop for a single flywheel motor.
 *
 * <p>Each instance tracks its own smoothed actual velocity and computes a
 * feedforward + proportional feedback power output. Tunable gains are passed in
 * from {@link Shooter}'s {@code @Config} static fields each loop iteration.
 *
 * <p>Telemetry diagnostics are reported automatically during {@link #update},
 * using the hardware name to distinguish left from right.
 */
@Config
public class ShooterMotor {

    static public double powerQuantum = 0.01;

    private final DcMotorEx motor;
    private final String name;
    private final Telemetry telemetry;
    public double actualVelocity = 0;
    public double smoothActualVelocity = 0;
    public double actualMotorPower = 0;
    private double lastSetPower = 0;
    private final double batteryVoltage;

    /**
     * Constructs a ShooterMotor, looking up the motor from the hardware map.
     *
     * @param hardwareMap the robot's hardware map
     * @param name        the hardware-map name for this motor (also used in telemetry)
     * @param telemetry   telemetry instance for logging diagnostics
     * @param direction   the spin direction for this motor
     */
    public ShooterMotor(HardwareMap hardwareMap, String name, Telemetry telemetry,
                        DcMotorSimple.Direction direction, double batteryVoltage) {
        this.name = name;
        this.telemetry = telemetry;
        this.motor = hardwareMap.get(DcMotorEx.class, name);
        this.motor.setDirection(direction);
        this.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.batteryVoltage = batteryVoltage;
    }

    /**
     * Reads the encoder velocity and updates the smoothed velocity estimate.
     * Call every loop iteration (even when the shooter is off) so the filter
     * stays current for a smooth resume from coasting.
     *
     * @param alpha IIR low-pass filter coefficient (0..1)
     */
    public void updateFilter(double alpha) {
        actualVelocity = motor.getVelocity();
        smoothActualVelocity = (actualVelocity * alpha) + (1 - alpha) * smoothActualVelocity;
    }

    /**
     * Runs one iteration of the voltage-based feedforward + feedback loop for this motor.
     * {@link #updateFilter} must be called first each loop to refresh the velocity estimate.
     *
     * <p>The feedforward model is {@code voltage = kS + kV * targetVelocity + kA * targetAccel},
     * matching the model from {@code FlywheelsFeedforwardTuning}. The feedback term
     * {@code kP * (target - actual)} is also in volts. The total voltage is converted
     * to a duty-cycle power by dividing by the current battery voltage.
     *
     * @param targetVelocity the target velocity (ticks/sec)
     * @param targetAccel    the target acceleration (ticks/sec²)
     * @param kS             static friction voltage (volts)
     * @param kV             velocity feedforward gain (volts per tick/sec)
     * @param kA             acceleration feedforward gain (volts per tick/sec²)
     * @param kp             proportional gain (volts per tick/sec error)
     */
    public void update(double targetVelocity, double targetAccel, double kS,
                       double kV, double kA, double kp) {
        double feedforward = kS * Math.signum(targetVelocity) + kV * targetVelocity + kA * targetAccel;
        double feedback = kp * (targetVelocity - smoothActualVelocity);
        double voltage = feedforward + feedback;
        setPowerInternal(voltage / batteryVoltage);

        if (telemetry != null) {
            telemetry.addData(name + " Actual Velocity", "%.0f", actualVelocity);
            telemetry.addData(name + " Smooth Velocity", "%.0f", smoothActualVelocity);
            telemetry.addData(name + " Feedforward (V)", "%.1f", feedforward);
            telemetry.addData(name + " Feedback (V)", "%.1f", feedback);
            telemetry.addData(name + " Motor Power", "%.2f", actualMotorPower);
        }
    }

    /**
     * Quantizes power to {@link #powerQuantum} and writes to the motor only if changed.
     */
    private void setPowerInternal(double power) {
        double scale = 1.0 / powerQuantum;
        actualMotorPower = Math.round(power * scale) / scale;
        if (actualMotorPower != lastSetPower) {
            motor.setPower(actualMotorPower);
            lastSetPower = actualMotorPower;
        }
    }

    /**
     * Stops the motor by setting power to 0.
     */
    public void cutPower() {
        setPowerInternal(0);
    }
}
