package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

@Config
public class Carousel {

    private final Servo kickerServo;//lifts the teardrop to feed the artifact to the shooter
    private final Servo carouselServo;//turns the carousel
   // double carouselPosition1 = .2;//for testing variable to hold the forward positon of the carousel (will be set in code)
   // double carouselPosition2 = .4;//for testing variable to hold the backward positon of the carousel (will be set in code)
   double kickerDownPosition = .9;//0
  static public double kickerTinyLiftPosition = .65;//.7
   double kickerFullLiftPosition = .5;//.5
   double carouselPositionHome = 0.5;
    double carouselPositionMin = 0;//forward sping for testing only just to see if carousel turns
    double carouselPositionMax = 1;//backward spin for testing only just to see carousel moves
    //carousel positions
    double offSetAdjustment = 75;
    double carouselPositionConversionFactor = (1.0)/(1620.0); // converts degress to servos function inputs (0 to 1.0)
    double carouselPositionIntakeOne = carouselPositionConversionFactor * (6.0 + offSetAdjustment);
    double carouselPositionIntakeTwo = carouselPositionConversionFactor * (126.0 + offSetAdjustment);
    double carouselPositionIntakeThree = carouselPositionConversionFactor * (246.0 + offSetAdjustment);
    double carouselPositionLaunchOne = carouselPositionConversionFactor * (186.0 + offSetAdjustment);
    double carouselPositionLaunchTwo = carouselPositionConversionFactor * (306.0 + offSetAdjustment);
    double carouselPositionLaunchThree = carouselPositionConversionFactor * (426.0 + offSetAdjustment);

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

    public void spinCarouselHome() {
        carouselServo.setPosition(carouselPositionHome);
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

