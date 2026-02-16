package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.tuning.FlywheelsFeedforwardTuning;

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
    public static class DummyMotor {
        private final ShooterMotor shooterMotor;

        private DummyMotor(ShooterMotor shooterMotor) {
            this.shooterMotor = shooterMotor;
        }

        @Deprecated
        public void setPower(double power) {
            // no-op. Only here to avoid more diffs in the opmodes
        }

        @Deprecated
        public double getVelocity() {
            return shooterMotor.actualVelocity;
        }
    }

    @Deprecated
    public final double shooterPower = 1;
    @Deprecated
    public double smoothActualLeftShooterVelocity;
    @Deprecated
    public double smoothActualRightShooterVelocity;
    @Deprecated
    public double actualMotorPower;


    private final ShooterMotor left;
    private final ShooterMotor right;
    @Deprecated
    public final DummyMotor shooterMotorLeft;
    @Deprecated
    public final DummyMotor shooterMotorRight;
    public final Servo tiltServo;
    private final Telemetry telemetry;

    /**
     * Tilt servo position for near-target shots.
     */
    static public double nearTiltPosition = .03;
    /**
     * Tilt servo position for far-target shots.
     */
    static public double farTiltPosition = .15;
    /**
     * Tilt servo home (flat) position.
     */
    static public double homeTiltPosition = 0;

    /*The near target as measured by tachometer is between 1700 and 1900 rpm so say 1800 (as measured by Mr. Beckstead on 1/21/2026).
    The far target as measured by tachometer is between 3600 and 3700 rpm so say 3650

    So, velocity for near target = (1800 rotations per minute /28 ticks per seconds)/60 seconds = 840 tps
    So, velocity for far target = (3650 rotations per second/28 ticks per seconds)/60 seconds = 1,703 tps

       */
    static public double nearShooterVelocity = 650;//tps for use with encoders to set shooter speed
    static public double farShooterVelocity = 945;//tps for use with encoders to set shooter speed

    /**
     * Current target velocity (ticks per second). Set before calling {@link #update}.
     */
    static public double shooterVelocity = 0;

    /**
     * Maximum velocity error (ticks/sec) for {@link #isReady()} to return true.
     */
    static public double readyThreshold = 25;

    /**
     * Proportional gain for the feedback term (volts per tick/sec error).
     */
    static public double kp = 0.002;
    /**
     * The cutoffHz used for the first order IIR LPF of motor measurement
     */
    static public double cutoffHz = 8;

    /**
     * Left motor static friction voltage (volts). From feedforward tuning.
     */
    static public double leftKS = 0.6647;
    /**
     * Left motor velocity gain (volts per tick/sec). From feedforward tuning.
     */
    static public double leftKV = 12.5 / 2377.3;
    /**
     * Left motor acceleration gain (volts per tick/sec²). From feedforward tuning.
     */
    static public double leftKA = 12.5 / 2380.5;
    /**
     * Right motor static friction voltage (volts). From feedforward tuning.
     */
    static public double rightKS = 0.8157;
    /**
     * Right motor velocity gain (volts per tick/sec). From feedforward tuning.
     */
    static public double rightKV = 12.5 / 2742.4;
    /**
     * Right motor acceleration gain (volts per tick/sec²). From feedforward tuning.
     */
    static public double rightKA = 12.5 / 1835.8;

    // ── Setpoint profile ────────────────────────────────────────────────
    /**
     * Maximum profiled acceleration in ticks/s².
     * <p>
     * The physical upper bound from a standstill is
     * {@code (V_battery - kS) / kA}.  Derate to ~80 % of that value
     * (using a conservative battery voltage, e.g. 11 V) to leave headroom
     * for the feedback term and for battery sag during a match:
     * <pre>
     *   maxAccelTPS2 ≈ 0.8 * (11.0 - kS) / kA
     * </pre>
     * Setting this higher than the physical limit causes the profile to
     * outrun the motor and forces the feedback controller to compensate.
     */
    public static double maxAccelTPS2 = Math.min((11.0 - leftKS) / leftKA, (11.0 - rightKS) / rightKA);
    /**
     * Exponential approach time constant (seconds).  Controls how smoothly
     * the profiled setpoint settles onto the target velocity.
     * <p>
     * Set this to {@code kA / kV} — the motor's physical time constant,
     * which {@link FlywheelsFeedforwardTuning}
     * already reports as {@code avgTau}.  This makes the profile match the
     * motor's natural dynamics so the feedforward does most of the work.
     * Increase beyond {@code kA / kV} if the feedforward fit was noisy
     * (low R² or few valid step-response trials).
     */
    public static double approachTau = Math.max(leftKA / leftKV, rightKA / rightKV);

    /**
     * Maximum elapsed time (seconds) applied to the setpoint profile per
     * call to {@link #update}.
     * <p>
     * If the caller does not invoke {@code update()} every loop iteration
     * (e.g. the OpMode only calls it inside certain state-machine steps),
     * the wall-clock {@code dt} can grow to seconds.  Without a cap the
     * profile would overshoot wildly
     * ({@code profiledVelocity += maxAccel * hugeΔt}).
     * <p>
     * Capping {@code dt} means the profile advances at most one
     * "normal-sized" step regardless of how long the gap was — the
     * velocity filter still uses the true {@code dt} so its estimate
     * snaps to the latest encoder reading correctly.
     */
    public static double maxProfileDt = 0.060;

    private long lastTimeNanos;
    private double profiledVelocity;
    private boolean stopped = true;

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

        double voltage = hardwareMap.voltageSensor.iterator().next().getVoltage();

        left = new ShooterMotor(hardwareMap, "shooterMotorLeft", telemetry,
                DcMotorSimple.Direction.FORWARD, voltage);
        right = new ShooterMotor(hardwareMap, "shooterMotorRight", telemetry,
                DcMotorSimple.Direction.REVERSE, voltage);
        shooterMotorLeft = new DummyMotor(left);
        shooterMotorRight = new DummyMotor(right);

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");
    }

    /**
     * This is purely here to support the old way of using Shooter.
     * The better way is to pass telemetry so that {@link Shooter} and
     * {@link ShooterMotor} can do their own logging.
     *
     * @param hardwareMap
     */
    @Deprecated
    public Shooter(HardwareMap hardwareMap) {
        this(hardwareMap, null);
    }

    /**
     * Does nothing except call update. The new calling convention
     * is to optionally set {@link #shooterVelocity}, and then call {@link #update()}.
     * {@link #update} should run on every control loop iteration.
     *
     * @param shooterVelocity ignored
     * @param telemetry       ignored
     */
    @Deprecated
    public void setShooterVelocity(double shooterVelocity, Telemetry telemetry) {
        update();
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
        if (telemetry != null)
            telemetry.addData("shooterVelocity", shooterVelocity);

        // ── Timing ──────────────────────────────────────────────────────
        long now = System.nanoTime();
        if (lastTimeNanos == 0) {
            lastTimeNanos = now;
            return;
        }
        double dt = (now - lastTimeNanos) / 1e9;
        lastTimeNanos = now;
        if (dt < 1e-6) return;

        // ── Velocity filter (always runs so readings stay fresh) ─────────
        // Exact discrete-time first-order LPF: alpha = 1 - exp(-dt/tau).
        // This keeps the effective cutoff frequency constant regardless of loop rate.
        double filterTau = 1.0 / (2.0 * Math.PI * cutoffHz);
        double alpha = 1.0 - Math.exp(-dt / filterTau);
        left.updateFilter(alpha);
        right.updateFilter(alpha);

        smoothActualLeftShooterVelocity = left.smoothActualVelocity;
        smoothActualRightShooterVelocity = right.smoothActualVelocity;

        // ── Coast when target is zero ────────────────────────────────────
        if (shooterVelocity == 0) {
            left.cutPower();
            right.cutPower();
            actualMotorPower = 0;
            stopped = true;
            return;
        }

        // ── Seed profile from coasting velocity on resume ────────────────
        // Reset the filter so the profile and feedback start from a clean
        // encoder reading, not a stale smoothed value from before the gap.
        if (stopped) {
            left.resetFilter();
            right.resetFilter();
            profiledVelocity = Math.max(left.smoothActualVelocity,
                    right.smoothActualVelocity);
            stopped = false;
        }

        // ── Clamped-exponential setpoint profile ─────────────────────────
        // Far from target: acceleration clamped at maxAccelTPS2 (linear ramp).
        // Close to target: acceleration = error/tau, decaying smoothly to zero.
        // The transition is continuous in acceleration — no step change for kA.
        //
        // Cap dt for the profile so a gap between update() calls doesn't
        // cause a massive overshoot.  The velocity filter above still uses
        // the true dt so its estimate stays accurate.
        double profileDt = Math.min(dt, maxProfileDt);
        double error = shooterVelocity - profiledVelocity;
        double rawAccel = error / approachTau;
        double accel = Math.max(-maxAccelTPS2, Math.min(maxAccelTPS2, rawAccel));
        profiledVelocity += accel * profileDt;

        if (telemetry != null){
            telemetry.addData("profile accel", "%.1f", accel);
            telemetry.addData("profile velocity", "%.1f", profiledVelocity);
            if (dt > maxProfileDt)
                telemetry.addData("profile dt CAPPED", "%.3f -> %.3f", dt, profileDt);
        }

        // Snap to target once negligibly close (avoids asymptotic creep)
        if (Math.abs(shooterVelocity - profiledVelocity) < 1.0) {
            profiledVelocity = shooterVelocity;
            accel = 0;
        }

        left.update(profiledVelocity, accel, leftKS, leftKV, leftKA, kp);
        right.update(profiledVelocity, accel, rightKS, rightKV, rightKA, kp);
        actualMotorPower = left.actualMotorPower + right.actualMotorPower / 2.0;
    }

    /**
     * Returns true when the shooter is up to speed and ready to fire.
     * Requires the acceleration profile to have settled and both motors'
     * smoothed velocities to be within {@link #readyThreshold} of the target.
     */
    public boolean isReady() {
        return shooterVelocity != 0
                && profiledVelocity == shooterVelocity
                && Math.abs(left.smoothActualVelocity - shooterVelocity) < readyThreshold
                && Math.abs(right.smoothActualVelocity - shooterVelocity) < readyThreshold;
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
