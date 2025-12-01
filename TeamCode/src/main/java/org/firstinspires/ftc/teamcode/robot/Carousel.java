package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {
   // private final Servo feedServo;
    //private final Servo shooterServo;
   // private final Servo spinServo;
    private final Servo tiltServo;

    public Carousel(HardwareMap hardwareMap) {
       // feedServo = hardwareMap.get(Servo.class, "feedServo");//teardrop from intake to carousel
       // shooterServo = hardwareMap.get(Servo.class, "shooterServo");//teardrop from carousel to shooters
        //spinServo = hardwareMap.get(Servo.class, "spinServo");//advances the carousel
        tiltServo = hardwareMap.get(Servo.class, "tiltServo");//controls angle of shooters

    }
//feeds an artifact into the carousel
   // public void flipFeedServo() {
       // feedServo.setPosition(1);
  //  }
    //resets the teardrop that feeds the carousel
    //public void unflipFeedServo(){
        //feedServo.setPosition(0);
   // }

    //feeds an artifact to the shooter
    //public void flipShooterServo() {
       // shooterServo.setPosition(1);
    //}
    //resets tej teardrop that feeds the shooter
   // public void unflipShooterServo(){
     //   shooterServo.setPosition(0);
   // }

   // public void advanceCarousel() {
      //  spinServo.setPosition(spinServo.getPosition() + 120);
   // }

   // public void resetCarousel(){
      //  spinServo.setPosition(0);
   // }
    }

