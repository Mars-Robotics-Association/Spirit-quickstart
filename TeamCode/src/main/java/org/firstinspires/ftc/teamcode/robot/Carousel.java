package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {

   private final Servo shooterServo;
   //private final Servo spinServo;


    public Carousel(HardwareMap hardwareMap) {

       shooterServo = hardwareMap.get(Servo.class, "shooterServo");//teardrop from carousel to shooters
       // spinServo = hardwareMap.get(Servo.class, "spinServo");//advances the carousel



    }


    //feeds an artifact to the shooter
    public void engageShooterServo() {
       shooterServo.setPosition(1);
    }
    //resets tej teardrop that feeds the shooter

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

