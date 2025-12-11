package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {

    private final Servo kickerServo;//lifts the teardrop to feed the artifact to the shooter
    private final Servo carouselServo;//turns the carousel

    public static class Config {
        // double carouselPosition1 = .2;//for testing variable to hold the forward positon of the carousel (will be set in code)
        // double carouselPosition2 = .4;//for testing variable to hold the backward positon of the carousel (will be set in code)
        public double kickerDownPosition = .8;
        public double kickerTinyLiftPosition = .7;
        public double kickerFullLiftPosition = .5;
        public double carouselPositionHome = 0;
        public double carouselPositionMin = 0;//forward sping for testing only just to see if carousel turns
        public double carouselPositionMax = 1;//backward spin for testing only just to see carousel moves
        //carousel positions
        public double carouselPositionConversionFactor = (1.0) / (1620.0); // converts degress to servos function inputs (0 to 1.0)
        public double carouselPositionIntakeOne = 405.0;
        public double carouselPositionIntakeTwo = 520.0;
        public double carouselPositionIntakeThree = 635.0;
        public double carouselPositionLaunchOne = 575.0;
        public double carouselPositionLaunchTwo = 460.0;
        public double carouselPositionLaunchThree = 340.0;
    }

    public static Config config = new Config();

    public Carousel(HardwareMap hardwareMap) {
        kickerServo = hardwareMap.get(Servo.class, "kickerServo");
        carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }

    public void setTinyKicker() {
        kickerServo.setPosition(config.kickerTinyLiftPosition);
    }

    public void setFullKicker() {
        kickerServo.setPosition(config.kickerFullLiftPosition);
    }

    public void setHomePositionKicker() {
        kickerServo.setPosition(config.kickerDownPosition);
    }

    //for testing
    public void spinCarouselForward() {
        carouselServo.setPosition(config.carouselPositionMin);
    }

    //for testing
    public void spinCarouselBackward() {
        carouselServo.setPosition(config.carouselPositionMax);
    }

    private void setPos(double unconverted) {
        carouselServo.setPosition(config.carouselPositionConversionFactor * unconverted);
    }

    public void spinCarouselIntakeOne() {
        setPos(config.carouselPositionIntakeOne);
    }

    public void spinCarouselIntakeTwo() {
        setPos(config.carouselPositionIntakeTwo);
    }

    public void spinCarouselIntakeThree() {
        setPos(config.carouselPositionIntakeThree);
    }

    public void spinCarouselHome() {
        setPos(config.carouselPositionHome);
    }

    public void spinCarouselLaunchOne() {
        setPos(config.carouselPositionLaunchOne);
    }

    public void spinCarouselLaunchTwo() {
        setPos(config.carouselPositionLaunchTwo);
    }

    public void spinCarouselLaunchThree() {
        setPos(config.carouselPositionLaunchThree);
    }
}

