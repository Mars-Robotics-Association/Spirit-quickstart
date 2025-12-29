package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Auto_Move_Turn_Strafe", group = "Robot")
public class Auto2bkf extends LinearOpMode {

    private DcMotorEx leftFront, rightFront, leftBack, rightBack;
    private final ElapsedTime runtime = new ElapsedTime();

    // Encoder constants
    static final double COUNTS_PER_MOTOR_REV = 1440;
    static final double DRIVE_GEAR_REDUCTION = 1.0;
    static final double WHEEL_DIAMETER_INCHES = 4.0;
    static final double COUNTS_PER_INCH =
            (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                    (WHEEL_DIAMETER_INCHES * Math.PI);

    // Turning constant (TUNE THIS)
    static final double COUNTS_PER_DEGREE = 10.0;

    static final double DRIVE_SPEED = 0.6;
    static final double TURN_SPEED = 0.5;
    static final double STRAFE_SPEED = 0.6;

    @Override
    public void runOpMode() {

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

        setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("Ready");
        telemetry.update();

        waitForStart();

        // 1. Move forward 5 inches
        encoderDrive(DRIVE_SPEED, 5, 5, 5, 5, 4.0);

        // 2. Turn left 25 degrees
        turnLeft(25);

        // 3. Strafe left 3 inches
        strafeLeft(3);

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
                leftFront.isBusy() && rightFront.isBusy() &&
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
                leftFront.isBusy() && rightFront.isBusy()) {
            telemetry.update();
        }

        stopMotors();
    }

    // Strafe left
    private void strafeLeft(double inches) {
        int move = (int)(inches * COUNTS_PER_INCH);

        leftFront.setTargetPosition(leftFront.getCurrentPosition() - move);
        rightFront.setTargetPosition(rightFront.getCurrentPosition() + move);
        leftBack.setTargetPosition(leftBack.getCurrentPosition() + move);
        rightBack.setTargetPosition(rightBack.getCurrentPosition() - move);

        setMode(DcMotor.RunMode.RUN_TO_POSITION);
        setPower(STRAFE_SPEED);

        while (opModeIsActive() &&
                leftFront.isBusy() && rightFront.isBusy()) {
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
