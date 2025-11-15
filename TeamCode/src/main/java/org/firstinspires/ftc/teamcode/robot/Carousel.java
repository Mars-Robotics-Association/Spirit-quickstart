package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Carousel {
    private final Servo feedServo;
    private final Servo shooterServo;
    private final CRServo spinServo;

    public Carousel(HardwareMap hardwareMap) {
        feedServo = hardwareMap.get(Servo.class, "feedServo");
        shooterServo = hardwareMap.get(Servo.class, "shooterServo");
        spinServo = hardwareMap.get(CRServo.class, "spinServo");
    }

    public void flipFeedServo() {
        feedServo.setPosition(1);
    }
    public void unflipFeedServo(){
        feedServo.setPosition(0);
    }
    public void flipShooterServo() {
        shooterServo.setPosition(1);
    }
    public void unflipShooterServo(){
        shooterServo.setPosition(0);
    }
}
