package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Shooter {
    public final DcMotorEx shooterMotorLeft;
    public final DcMotorEx shooterMotorRight;
    public final Servo tiltServo;



    public Shooter(HardwareMap hardwareMap) {

        shooterMotorLeft = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");//controls angle of shooters

        shooterMotorRight.setDirection(DcMotorSimple.Direction.FORWARD);
        shooterMotorLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    /**
     * Set the power to the left and right motors
     */
        public void setShooterPower(double power) {
        shooterMotorLeft.setPower(power);
        shooterMotorRight.setPower(power);
         }


        public void setTiltServo(double shooterTiltPositon){
           tiltServo.setPosition(shooterTiltPositon);
         }
    }

/**
 Set position of servo that tilts the shooters
 *
 */


