package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOpDrive - Introduction to FTC Robot Programming
 *
 * This OpMode drives a 4-wheel mecanum robot using Gamepad 1.
 *
 * MOTOR LAYOUT (top-down view):
 *
 *        FRONT
 *   [FL]      [FR]
 *   [BL]      [BR]
 *        BACK
 *
 * GAMEPAD CONTROLS:
 *   Left  Stick Y  → Forward / Backward
 *   Left  Stick X  → Strafe Left / Right   (side-to-side)
 *   Right Stick X  → Rotate Left / Right
 *   Right Bumper   → Hold to enable TURBO (full speed)
 *   (Default speed is 50% for easier control)
 */

@TeleOp(name = "TeleopDrive", group = "Intro")
public class TeleopDrive extends LinearOpMode {

    // -----------------------------------------------------------------------
    // STEP 1: Declare our motor variables
    //   DcMotor is the class that controls a motor plugged into the Control Hub.
    //   We declare one variable per motor.
    // -----------------------------------------------------------------------
    private DcMotor frontLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backLeftMotor;
    private DcMotor backRightMotor;

    // -----------------------------------------------------------------------
    // STEP 2: Constants
    //   Constants are values that never change while the program runs.
    //   Using named constants makes code easier to read and adjust.
    // -----------------------------------------------------------------------

    // Normal driving speed (0.0 = stopped, 1.0 = full power)
    private static final double NORMAL_SPEED = 0.5;

    // Turbo driving speed — held with right bumper
    private static final double TURBO_SPEED = 1.0;


    // -----------------------------------------------------------------------
    // STEP 3: runOpMode()
    //   This is the main method FTC calls when you press INIT on the Driver Station.
    //   It runs once, and contains two phases:
    //     • INIT  – everything before waitForStart()
    //     • RUN   – the loop that runs while the match is active
    // -----------------------------------------------------------------------
    @Override
    public void runOpMode() {

        // -------------------------------------------------------------------
        // INIT PHASE
        // Initialize (set up) the four motors by name.
        // The string name must EXACTLY match what is configured in the
        // Driver Station robot configuration file.
        // -------------------------------------------------------------------
        frontLeftMotor  = hardwareMap.get(DcMotor.class, "front_left");
        frontRightMotor = hardwareMap.get(DcMotor.class, "front_right");
        backLeftMotor   = hardwareMap.get(DcMotor.class, "back_left");
        backRightMotor  = hardwareMap.get(DcMotor.class, "back_right");

        // -------------------------------------------------------------------
        // Set motor directions.
        //
        // Because the left and right motors face opposite directions on the
        // robot, we reverse the left side so that positive power always means
        // "forward" for every motor.
        //
        // If your robot drives backwards, swap REVERSE and FORWARD here.
        // -------------------------------------------------------------------
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.FORWARD);

        // -------------------------------------------------------------------
        // Set zero-power behavior.
        // BRAKE  → motors resist movement when power = 0 (robot stops sharply)
        // FLOAT  → motors spin freely when power = 0  (robot coasts)
        // BRAKE is usually safer for beginners.
        // -------------------------------------------------------------------
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Tell the driver the robot is ready
        telemetry.addData("Status", "Initialized — waiting for START");
        telemetry.update();

        // -------------------------------------------------------------------
        // Wait here until the driver presses START on the Driver Station.
        // Nothing below this line runs until the match begins.
        // -------------------------------------------------------------------
        waitForStart();

