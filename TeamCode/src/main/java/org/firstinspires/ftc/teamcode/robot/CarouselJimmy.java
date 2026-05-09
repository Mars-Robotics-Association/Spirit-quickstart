package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

/**
 * Subsystem controlling the ball carousel and kicker mechanism.
 *
 * <p>The carousel is a rotating platform with three ball slots spaced 120 degrees apart.
 * It uses two servos:
 * <ul>
 *   <li>{@code carouselServo} — rotates the carousel to position balls for intake or launch</li>
 *   <li>{@code kickerServo} — a teardrop-shaped arm that lifts balls from the carousel
 *       into the shooter</li>
 * </ul>
 *
 * <p>Intake positions are computed from degree offsets converted to servo values using
 * {@link #carouselPositionConversionFactor}. Launch positions use directly tuned values.
 *
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see ShooterJimmy
 */
@Config
public class CarouselJimmy {

    /** Lifts the kicker to feed a ball to the shooter. */
    private final Servo kickerServo;
    /** Rotates the carousel to position balls. */
    private final Servo carouselServo;

    // KICKER POSITIONS
    public static double kickerDownPosition   = .52;
    double kickerFullLiftPosition = .21;

    // CAROUSEL POSITIONS
    public static double carouselPositionHome = 0.0;

    public static double offSetAdjustment = 175;
    public static int    degreeRange      = 1667;
    public static double carouselPositionConversionFactor = (1.0) / (degreeRange);

    double carouselPositionIntakeOne   = carouselPositionConversionFactor * (0     + offSetAdjustment);
    double carouselPositionIntakeTwo   = carouselPositionConversionFactor * (120.0 + offSetAdjustment);
    double carouselPositionIntakeThree = carouselPositionConversionFactor * (240.0 + offSetAdjustment);

    double carouselPositionLaunchOne   = 0.13;
    double carouselPositionLaunchTwo   = 0.2;
    double carouselPositionLaunchThree = 0.06;

    /**
     * Constructs a Carousel subsystem and maps the servos from hardware.
     *
     * @param hardwareMap the robot's hardware map containing {@code "kickerServo"}
     *                    and {@code "carouselServo"}
     */
    public CarouselJimmy(HardwareMap hardwareMap) {
        kickerServo   = hardwareMap.get(Servo.class, "kickerServo");
        carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }

    /** Raises the kicker fully to launch a ball into the shooter. */
    public void setFullKicker() {
        kickerServo.setPosition(kickerFullLiftPosition);
    }

    /** Lowers the kicker to its resting (down) position. */
    public void setHomePositionKicker() {
        kickerServo.setPosition(kickerDownPosition);
    }

    /** Rotates the carousel to intake slot 1 (0 degrees + offset). */
    public void spinCarouselIntakeOne() {
        carouselServo.setPosition(carouselPositionIntakeOne);
    }

    /** Rotates the carousel to intake slot 2 (120 degrees + offset). */
    public void spinCarouselIntakeTwo() {
        carouselServo.setPosition(carouselPositionIntakeTwo);
    }

    /** Rotates the carousel to intake slot 3 (240 degrees + offset). */
    public void spinCarouselIntakeThree() {
        carouselServo.setPosition(carouselPositionIntakeThree);
    }

    /** Rotates the carousel to its home position. */
    public void spinCarouselHome() {
        carouselServo.setPosition(carouselPositionHome);
    }

    /** Rotates the carousel to launch slot 1. */
    public void spinCarouselLaunchOne() {
        carouselServo.setPosition(carouselPositionLaunchOne);
    }

    /** Rotates the carousel to launch slot 2. */
    public void spinCarouselLaunchTwo() {
        carouselServo.setPosition(carouselPositionLaunchTwo);
    }

    /** Rotates the carousel to launch slot 3. */
    public void spinCarouselLaunchThree() {
        carouselServo.setPosition(carouselPositionLaunchThree);
    }

    /**
     * Disables PWM on both servos so they stop holding position and can spin freely.
     * Uses ServoImplEx because the standard Servo interface does not expose PWM control.
     */
    public void disableCarouselAndKicker() {
        ((ServoImplEx) kickerServo).setPwmDisable();
        ((ServoImplEx) carouselServo).setPwmDisable();
    }

    /** Re-enables PWM on both servos so they resume holding position. */
    public void enableCarouselAndKicker() {
        ((ServoImplEx) kickerServo).setPwmEnable();
        ((ServoImplEx) carouselServo).setPwmEnable();
    }
}
