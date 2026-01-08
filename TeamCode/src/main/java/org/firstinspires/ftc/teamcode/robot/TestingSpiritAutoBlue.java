package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.acmerobotics.roadrunner.Pose2d;

@Autonomous(name = "SpiritAutoBlue", group = "Robot")
public class TestingSpiritAutoBlue extends LinearOpMode {

    private DcMotorEx leftFront, rightFront, leftBack, rightBack;
    private final ElapsedTime runtime = new ElapsedTime();

    // Encoder constants
    static final double COUNTS_PER_MOTOR_REV = 781.5;
    static final double DRIVE_GEAR_REDUCTION = 1.0;
    static final double WHEEL_DIAMETER_INCHES = 4.0;
    static final double COUNTS_PER_INCH =
            (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                    (WHEEL_DIAMETER_INCHES * Math.PI);

    // Turning constant (TUNE THIS)
    static final double COUNTS_PER_DEGREE = 10.0;

    static final double DRIVE_SPEED = 0.6;
    static final double TURN_SPEED = 0.5;

    @Override
    public void runOpMode() {
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        // Hardware mapping
        leftFront  = hardwareMap.get(DcMotorEx.class, "leftFront");
        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
        leftBack   = hardwareMap.get(DcMotorEx.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotorEx.class, "rightBack");

        // Motor directions
        leftFront.setDirection(DcMotorSimple.Direction.FORWARD);
        leftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);

        setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("Ready");
        telemetry.update();

        waitForStart();

        // 1. Move forward 26 inches
        encoderDrive(DRIVE_SPEED, 26, 26, 26, 26, 4.0);

        // 2. Turn left 45 degrees
        //turnLeft(45);

        // 3. Move forward 18 inches
        //encoderDrive(DRIVE_SPEED, 18, 18, 18, 18, 3.0);

        // 4. Stop
        stopMotors();

        telemetry.addLine("Autonomous Complete");
        telemetry.update();
        sleep(1000);
    }

    // Forward / backward
    private void encoderDrive(double speed,
                              double lf, double rf, double lb, double rb,
                              double timeoutS) {

        int lfTarget = leftFront.getCurrentPosition() + (int)(lf * COUNTS_PER_INCH);
        int rfTarget = rightFront.getCurrentPosition() + (int)(rf * COUNTS_PER_INCH);
       int lbTarget = leftBack.getCurrentPosition() + (int)(lb * COUNTS_PER_INCH);
        int rbTarget = rightBack.getCurrentPosition() + (int)(rb * COUNTS_PER_INCH);

        leftFront.setTargetPosition(lfTarget);
        rightFront.setTargetPosition(rfTarget);
        leftBack.setTargetPosition(lbTarget);
        rightBack.setTargetPosition(rbTarget);

        setMode(DcMotor.RunMode.RUN_TO_POSITION);
        runtime.reset();
        setPower(speed);

        while (opModeIsActive() && runtime.seconds() < timeoutS &&
                // leftFront.isBusy() && rightFront.isBusy() &&
                leftBack.isBusy() && rightBack.isBusy()) {
            telemetry.update();
        }

        stopMotors();
    }

    // Turn left by degrees
    private void turnLeft(double degrees) {
        int move = (int)(degrees * COUNTS_PER_DEGREE);

        leftFront.setTargetPosition(leftFront.getCurrentPosition() - move);
        leftBack.setTargetPosition(leftBack.getCurrentPosition() - move);
        rightFront.setTargetPosition(rightFront.getCurrentPosition() + move);
        rightBack.setTargetPosition(rightBack.getCurrentPosition() + move);

        setMode(DcMotor.RunMode.RUN_TO_POSITION);
        setPower(TURN_SPEED);

        while (opModeIsActive() &&
                leftBack.isBusy() && rightBack.isBusy()) {
            telemetry.update();
        }

        stopMotors();
    }

    private void stopMotors() {
        setPower(0);
        setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        sleep(200);
    }

    private void setMode(DcMotor.RunMode mode) {
        leftFront.setMode(mode);
        rightFront.setMode(mode);
        leftBack.setMode(mode);
        rightBack.setMode(mode);
    }

    private void setPower(double power) {
        leftFront.setPower(power);
        rightFront.setPower(power);
        leftBack.setPower(power);
        rightBack.setPower(power);
    }
}
