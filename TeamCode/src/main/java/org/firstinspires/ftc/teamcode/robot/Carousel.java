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
 * <p>Carousel positions are computed from degree offsets converted to servo values using
 * {@link #carouselPositionConversionFactor}. There are three intake positions (where balls
 * are loaded in) and three launch positions (where balls are fed to the shooter).
 *
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see Shooter
 * @see Intake
 */
@Config
public class Carousel {

    /** Lifts the teardrop to feed the artifact to the shooter. */
    private final Servo kickerServo;
    /** Rotates the carousel to position balls. */
    private final Servo carouselServo;
   // double carouselPosition1 = .2;//for testing variable to hold the forward positon of the carousel (will be set in code)
   // double carouselPosition2 = .4;//for testing variable to hold the backward positon of the carousel (will be set in code)

  //KICKER POSITIONS
     public static double kickerDownPosition = .8;//0
     static public double kickerTinyLiftPosition = .65;//.7
     double kickerFullLiftPosition = .5;//.5

    //CAROUSEL POSITIONS
    public static double carouselPositionHome = 0.5;
    double carouselPositionMin = 0;//forward sping for testing only just to see if carousel turns
    double carouselPositionMax = 1;//backward spin for testing only just to see carousel moves
    //carousel positions
    public static double offSetAdjustment = 175;
    public static int degreeRange = 1667;
    public static double carouselPositionConversionFactor = (1.0)/(degreeRange); // converts degrees to servos function inputs (0 to 1.0)
    double carouselPositionIntakeOne = carouselPositionConversionFactor * (0 + offSetAdjustment);
    double carouselPositionIntakeTwo = carouselPositionConversionFactor * (120.0 + offSetAdjustment);
    double carouselPositionIntakeThree = carouselPositionConversionFactor * (240.0 + offSetAdjustment);
    double carouselPositionLaunchOne = carouselPositionConversionFactor * (180.0 + offSetAdjustment);
    double carouselPositionLaunchTwo = carouselPositionConversionFactor * (310.0 + offSetAdjustment);
    double carouselPositionLaunchThree = carouselPositionConversionFactor * (430.0 + offSetAdjustment);

    /**
     * Constructs a Carousel subsystem and maps the servos from hardware.
     *
     * @param hardwareMap the robot's hardware map containing {@code "kickerServo"}
     *                    and {@code "carouselServo"}
     */
    public Carousel(HardwareMap hardwareMap) {
       kickerServo = hardwareMap.get(Servo.class, "kickerServo");
       carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }

    /** Raises the kicker slightly to seat a ball against the shooter flywheel. */
    public void setTinyKicker(){
        kickerServo.setPosition(kickerTinyLiftPosition);
    }

    /** Raises the kicker fully to launch a ball into the shooter. */
    public void setFullKicker(){
        kickerServo.setPosition(kickerFullLiftPosition);
    }

    /** Lowers the kicker to its resting (down) position. */
    public void setHomePositionKicker(){
     kickerServo.setPosition(kickerDownPosition);
    }
    //for testing

    //for testing
   // public void spinCarouselBackward(){
      //  carouselServo.setPosition(carouselPositionMax);
   // }
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

    /** Rotates the carousel to its home (center) position. */
    public void spinCarouselHome() {
        carouselServo.setPosition(carouselPositionHome);
    }

    /** Rotates the carousel to its minimum position (for testing). */
    public void spinCarouselMin() {
        carouselServo.setPosition(carouselPositionMin);
    }

    /** Rotates the carousel to its maximum position (for testing). */
    public void spinCarouselMax() {
        carouselServo.setPosition(carouselPositionMax);
    }

    /** Rotates the carousel to launch slot 1 (180 degrees + offset). */
    public void spinCarouselLaunchOne() {
        carouselServo.setPosition(carouselPositionLaunchOne);
    }

    /** Rotates the carousel to launch slot 2 (310 degrees + offset). */
    public void spinCarouselLaunchTwo() {
        carouselServo.setPosition(carouselPositionLaunchTwo);
    }

    /** Rotates the carousel to launch slot 3 (430 degrees + offset). */
    public void spinCarouselLaunchThree() {
        carouselServo.setPosition(carouselPositionLaunchThree);
    }

    public void disableCarouselAndKicker(){
        /** To stop a servo, we need to disable it, and for that, we need the ServoImplEx Class so we
        must cast the kickerServo and carouselServo into SErvoImplEx () -> servos
        */
        ServoImplEx kickerStopServo = (ServoImplEx) kickerServo;
        kickerStopServo.setPwmDisable();

        ServoImplEx carouselStopServo = (ServoImplEx) carouselServo;
        carouselStopServo.setPwmDisable();
    }

    public void enableCarouselAndKicker(){
        ServoImplEx kickerEnableServo = (ServoImplEx) kickerServo;
        kickerEnableServo.setPwmEnable();

        ServoImplEx carouselEnableServo = (ServoImplEx) carouselServo;
        carouselEnableServo.setPwmEnable();
    }
}

