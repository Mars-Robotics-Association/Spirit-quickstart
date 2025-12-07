package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {

   private final Servo shooterServo;//lifts the teardrop to feed the artifact to the shooter
   private final Servo carouselServo;//turns the carousel
    double downKickerPosition = .9;//value to position kicker down
    double tinyLiftKickerPosition = .8;//value to cause kicker to lift a tiny bit to allow tilt
    double fullLiftKickerPosition = .5;//value to life kicker up to shooter
    double carouselHomePosition = 0;//starting position of carousel
    double carouselPosition = .33;//position to which carousel will move to advance
   // double carouselIncrement = .33;//value needed to advance carousel by 120 degres


    public Carousel(HardwareMap hardwareMap) {
       shooterServo = hardwareMap.get(Servo.class, "shooterServo");
       carouselServo = hardwareMap.get(Servo.class, "carouselServo");
    }
    //-------------------KICK SHOOTER SERVO POSITIONING ----------------------
    //positions the shooter servo in the down position
    public void downPositionKickerServo(){
        shooterServo.setPosition(downKickerPosition);
    }

    //lifts the shooter servo up slightly so the carousel can move
    public void tinyLiftKickerServo(){
        shooterServo.setPosition(tinyLiftKickerPosition);
    }
    //makes the shooter servo feed the artifact to the shooters
    public void fullLiftKickerServo(){
        shooterServo.setPosition(fullLiftKickerPosition);
    }

    public void spinCarouselHome(){
        carouselServo.setPosition(carouselHomePosition);
}
   public void spinCarousel() {
       carouselServo.setPosition(carouselPosition);
   }
   // public void spinCarouselBackward(){
   //    carouselServo.setPosition();
   // }
    }

