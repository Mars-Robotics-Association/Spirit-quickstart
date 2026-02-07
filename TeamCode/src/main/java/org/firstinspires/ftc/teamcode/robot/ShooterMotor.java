package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
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
    public double smoothActualVelocity = 0;
    public double actualMotorPower = 0;
    private double lastSetPower = 0;

    /**
     * Constructs a ShooterMotor, looking up the motor from the hardware map.
     *
     * @param hardwareMap the robot's hardware map
     * @param name        the hardware-map name for this motor (also used in telemetry)
     * @param telemetry   telemetry instance for logging diagnostics
     * @param direction   the spin direction for this motor
     */
    public ShooterMotor(HardwareMap hardwareMap, String name, Telemetry telemetry,
                        DcMotorSimple.Direction direction) {
        this.name = name;
        this.telemetry = telemetry;
        this.motor = hardwareMap.get(DcMotorEx.class, name);
        this.motor.setDirection(direction);
    }

    /**
     * Runs one iteration of smoothing + proportional feedback for this motor and sets power.
     * Reports velocity, smoothing, feedback, and power telemetry using the motor name.
     *
     * @param targetVelocity  the target velocity (ticks/sec)
     * @param feedForward     the static feedforward power for this motor
     * @param kp              proportional gain
     * @param smoothingFactor exponential smoothing factor (0..1)
     */
    public void update(double targetVelocity, double feedForward, double kp, double smoothingFactor) {
        double actualVelocity = motor.getVelocity();
        smoothActualVelocity = (actualVelocity * smoothingFactor) + (1 - smoothingFactor) * smoothActualVelocity;

        double feedback = (targetVelocity - smoothActualVelocity) * kp;
        setPower(feedback + feedForward);

        telemetry.addData(name + " Actual Velocity", actualVelocity);
        telemetry.addData(name + " Smooth Velocity", smoothActualVelocity);
        telemetry.addData(name + " Feed Forward", feedForward);
        telemetry.addData(name + " Feedback", feedback);
        telemetry.addData(name + " Motor Power", actualMotorPower);
    }

    /**
     * Quantizes power to {@link #powerQuantum} and writes to the motor only if changed.
     */
    private void setPower(double power) {
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
    public void stop() {
        setPower(0);
    }
}
