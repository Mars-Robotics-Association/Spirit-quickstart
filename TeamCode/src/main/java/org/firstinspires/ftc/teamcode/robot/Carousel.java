package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {

    private final Servo kickerServo;//lifts the teardrop to feed the artifact to the shooter
    private final Servo carouselServo;//turns the carousel
   // double carouselPosition1 = .2;//for testing variable to hold the forward positon of the carousel (will be set in code)
   // double carouselPosition2 = .4;//for testing variable to hold the backward positon of the carousel (will be set in code)
   double kickerDownPosition = .8;
   double kickerTinyLiftPosition = .7;
   double kickerFullLiftPosition = .5;
    double carouselPosition1 = .2;//forward sping for testing only just to see if carosel turns
    double carouselPosition2 = .4;//backward spin for testing only just to see carousel moves

    public Carousel(HardwareMap hardwareMap) {
       kickerServo = hardwareMap.get(Servo.class, "kickerServo");
       carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }

    public void setTinyKicker(){
        kickerServo.setPosition(kickerTinyLiftPosition);
    }

    public void setFullKicker(){
        kickerServo.setPosition(kickerFullLiftPosition);
    }

    public void setHomePositionKicker(){
     kickerServo.setPosition(kickerDownPosition);
    }

    public void spinCarouselForward(){
        carouselServo.setPosition(carouselPosition1);
    }
    public void spinCarouselBackward(){
        carouselServo.setPosition(carouselPosition2);
    }
    }

