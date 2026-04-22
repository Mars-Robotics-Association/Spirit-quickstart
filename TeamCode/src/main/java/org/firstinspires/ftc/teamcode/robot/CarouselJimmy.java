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
 * @see ShooterTimmy
 * @see IntakeTimmy
 */
@Config
public class CarouselJimmy {

    /** Lifts the teardrop to feed the artifact to the shooter. */
    private Servo kickerServo;
    /** Rotates the carousel to position artifact. */
    private Servo carouselServo;


    //KICKER POSITIONS
    public static double kickerDownPosition = .52;//0

    double kickerFullLiftPosition = .21;

    //CAROUSEL POSITIONS
    public static double carouselPositionHome = 0.0;
    double carouselPositionMin = 0;//forward sping for testing only just to see if carousel turns
   double carouselPositionMax = 1;//backward spin for testing only just to see carousel moves
    //carousel positions
    public static double offSetAdjustment = 175;
   public static int degreeRange = 1667;public static double carouselPositionConversionFactor = (1.0)/(degreeRange); // converts degrees to servos function inputs (0 to 1.0)
   double carouselPositionIntakeOne = carouselPositionConversionFactor * (0 + offSetAdjustment);
   double carouselPositionIntakeTwo = carouselPositionConversionFactor * (120.0 + offSetAdjustment);
   double carouselPositionIntakeThree = carouselPositionConversionFactor * (240.0 + offSetAdjustment);
    double carouselPositionLaunchOne = 0.13;
    double carouselPositionLaunchTwo = 0.2;
    double carouselPositionLaunchThree = 0.06;

    /**
     * Constructs a Carousel subsystem and maps the servos from hardware.
     *
     * @param hardwareMap the robot's hardware map containing {@code "kickerServo"}
     *                    and {@code "carouselServo"}
     */
    public CarouselJimmy(HardwareMap hardwareMap) {
        kickerServo = hardwareMap.get(Servo.class, "kickerServo");
        carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }

    /** Raises the kicker slightly to seat a ball against the shooter flywheel. */
   //public void setTinyKicker(){
      //  kickerServo.setPosition(kickerTinyLiftPosition);
    //}

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