        // -------------------------------------------------------------------
        // RUN PHASE
        // opModeIsActive() returns true while the match timer is running.
        // The loop keeps repeating until time runs out or STOP is pressed.
        // -------------------------------------------------------------------
        while (opModeIsActive()) {

            // ---------------------------------------------------------------
            // Read gamepad inputs
            //
            // Gamepad axes return a value between -1.0 and +1.0.
            // We negate the Y axes because pushing the stick forward gives a
            // NEGATIVE value by default (up = negative in screen coordinates).
            // ---------------------------------------------------------------
            double drive  = -gamepad1.left_stick_y;   // Forward / Backward
            double strafe =  gamepad1.left_stick_x;   // Left / Right (strafe)
            double rotate =  gamepad1.right_stick_x;  // Rotate (turn in place)

            // ---------------------------------------------------------------
            // Choose speed multiplier
            // Hold the right bumper for turbo (full speed).
            // ---------------------------------------------------------------
            double speedMultiplier = gamepad1.right_bumper ? TURBO_SPEED : NORMAL_SPEED;

            // ---------------------------------------------------------------
            // Calculate motor powers — Mecanum wheel math
            //
            // Each wheel gets a combination of drive, strafe, and rotate.
            // The +/- signs come from the geometry of mecanum rollers:
            //
            //   Front Left  =  drive + strafe + rotate
            //   Front Right =  drive - strafe - rotate
            //   Back  Left  =  drive - strafe + rotate
            //   Back  Right =  drive + strafe - rotate
            //
            // Think of it this way:
            //   • All four wheels positive  → move forward
            //   • FL & BR positive, FR & BL negative → strafe right
            //   • Left side positive, right side negative → rotate right
            // ---------------------------------------------------------------
            double flPower = (drive + strafe + rotate) * speedMultiplier;
            double frPower = (drive - strafe - rotate) * speedMultiplier;
            double blPower = (drive - strafe + rotate) * speedMultiplier;
            double brPower = (drive + strafe - rotate) * speedMultiplier;

            // ---------------------------------------------------------------
            // Normalize motor powers
            //
            // If any calculated power exceeds 1.0 or -1.0, we scale ALL
            // motors down proportionally so the robot still steers correctly.
            // ---------------------------------------------------------------
            double maxPower = Math.max(
                    Math.max(Math.abs(flPower), Math.abs(frPower)),
                    Math.max(Math.abs(blPower), Math.abs(brPower))
            );

            if (maxPower > 1.0) {
                flPower /= maxPower;
                frPower /= maxPower;
                blPower /= maxPower;
                brPower /= maxPower;
            }

            // ---------------------------------------------------------------
            // Clamp powers as a safety net using Range.clip()
            // This ensures values stay between -1.0 and 1.0 no matter what.
            // ---------------------------------------------------------------
            flPower = Range.clip(flPower, -1.0, 1.0);
            frPower = Range.clip(frPower, -1.0, 1.0);
            blPower = Range.clip(blPower, -1.0, 1.0);
            brPower = Range.clip(brPower, -1.0, 1.0);

            // ---------------------------------------------------------------
            // Send the calculated power to each motor
            // ---------------------------------------------------------------
            frontLeftMotor.setPower(flPower);
            frontRightMotor.setPower(frPower);
            backLeftMotor.setPower(blPower);
            backRightMotor.setPower(brPower);

            // ---------------------------------------------------------------
            // Telemetry — send information to the Driver Station screen
            // This is very helpful for debugging!
            // ---------------------------------------------------------------
            telemetry.addData("--- Gamepad Inputs ---", "");
            telemetry.addData("Drive  (Fwd/Back)", "%.2f", drive);
            telemetry.addData("Strafe (L/R)     ", "%.2f", strafe);
            telemetry.addData("Rotate           ", "%.2f", rotate);
            telemetry.addData("Turbo            ", gamepad1.right_bumper ? "ON" : "off");

            telemetry.addData("--- Motor Powers ---", "");
            telemetry.addData("Front Left  ", "%.2f", flPower);
            telemetry.addData("Front Right ", "%.2f", frPower);
            telemetry.addData("Back  Left  ", "%.2f", blPower);
            telemetry.addData("Back  Right ", "%.2f", brPower);

            telemetry.update();

        } // end while loop

        // -------------------------------------------------------------------
        // Safety: stop all motors when the OpMode ends
        // -------------------------------------------------------------------
        stopAllMotors();

    } // end runOpMode()


    // -----------------------------------------------------------------------
    // HELPER METHOD: stopAllMotors()
    //
    // A helper method is a reusable block of code we can call by name.
    // Here we set every motor to 0 power (stopped) in one place.
    // -----------------------------------------------------------------------
    private void stopAllMotors() {
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backLeftMotor.setPower(0);
        backRightMotor.setPower(0);
    }

} // end class TeleOpDrive