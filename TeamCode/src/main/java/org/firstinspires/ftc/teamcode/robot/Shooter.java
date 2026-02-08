package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Subsystem controlling the dual-flywheel ball shooter and its tilt servo.
 *
 * <p>The shooter uses two {@link ShooterMotor} instances ({@code "shooterMotorLeft"} and
 * {@code "shooterMotorRight"}) spinning in opposite directions to launch balls, and a
 * servo ({@code "tiltServo"}) to adjust the launch angle for near vs. far targets.
 *
 * <p>Velocity control uses a voltage-based feedforward + proportional feedback loop with
 * exponential smoothing (see {@link #update}). The feedforward model is
 * {@code voltage = kS + kV * velocity}, and the feedback term corrects any error between
 * the smoothed target and actual velocities using gain {@link #kp}. The total voltage is
 * converted to motor power by dividing by the current battery voltage.
 *
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see Carousel
 */
@Config
public class Shooter {

    private final ShooterMotor left;
    private final ShooterMotor right;
    public final Servo tiltServo;
    private final Telemetry telemetry;
    private final VoltageSensor voltageSensor;

    /** Tilt servo position for near-target shots. */
    static public double nearTiltPosition = .03;
    /** Tilt servo position for far-target shots. */
    static public double farTiltPosition = .15;
    /** Tilt servo home (flat) position. */
    static public double homeTiltPosition = 0;

    /** Target flywheel velocity for near shots (ticks per second). */
    static public double nearShooterVelocity = 650;
    /** Target flywheel velocity for far shots (ticks per second). */
    static public double farShooterVelocity = 925;

    /** Current target velocity (ticks per second). Set before calling {@link #update}. */
    static public double shooterVelocity = 0;

    double smoothTargetShooterVelocity = 0;

    /** Proportional gain for the feedback term (volts per tick/sec error). */
    static public double kp = 0.002;
    /** Exponential smoothing factor (0..1) applied to the target velocity. */
    static public double targetSmoothingFactor = .1;
    /** Exponential smoothing factor (0..1) applied to actual motor velocities. */
    static public double actualSmoothingFactor = .1;

    /** Left motor static friction voltage (volts). From feedforward tuning. */
    static public double leftKS = 1.5109;
    /** Left motor velocity gain (volts per tick/sec). From feedforward tuning. */
    static public double leftKV = 0.005178;
    /** Right motor static friction voltage (volts). From feedforward tuning. */
    static public double rightKS = 1.3725;
    /** Right motor velocity gain (volts per tick/sec). From feedforward tuning. */
    static public double rightKV = 0.004908;

    /**
     * Constructs a Shooter subsystem and maps the motors and tilt servo from hardware.
     * The right motor is reversed so both flywheels spin inward.
     *
     * @param hardwareMap the robot's hardware map containing {@code "shooterMotorLeft"},
     *                    {@code "shooterMotorRight"}, and {@code "tiltServo"}
     * @param telemetry   telemetry instance for logging velocity diagnostics
     */
    public Shooter(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        left = new ShooterMotor(hardwareMap, "shooterMotorLeft", telemetry,
                DcMotorSimple.Direction.FORWARD);
        right = new ShooterMotor(hardwareMap, "shooterMotorRight", telemetry,
                DcMotorSimple.Direction.REVERSE);

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");
        voltageSensor = hardwareMap.voltageSensor.iterator().next();
    }

    /**
     * Runs one iteration of the feedforward + feedback velocity control loop.
     *
     * <p>Applies exponential smoothing to both the target and actual velocities, then
     * computes a proportional correction term added to the static feedforward power.
     * If {@link #shooterVelocity} is 0, both motors are stopped immediately.
     * Set {@link #shooterVelocity} before calling this method to change the target speed.
     */
    public void update() {
        telemetry.addData("Target T/S", shooterVelocity);

        //fail safe
        if (shooterVelocity == 0) {
            left.stop();
            right.stop();
            return;
        }
        //calculate smoothing
        smoothTargetShooterVelocity = (shooterVelocity * targetSmoothingFactor) + (1 - targetSmoothingFactor) * smoothTargetShooterVelocity;
        // An IIR filter approaches its target asymptotically (never truly arrives).
        // Snap to the exact target once we're within 1% to avoid lingering error.
        if (Math.abs(smoothTargetShooterVelocity - shooterVelocity) / shooterVelocity < 0.01) {
            smoothTargetShooterVelocity = shooterVelocity;
        }

        double batteryVoltage = voltageSensor.getVoltage();
        left.update(smoothTargetShooterVelocity, leftKS, leftKV, kp, actualSmoothingFactor, batteryVoltage);
        right.update(smoothTargetShooterVelocity, rightKS, rightKV, kp, actualSmoothingFactor, batteryVoltage);
    }


    /**
     * Sets the tilt servo to an arbitrary position.
     *
     * @param tiltPositon servo position (0.0 to 1.0)
     */
    public void setTiltPosition(double tiltPositon) {
        tiltServo.setPosition(tiltPositon);
    }

    /**
     * Sets the tilt servo to the near-target launch angle.
     *
     * @param nearTiltPosition servo position for near shots
     */
    public void setNearTiltPosition(double nearTiltPosition) {
        tiltServo.setPosition(nearTiltPosition);
    }

    /**
     * Sets the tilt servo to the far-target launch angle.
     *
     * @param farTiltPosition servo position for far shots
     */
    public void setFarTiltPosition(double farTiltPosition) {
        tiltServo.setPosition(farTiltPosition);
    }

    /**
     * Resets the tilt servo to the home (flat) position.
     */
    public void setHomeTiltPosition() {
        tiltServo.setPosition(homeTiltPosition);
    }

}
