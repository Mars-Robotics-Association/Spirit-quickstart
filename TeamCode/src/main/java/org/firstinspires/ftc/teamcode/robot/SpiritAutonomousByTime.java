package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "SpiritAutonomousByTime", group = "Robot")
public class SpiritAutonomousByTime extends LinearOpMode {

    DcMotor leftFront, rightFront, leftBack, rightBack;

    @Override
    public void runOpMode() {

        // Hardware map
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");

        // Motor directions
        leftFront.setDirection(DcMotorSimple.Direction.FORWARD);
        leftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.FORWARD);

        telemetry.addLine("Ready");
        telemetry.update();
        waitForStart();

        /* ----------------- AUTONOMOUS STEPS ----------------- */

        //ADD CODE TO SHOOT HERE

        // Drive forward ~5 inches
        drive(0.5, 500);   // power, time in ms

        //Turn left ~25 degrees
       // turnLeft(0.5, 6000);

        //Strafe Right for 1 second
        strafeRight(.5,1200);

        //Drive forward ~5 inches
       // drive(0.5, 6000);

        // Stop
        stopMotors();
    }


    /* ----------------- BASIC MOVEMENT METHODS ----------------- */

    void drive(double power, long timeMs) {
        setAllPower(power);
        sleep(timeMs);
        stopMotors();
    }

    void turnLeft(double power, long timeMs) {
        leftFront.setPower(-power);
        leftBack.setPower(-power);
        rightFront.setPower(power);
        rightBack.setPower(power);

        sleep(timeMs);
        stopMotors();
    }

    void strafeRight(double power, long timeMs) {
        leftFront.setPower(-power);
        leftBack.setPower(power);
        rightFront.setPower(power);
        rightBack.setPower(-power);

        sleep(timeMs);
        stopMotors();
    }

    void setAllPower(double power) {
        leftFront.setPower(power);
        leftBack.setPower(power);
        rightFront.setPower(power);
        rightBack.setPower(power);
    }

    void stopMotors() {
        setAllPower(0);
        sleep(200);
    }
}

