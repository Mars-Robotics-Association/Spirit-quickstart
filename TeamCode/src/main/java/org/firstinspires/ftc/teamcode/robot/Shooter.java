package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Subsystem controlling the dual-flywheel ball shooter and its tilt servo.
 *
 * <p>The shooter uses two motors ({@code "shooterMotorLeft"} and {@code "shooterMotorRight"})
 * spinning in opposite directions to launch balls, and a servo ({@code "tiltServo"}) to
 * adjust the launch angle for near vs. far targets.
 *
 * <p>Velocity control uses a feedforward + proportional feedback loop with exponential
 * smoothing (see {@link #setShooterVelocity}). The feedforward values
 * ({@link #feedLeftForward}, {@link #feedRightForward}) set a baseline power proportional
 * to the desired speed, while the feedback term corrects any error between the smoothed
 * target and actual velocities using gain {@link #kp}.
 *
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see Carousel
 */
@Config
public class Shooter {

    public final DcMotorEx shooterMotorLeft;
    public final DcMotorEx shooterMotorRight;
    public final Servo tiltServo;
    static public double nearTiltPosition = .03;//for testing
    static public double farTiltPosition = .15;//for testing
    static public double homeTiltPosition = 0;//for testing
    /* *****************BALLPARK VELOCITY OF ENCODED SHOOTER MOTORS


/*The near target as measured by tachometer is between 1700 and 1900 rpm so say 1800 (as measured by Mr. Beckstead on 1/21/2026).
The far target as measured by tachometer is between 3600 and 3700 rpm so say 3650

So, velocity for near target = (1800 rotations per minute /28 ticks per seconds)/60 seconds = 840 tps
So, velocity for far target = (3650 rotations per second/28 ticks per seconds)/60 seconds = 1,703 tps

   */
    static public double nearShooterVelocity = 650;//tps for use with encoders to set shooter speed
    static public double farShooterVelocity = 945;//tps for use with encoders to set shooter speed

    static public double shooterVelocity = 0;
    static public double maxShooterVelocityLeft = 2350;//tps when power set to 1
    static public double maxShooterVelocityRight = 2460;//tps when power set to 1

    double smoothTargetShooterVelocity = 0;
    double smoothActualLeftShooterVelocity = 0;
    double smoothActualRightShooterVelocity = 0;
    public static double actualMotorPower = 0;

    static public double kp = 0.002;
    static public double smoothingFactor = .1;
    public double shooterPower = 1;
    static public double speed = .2;

    static public double feedLeftForward = .41;
            static public double feedRightForward = .35;
    //*********************************************

    /**
     * Constructs a Shooter subsystem and maps the motors and tilt servo from hardware.
     * The right motor is reversed so both flywheels spin inward.
     *
     * @param hardwareMap the robot's hardware map containing {@code "shooterMotorLeft"},
     *                    {@code "shooterMotorRight"}, and {@code "tiltServo"}
     */
    public Shooter(HardwareMap hardwareMap) {

        shooterMotorLeft = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        //enable the encoders on the shooter motors
       // shooterMotorLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
       // shooterMotorRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");//controls angle of shooters

        shooterMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);
        shooterMotorLeft.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Runs the feedforward + feedback velocity control loop for both shooter motors.
     *
     * <p>Applies exponential smoothing to both the target and actual velocities, then
     * computes a proportional correction term added to the static feedforward power.
     * If {@code shooterVelocity} is 0, both motors are stopped immediately.
     *
     * @param shooterVelocity desired flywheel velocity in ticks per second
     * @param telemetry       telemetry instance for logging velocity diagnostics
     */
    public void setShooterVelocity(double shooterVelocity, Telemetry telemetry) {
        telemetry.addData("Target T/S", shooterVelocity);
      // shooterMotorLeft.setVelocity(shooterVelocity);//if using encoders, use this code. Otherwise, set power
      // shooterMotorRight.setVelocity(shooterVelocity);//if using encoders, use this code. Otherwise, set power
//        shooterMotorLeft.setPower(shooterPower);
//        shooterMotorRight.setPower(shooterPower);

        //fail safe
        if (shooterVelocity == 0){
           shooterMotorLeft.setPower(0);
           shooterMotorRight.setPower(0);
           return;
       }
        //convert target velocity to feed forward

       // double feedLeftForward = shooterVelocity/maxShooterVelocityLeft;//target/max (moved to class variable)
        //double feedRightForward = shooterVelocity/maxShooterVelocityRight;//target/max (moved to class variable)

        //measure current velocity
        telemetry.addData("FeedLeftForward ", feedLeftForward);
        telemetry.addData("FeedRightForward ", feedRightForward);
        double actualLeftVelocity = shooterMotorLeft.getVelocity();//removed mw's negative from shooterMotorLeft
        double actualRightVelocity = shooterMotorRight.getVelocity();//removed mw's negative from shooterMotorRight

        //calculate smoothing
        smoothTargetShooterVelocity = (shooterVelocity * smoothingFactor) + (1 -  smoothingFactor) * smoothTargetShooterVelocity;
        smoothActualLeftShooterVelocity = (actualLeftVelocity * smoothingFactor) + (1 -  smoothingFactor) * smoothActualLeftShooterVelocity;
        smoothActualRightShooterVelocity = (actualRightVelocity * smoothingFactor) + (1 -  smoothingFactor) * smoothActualRightShooterVelocity;

        //how fast am I going versus how fast I want to go
        double feedbackLeft = (smoothTargetShooterVelocity - smoothActualLeftShooterVelocity) * kp;
        double feedbackRight = (smoothTargetShooterVelocity - smoothActualRightShooterVelocity) * kp;

        telemetry.addData("FeedbackLeft", feedbackLeft);
        telemetry.addData("FeedbackRight", feedbackRight);

        //set the power of the shooter motors to their current speed plus or minus the feed foward factors
        //so it is constantly adjusting itself to the target
      //shooterMotorLeft.setPower(feedbackLeft + feedLeftForward);
        actualMotorPower = (feedbackLeft + feedLeftForward) * 1000;
        shooterMotorLeft.setPower(feedbackLeft + feedLeftForward);
       shooterMotorRight.setPower(feedbackRight + feedRightForward);
        //shooterMotorLeft.setPower(speed);
       // shooterMotorRight.setPower(speed);
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

    /** Resets the tilt servo to the home (flat) position. */
    public void setHomeTiltPosition() {
        tiltServo.setPosition(homeTiltPosition);
    }

}



