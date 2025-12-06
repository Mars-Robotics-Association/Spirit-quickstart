package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {

   private final Servo shooterServo;//lifts the teardrop to feed the artifact to the shooter
   private final Servo carouselServo;//turns the carousel


    public Carousel(HardwareMap hardwareMap) {
       shooterServo = hardwareMap.get(Servo.class, "shooterServo");
       carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }

    public void tinyLiftShooterServo(){
        shooterServo.setPosition(.2);
    }

    public void fullLiftShooterServo(){
        shooterServo.setPosition(.8);
    }

    public void releaseShooterServo(){
     shooterServo.setPosition(0);
    }
/*
   public void advanceCarousel()
   {
        spinServo.setPosition(spinServo.getPosition() + .06);//120 degrees/1800 servo degree range -= .06
    }
*/
    //public void resetCarousel(){
     //   spinServo.setPosition(0);
    //}
    }

