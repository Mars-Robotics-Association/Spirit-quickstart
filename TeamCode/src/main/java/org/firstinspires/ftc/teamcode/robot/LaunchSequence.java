package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;

import java.util.function.DoubleSupplier;

/**
 * Reusable 3-ball launch sequence shared by Teleop and Autonomous OpModes.
 *
 * <p>Encapsulates the timed state machine that ramps the shooter flywheels,
 * then loops through all three carousel positions — rotating, tilting, kicking,
 * and resetting for each ball — followed by a cleanup phase.
 *
 * <p>Usage:
 * <pre>
 *   LaunchSequence launch = new LaunchSequence(shooter, carousel, this::getRuntime);
 *   launch.start(Shooter.nearShooterVelocity, Shooter.nearTiltPosition);
 *   // in loop:
 *   launch.update();
 *   if (launch.isDone()) { launch.reset(); }
 * </pre>
 *
 * @see Shooter
 * @see Carousel
 */
@Config
public class LaunchSequence {
    private final Shooter shooter;
    private final Carousel carousel;
    private final DoubleSupplier clock;

    static public double defaultStepDelay = 0.5;
    static public double tiltToLaunchDelay = 1.0;
    static public double rampUpDuration = 1.0;

    private State state = State.IDLE;
    private int ballNumber;
    private int subStep;
    private double stepStartTime;
    private double rampUpDeadline;
    private double tiltPosition;

    private enum State {IDLE, RAMPING, LAUNCHING, CLEANUP, DONE}

    public LaunchSequence(Shooter shooter, Carousel carousel, DoubleSupplier clock) {
        this.shooter = shooter;
        this.carousel = carousel;
        this.clock = clock;
    }

    /**
     * Begin the launch sequence — starts flywheel ramp-up.
     */
    public void start(double velocity, double tiltPosition) {
        shooter.shooterVelocity = velocity;
        shooter.update();
        this.tiltPosition = tiltPosition;
        rampUpDeadline = clock.getAsDouble() + rampUpDuration;
        state = State.RAMPING;
    }

    /**
     * Advance the state machine. Call every loop iteration. No-op when IDLE or DONE.
     */
    public void update() {
        double now = clock.getAsDouble();

        switch (state) {
            case IDLE:
            case DONE:
                return;

            case RAMPING:
                shooter.update();
                if (now >= rampUpDeadline) {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                    stepStartTime = now;
                    ballNumber = 1;
                    subStep = 0;
                    state = State.LAUNCHING;
                }
                break;

            case LAUNCHING:
                shooter.update();
                launchStep(now);
                break;

            case CLEANUP:
                cleanupStep(now);
                break;
        }
    }

    private void launchStep(double now) {
        switch (subStep) {
            case 0: // Rotate carousel to launch position
                if (now - stepStartTime > defaultStepDelay) {
                    carousel.spinCarouselLaunch(ballNumber);
                    stepStartTime = now;
                    subStep++;
                }
                break;

            case 1: // Tiny kicker (extra +0.2s delay on first ball)
                double kickerDelay = (ballNumber == 1)
                        ? defaultStepDelay + 0.2
                        : defaultStepDelay;
                if (now - stepStartTime > kickerDelay) {
                    carousel.setTinyKicker();
                    stepStartTime = now;
                    subStep++;
                }
                break;

            case 2: // Tilt shooter
                if (now - stepStartTime > defaultStepDelay) {
                    shooter.setTiltPosition(tiltPosition);
                    stepStartTime = now;
                    subStep++;
                }
                break;

            case 3: // Full kicker (launch ball)
                if (now - stepStartTime > tiltToLaunchDelay) {
                    carousel.setFullKicker();
                    stepStartTime = now;
                    if (ballNumber == 3) {
                        // Last ball — skip reset, go to cleanup
                        subStep = 0;
                        state = State.CLEANUP;
                    } else {
                        subStep++;
                    }
                }
                break;

            case 4: // Reset tilt + kicker (first two balls only)
                if (now - stepStartTime > defaultStepDelay) {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                    stepStartTime = now;
                    ballNumber++;
                    subStep = 0;
                }
                break;
        }
    }

    private void cleanupStep(double now) {
        switch (subStep) {
            case 0: // Home tilt
                if (now - stepStartTime > defaultStepDelay + 0.2) {
                    shooter.setHomeTiltPosition();
                    stepStartTime = now;
                    subStep++;
                }
                break;
            case 1: // Home kicker
                if (now - stepStartTime > defaultStepDelay + 0.5) {
                    carousel.setHomePositionKicker();
                    stepStartTime = now;
                    subStep++;
                }
                break;
            case 2: // Reset carousel and stop shooter
                if (now - stepStartTime > defaultStepDelay + 0.5) {
                    carousel.spinCarouselLaunch(1);
                    shooter.shooterVelocity = 0;
                    shooter.update();
                    state = State.DONE;
                }
                break;
        }
    }

    /**
     * True after the full sequence (3 balls + cleanup) has finished.
     */
    public boolean isDone() {
        return state == State.DONE;
    }

    /**
     * True while ramping or launching (not idle, not done).
     */
    public boolean isRunning() {
        return state == State.RAMPING || state == State.LAUNCHING || state == State.CLEANUP;
    }

    /**
     * Return to IDLE so the sequence can be started again (for Teleop reuse).
     */
    public void reset() {
        state = State.IDLE;
    }
}
