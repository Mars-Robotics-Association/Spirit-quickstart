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
    double carouselPositionMin = 0;//forward sping for testing only just to see if carousel turns
    double carouselPositionMax = 1;//backward spin for testing only just to see carousel moves
    //carousel positions
    double carouselPositionConversionFactor = (1.0)/(1620.0); // converts degress to servos function inputs (0 to 1.0)
    double carouselPositionIntakeOne = carouselPositionConversionFactor * 405.0;
    double carouselPositionIntakeTwo = carouselPositionConversionFactor * 520.0;
    double carouselPositionIntakeThree = carouselPositionConversionFactor * 635.0;
    double carouselPositionLaunchOne = carouselPositionConversionFactor * 575.0;
    double carouselPositionLaunchTwo = carouselPositionConversionFactor * 460.0;
    double carouselPositionLaunchThree = carouselPositionConversionFactor * 340.0;

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
    //for testing
    public void spinCarouselForward(){
        carouselServo.setPosition(carouselPositionMin);
    }
    //for testing
    public void spinCarouselBackward(){
        carouselServo.setPosition(carouselPositionMax);
    }
    public void spinCarouselIntakeOne() {
        carouselServo.setPosition(carouselPositionIntakeOne);
    }
    public void spinCarouselIntakeTwo() {
        carouselServo.setPosition(carouselPositionIntakeTwo);
    }
    public void spinCarouselIntakeThree() {
        carouselServo.setPosition(carouselPositionIntakeThree);
    }
    public void spinCarouselLaunchOne() {
        carouselServo.setPosition(carouselPositionLaunchOne);
    }
    public void spinCarouselLaunchTwo() {
        carouselServo.setPosition(carouselPositionLaunchTwo);
    }
    public void spinCarouselLaunchThree() {
        carouselServo.setPosition(carouselPositionLaunchThree);
    }
    }

