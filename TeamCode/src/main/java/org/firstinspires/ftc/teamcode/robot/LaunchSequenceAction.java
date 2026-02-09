package org.firstinspires.ftc.teamcode.robot;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;


/**
 * Road Runner Actions port of {@link LaunchSequence}.
 *
 * <p>Instead of a manually-pumped state machine, this class builds a composable
 * {@link Action} that can be nested inside drive trajectories or run standalone.
 *
 * <p>Usage in Autonomous (blocking):
 * <pre>
 *   LaunchSequenceAction factory = new LaunchSequenceAction(shooter, carousel);
 *   Actions.runBlocking(factory.build(Shooter.nearShooterVelocity, Shooter.nearTiltPosition));
 * </pre>
 *
 * <p>Usage in Teleop (non-blocking, driven manually each loop):
 * <pre>
 *   LaunchSequenceAction factory = new LaunchSequenceAction(shooter, carousel);
 *   Action active = null;
 *
 *   // in loop:
 *   if (active == null &amp;&amp; triggerPressed) {
 *       active = factory.build(Shooter.nearShooterVelocity, Shooter.nearTiltPosition);
 *   }
 *   if (active != null &amp;&amp; !active.run(new TelemetryPacket())) {
 *       active = null;  // sequence finished
 *   }
 * </pre>
 *
 * @see LaunchSequence
 * @see Shooter
 * @see Carousel
 */
@Config
public class LaunchSequenceAction {

    private final Shooter shooter;
    private final Carousel carousel;

    static public double defaultStepDelay = 1.0;
    static public double tiltToLaunchDelay = 1.0;
    static public double rampUpDuration = 2.0;

    public LaunchSequenceAction(Shooter shooter, Carousel carousel) {
        this.shooter = shooter;
        this.carousel = carousel;
    }

    /**
     * Builds a new launch sequence Action for the given velocity and tilt.
     * Each call returns a fresh, single-use Action.
     *
     * @param velocity     target flywheel velocity (ticks per second)
     * @param tiltPosition tilt servo position for the shot
     */
    public Action build(double velocity, double tiltPosition) {
        return new SequentialAction(
                // RAMP UP: spin flywheels while waiting for them to reach speed
                new InstantAction(() -> shooter.shooterVelocity = velocity),
                new RaceAction(
                        shooterUpdateLoop(),
                        new SleepAction(rampUpDuration)
                ),

                // Setup: home tilt and kicker before first ball
                new InstantAction(() -> {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                }),

                // LAUNCHING: shooter velocity loop runs alongside all 3 ball sub-sequences.
                // RaceAction ends when the sequential ball steps finish;
                // the perpetual shooterUpdateLoop is stopped automatically.
                new RaceAction(
                        shooterUpdateLoop(),
                        new SequentialAction(
                                launchBall(1, tiltPosition, defaultStepDelay + 0.2),
                                resetBetweenLaunches(),
                                launchBall(2, tiltPosition, defaultStepDelay),
                                resetBetweenLaunches(),
                                launchBall(3, tiltPosition, defaultStepDelay)
                        )
                ),

                // CLEANUP: no shooter update — servos reset, then flywheels stop
                new SleepAction(defaultStepDelay + 0.2),
                new InstantAction(shooter::setHomeTiltPosition),
                new SleepAction(defaultStepDelay + 0.5),
                new InstantAction(carousel::setHomePositionKicker),
                new SleepAction(defaultStepDelay + 0.5),
                new InstantAction(() -> {
                    carousel.spinCarouselLaunch(1);
                    shooter.shooterVelocity = 0;
                    shooter.update();
                })
        );
    }

    /**
     * Perpetual action that pumps the shooter velocity controller every cycle.
     * Always returns {@code true} so it never finishes on its own — pair with
     * {@link RaceAction} so the sleep / step sequence terminates it.
     *
     * <p><b>Important:</b> place this <em>before</em> the terminating action
     * inside {@code RaceAction} so that {@code shooter.update()} is guaranteed
     * to run on the final cycle (RaceAction short-circuits after the first
     * child that returns {@code false}).
     */
    private Action shooterUpdateLoop() {
        return (@NonNull TelemetryPacket packet) -> {
            shooter.update();
            return true;
        };
    }

    /** Sub-sequence for one ball: rotate, seat, tilt, kick. */
    private Action launchBall(int ballNumber, double tiltPosition, double kickerDelay) {
        return new SequentialAction(
                new SleepAction(defaultStepDelay),
                carouselToLaunchPosition(ballNumber),
                new SleepAction(kickerDelay),
                new InstantAction(carousel::setTinyKicker),
                new SleepAction(defaultStepDelay),
                new InstantAction(() -> shooter.setTiltPosition(tiltPosition)),
                new SleepAction(tiltToLaunchDelay),
                new InstantAction(carousel::setFullKicker)
        );
    }

    /** Reset tilt and kicker between balls. */
    private Action resetBetweenLaunches() {
        return new SequentialAction(
                new SleepAction(defaultStepDelay),
                new InstantAction(() -> {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                })
        );
    }

    private Action carouselToLaunchPosition(int ballNumber) {
        return new InstantAction(() -> carousel.spinCarouselLaunch(ballNumber));
    }
}
