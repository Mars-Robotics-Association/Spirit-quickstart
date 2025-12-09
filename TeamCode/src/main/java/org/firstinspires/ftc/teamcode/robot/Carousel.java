package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.robotcore.hardware.ServoImplEx;

public class Carousel {

   private final Servo shooterServo;//lifts the teardrop to feed the artifact to the shooter
  // private final Servo carouselServo;//turns the carousel
  //private final ServoImplEx carouselServo;//turns the carousel
     private final Servo carouselServo;//turns the carousel
    double downKickerPosition = .9;//value to position kicker down
    double tinyLiftKickerPosition = .8;//value to cause kicker to lift a tiny bit to allow tilt
    double fullLiftKickerPosition = .5;//value to life kicker up to shooter
    double carouselHomePosition = 0;//starting position of carousel
    double carouselPosition = .2;//position to which carousel will move to advance
    double carouselPositionIncrement = .2;//value needed to advance carousel by 120 degrees


    public Carousel(HardwareMap hardwareMap) {
       shooterServo = hardwareMap.get(Servo.class, "shooterServo");
        carouselServo = hardwareMap.get(Servo.class, "carouselServo");
      // carouselServo = hardwareMap.get(ServoImplEx.class, "carouselServo");
     // carouselServo.setPwmRange(new PwmRange(500, 2500));
      //  carouselServo.setPwmRange(new PwmControl.PwmRange(500,2500));
    }
    //-------------------KICK SHOOTER SERVO POSITIONING ----------------------

    //positions the shooter servo in the down position
    public void setDownPositionKickerServo(){
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

