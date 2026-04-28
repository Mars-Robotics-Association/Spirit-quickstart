package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * TeleOpDrive - Introduction to FTC Robot Programming
 *
 * This OpMode drives a 4-wheel mecanum robot using Gamepad 1.
 *
 * MOTOR LAYOUT (top-down view):
 *
 *        FRONT OF ROBOT
 *   [frontLeftMotor]      [frontRightMotor]
 *   [backLeftMotor]      [backRightMotor]
 *        BACK OF ROBOT
 *
 * GAMEPAD CONTROLS:
 *   Left  Stick Y  → Forward / Backward
 *   Left  Stick X  → Strafe Left / Right   (side-to-side)
 *   Right Stick X  → Rotate Left / Right
 */

@TeleOp(name = "TeleopDriveJimmy", group = "Intro")
public class TeleopDriveJimmy extends LinearOpMode {

    // -----------------------------------------------------------------------
    // STEP 1: Declare our motor variables
    //   DcMotor is the class that controls a motor plugged into the Control Hub.
    //   We declare one variable per motor.
    // -----------------------------------------------------------------------
    private DcMotor leftFront;
    //TODO: declare the rightFront motor
    //TODO: declare the leftBack motor
    //TODO: declare the rightBack motor

    // -----------------------------------------------------------------------
    // STEP 2: runOpMode()
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
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        //TODO: map the rightFront motor
        //TODO: map the leftBack motor
        //TODO: map the right motorBack

        // -------------------------------------------------------------------
        // Set motor directions.
        //
        // Because the left and right motors sometimes face directions on the
        // robot, we set the direction on the motors so that positive power always means
        // "forward" for every motor.
        //
        // If your robot is not driving correctly, check the direction of the wheels (motors).
        //It can be helpful to put the robot on blocks to check the direction of the wheels (motors).
        // -------------------------------------------------------------------
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        //TODO: set the direction of the rightFront motor to FORWARD
        //TODO: set the direction of the leftBack motor to REVERSE
        //TODO: set the direction of the rightBack motor to FORWARD

        // -------------------------------------------------------------------
        // Set zero-power behavior.
        // BRAKE  → motors resist movement when power = 0 (robot stops sharply)
        // FLOAT  → motors spin freely when power = 0  (robot coasts)
        // BRAKE is usually safer for beginners.
        // -------------------------------------------------------------------
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //TODO:set zero power behavior of rightFront motor
        //TODO:set zero power behavior of leftBack motor
        //TODO:set zero power behavior of rightBack motor

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
            // Gamepad x and y axes return a value between -1.0 and +1.0.
            // We negate the Y axes because pushing the stick forward gives a
            // NEGATIVE value by default (up = negative in screen coordinates).
            // ---------------------------------------------------------------
            double drive  = -gamepad1.left_stick_y;   // Forward / Backward
            double strafe =  gamepad1.left_stick_x;   // Left / Right (strafe)
            double rotate =  gamepad1.right_stick_x;  // Rotate (turn in place)

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
            double frontLeftPower = (drive + strafe + rotate);
            double frontRightPower = (drive - strafe - rotate);
            double rearLeftPower = (drive - strafe + rotate);
            double rearRightPower = (drive + strafe - rotate);

            // ---------------------------------------------------------------
            // Send the calculated power to each motor
            // ---------------------------------------------------------------
            leftFront.setPower(frontLeftPower);
            //TODO: set the power to the rightFront motor
            //TODO: set the power to the leftBack motor
            //TODO: set the power to the rightBack motor

            // ---------------------------------------------------------------
            // Telemetry — send information to the Driver Station screen
            // This is very helpful for debugging!
            // ---------------------------------------------------------------
            telemetry.addData("--- Gamepad Inputs ---", "");
            telemetry.addData("Drive  (Fwd/Back)", "%.2f", drive);
            telemetry.addData("Strafe (L/R)     ", "%.2f", strafe);
            telemetry.addData("Rotate           ", "%.2f", rotate);

            telemetry.addData("--- Motor Powers ---", "");
            telemetry.addData("Front Left  ", frontLeftPower);
            telemetry.addData("Front Right ", frontRightPower);
            telemetry.addData("Back  Left  ", rearLeftPower);
            telemetry.addData("Back  Right ", rearRightPower);

            telemetry.update();

        } // end while loop

        // -------------------------------------------------------------------
        // Safety: stop all motors when the OpMode ends
        // -------------------------------------------------------------------
        stopAllMotors();//this calls the method stopAllMotors() below

    } // ends runOpMode()

    // -----------------------------------------------------------------------
    // HELPER METHOD: stopAllMotors()
    //
    // A helper method is a reusable block of code we can call by name.
    // Here we set every motor to 0 power (stopped) in one place.
    // -----------------------------------------------------------------------
    private void stopAllMotors() {
        leftFront.setPower(0);
        rightFront.setPower(0);//TODO: set the power to the rightFront motor to 0
        leftBack.setPower(0);//TODO: set the power to the leftBack motor to 0
        rightBack.setPower(0);//TODO: set the power to the rightBack motor to 0
    }

} // end class TeleOpDrive