package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {
   private final Servo feedServo;
   private final Servo shooterServo;
   private final Servo spinServo;


    public Carousel(HardwareMap hardwareMap) {
       feedServo = hardwareMap.get(Servo.class, "feedServo");//teardrop from intake to carousel
       shooterServo = hardwareMap.get(Servo.class, "shooterServo");//teardrop from carousel to shooters
        spinServo = hardwareMap.get(Servo.class, "spinServo");//advances the carousel


    }
    //feeds an artifact into the carousel
    public void engageFeedServo() {
        feedServo.setPosition(1);
         }
    //resets the teardrop that feeds the carousel
    public void resetFeedServo(){
        feedServo.setPosition(0);
    }

    //feeds an artifact to the shooter
    public void engageShooterServo() {
       shooterServo.setPosition(1);
    }
    //resets tej teardrop that feeds the shooter
   public void releaseShooterServo(){
     shooterServo.setPosition(0);
    }

   public void advanceCarousel()
   {
        spinServo.setPosition(spinServo.getPosition() + .06);//120 degrees/1800 servo degree range -= .06
    }

    public void resetCarousel(){
        spinServo.setPosition(0);
    }
    }

