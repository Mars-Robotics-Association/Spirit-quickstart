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
    public static double offSetAdjustment = 175;
    public static int degreeRange = 1667;
    public static double carouselPositionConversionFactor = (1.0)/(degreeRange); // converts degrees to servos function inputs (0 to 1.0)
    double carouselPositionIntakeOne = carouselPositionConversionFactor * (0 + offSetAdjustment);
    double carouselPositionIntakeTwo = carouselPositionConversionFactor * (120.0 + offSetAdjustment);
    double carouselPositionIntakeThree = carouselPositionConversionFactor * (240.0 + offSetAdjustment);
    double carouselPositionLaunchOne = carouselPositionConversionFactor * (180.0 + offSetAdjustment);
    double carouselPositionLaunchTwo = carouselPositionConversionFactor * (310.0 + offSetAdjustment);
    double carouselPositionLaunchThree = carouselPositionConversionFactor * (430.0 + offSetAdjustment);

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
   // public void spinCarouselBackward(){
      //  carouselServo.setPosition(carouselPositionMax);
   // }
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

    public void spinCarouselMin() {
        carouselServo.setPosition(carouselPositionMin);
    }
    public void spinCarouselMax() {
        carouselServo.setPosition(carouselPositionMax);
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

