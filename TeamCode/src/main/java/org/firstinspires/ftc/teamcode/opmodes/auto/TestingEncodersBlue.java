package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.MecanumDrive;

/**
 * Experimental encoder-based autonomous for the blue alliance (currently disabled).
 *
 * <p>Uses the {@code rightBack} motor encoder to drive a measured distance (26 inches)
 * rather than relying on timed movements. This was a test to explore encoder-based
 * navigation as an alternative to time-based driving.
 */
@Disabled
@Config
@Autonomous(name = "TestingEncodersBlue", group = "Autonomous Testing")
public class TestingEncodersBlue extends LinearOpMode {

    private DcMotorEx leftFront, rightFront, leftBack, rightBack;
    private final ElapsedTime runtime = new ElapsedTime();

    // Encoder constants
    static final double COUNTS_PER_MOTOR_REV = 751.8;
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
        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
        leftBack = hardwareMap.get(DcMotorEx.class, "leftBack");
        rightBack = hardwareMap.get(DcMotorEx.class, "rightBack");
/*
        // Motor directions
        leftFront.setDirection(DcMotorSimple.Direction.FORWARD);
        leftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);
*/

        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

       telemetry.addLine("Ready");
        telemetry.update();

        waitForStart();

        // 1. Move forward 43 inches
        // encoderDrive(DRIVE_SPEED, 43, 43, 43, 43, 4.0);
        double driveX = 0.0;
        double driveY = 0.5;
        double targetPosition = rightBack.getCurrentPosition() - (COUNTS_PER_INCH * 26);
        telemetry.setAutoClear(false);
        telemetry.clear();
        telemetry.addData("Encoder Ticks ",rightBack.getCurrentPosition());
        telemetry.update();

        while (rightBack.getCurrentPosition() > targetPosition) {
            drive.setDrivePowers(new PoseVelocity2d(
                    new Vector2d(
                            driveY,
                            driveX

                    ),
                    0
            ));
        }

            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }



      /*
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


    /*private void encoderDrive(double speed,
                              double lf, double rf, double lb, double rb,
                              double timeoutS) {
*/
        // int lfTarget = leftFront.getCurrentPosition() + (int)(lf * COUNTS_PER_INCH);
        // int rfTarget = rightFront.getCurrentPosition() + (int)(rf * COUNTS_PER_INCH);
        //  int lbTarget = leftBack.getCurrentPosition() + (int)(lb * COUNTS_PER_INCH);
        // int rbTarget = rightBack.getCurrentPosition() + (int)(rb * COUNTS_PER_INCH);

/*
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

      //  leftFront.setTargetPosition(leftFront.getCurrentPosition() - move);
        leftBack.setTargetPosition(leftBack.getCurrentPosition() - move);
      //  rightFront.setTargetPosition(rightFront.getCurrentPosition() + move);
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

    public void setPower(double power) {
        leftFront.setPower(power);
       rightFront.setPower(power);
        leftBack.setPower(power);
        rightBack.setPower(power);
    }
   */
    }

