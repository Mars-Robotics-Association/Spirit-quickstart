package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class ShooterforUSC {

    public final DcMotorEx shooterMotorLeft;
    public final DcMotorEx shooterMotorRight;
    public final Servo tiltServo;
    static public double nearTiltPosition = .1;//for testing
    static public double farTiltPosition = .2;//for testing
    static public double homeTiltPosition = 0;//for testing
    /* *****************BALLPARK VELOCITY OF ENCODED SHOOTER MOTORS


/*The near target as measured by tacometer is between 1700 and 1900 rpm so say 1800 (as measured by Mr. Beckstead on 1/21/2026).
The far target as measured by tacometer is between 3600 and 3700 rpm so say 3650

So, velocity for near target = (1800 rotations per minute /28 ticks per seconds)/60 seconds = 840 rpm
So, velocity for far target = (3650 rotations per second/28 ticks per seconds)/60 seconds = 1,703 rpm

   */
    static public double nearShooterVelocity = 840;//rpm for use with encoders to set shooter speed
    static public double farShooterVelocity = 1703;//rpm for use with encoders to set shooter speed

    static public double shooterVelocity = 0;
    static public double maxShooterVelocityLeft = 2350;//rpm when power set to 1
    static public double maxShooterVelocityRight = 2460;//rpm when power set to 1

    double smoothTargetShooterVelocity = 0;
    double smoothActualLeftShooterVelocity = 0;
    double smoothActualRightShooterVelocity = 0;

    static public double kp = 0.005;
    static public double smoothingFactor = .01;
    public double shooterPower = 1;
    static public double speed = .2;

    static public double feedLeftForward = .4;
            static public double feedRightForward = .4;
    //*********************************************

    public ShooterforUSC(HardwareMap hardwareMap) {

        shooterMotorLeft = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        //enable the encoders on the shooter motors
        shooterMotorLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooterMotorRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");//controls angle of shooters

        shooterMotorRight.setDirection(DcMotorSimple.Direction.FORWARD);
        shooterMotorLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    /**
     * Set the power to the left and right motors
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
        double feedbackLeft = (shooterVelocity - actualLeftVelocity) * kp;
        double feedbackRight = (shooterVelocity - actualRightVelocity) * kp;

        telemetry.addData("FeedbackLeft", feedbackLeft);
        telemetry.addData("FeedbackRight", feedbackRight);

        //set the power of the shooter motors to their current speed plus or minus the feed foward factors
        //so it is constantly adjusting itself to the target
      //  shooterMotorLeft.setPower(feedbackLeft + feedLeftForward);
       // shooterMotorRight.setPower(feedbackRight + feedRightForward);
        shooterMotorLeft.setPower(speed);
        shooterMotorRight.setPower(speed);
    }



    //Set position of servo that tilts the shooters
    public void setTiltPosition(double tiltPositon) {
        tiltServo.setPosition(tiltPositon);
    }

    //FOR TESTING
    public void setNearTiltPosition(double nearTiltPosition) {
        tiltServo.setPosition(nearTiltPosition);
    }

    public void setFarTiltPosition(double farTiltPosition) {
        tiltServo.setPosition(farTiltPosition);
    }

    public void setHomeTiltPosition() {
        tiltServo.setPosition(homeTiltPosition);
    }

}



