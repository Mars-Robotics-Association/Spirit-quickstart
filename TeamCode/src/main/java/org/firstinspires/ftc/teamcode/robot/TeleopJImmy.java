package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Drawing;

/**
 * Main teleop OpMode for Jimmy's robot.
 *
 * <p><b>Gamepad 1 (driver):</b>
 * <ul>
 *   <li>Left stick — mecanum drive (forward/back and strafe)</li>
 *   <li>Right stick X — rotation</li>
 *   <li>Right trigger — intake + carousel advance sequence (3-step cycle)</li>
 *   <li>D-pad up/right/down — carousel to launch positions 1/2/3</li>
 * </ul>
 *
 * <p><b>Gamepad 2 (operator):</b>
 * <ul>
 *   <li>Left bumper — intake in</li>
 *   <li>Right bumper — intake eject</li>
 *   <li>Right trigger — near shot launch sequence</li>
 *   <li>Left trigger — far shot launch sequence</li>
 *   <li>B / Y — kicker home / full (testing)</li>
 *   <li>D-pad — carousel to intake/home positions (testing)</li>
 *   <li>Back button — kill switch (stops shooter, returns to IDLE)</li>
 * </ul>
 *
 * <p>The launch sequence is a 10-step timed state machine (IDLE → RAMPING → LAUNCHING)
 * that spins up the flywheels, then cycles through all three carousel positions —
 * kicking and resetting for each ball.
 */
@Config
@TeleOp(name = "TeleopJimmy", group = "Teleop")
public class TeleopJImmy extends LinearOpMode {

    int launchStep = 0;
    double stepStartTime = 0;
    public static double launchStepDelay = .6;

    // VARIABLES USED IN INTAKE SEQUENCE
    int intakeStep = 0;          // cycles 0 → 1 → 2 → 0
    boolean triggerHeld = false; // edge detection

    @Override
    public void runOpMode() {
        telemetry.clear();
        double rampUpTimer = 0;

        State currentState = State.IDLE;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0), true);
        IntakeJimmy intakeJimmy     = new IntakeJimmy(hardwareMap);
        ShooterJimmy shooterJimmy   = new ShooterJimmy(hardwareMap);
        CarouselJimmy carouselJimmy = new CarouselJimmy(hardwareMap);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        waitForStart();

        while (opModeIsActive()) {

            // -----------------------------------------------------------------------
            //  STATE MACHINE — controls the flywheel spin-up and launch sequence
            // -----------------------------------------------------------------------
            switch (currentState) {

                // IDLE — waiting for trigger input
                case IDLE:
                    // Near shot: right trigger pulled, left trigger not pulled
                    // (The condition is written this way so nothing happens if both are pulled at once)
                    if (gamepad2.right_trigger > 0.25 && gamepad2.left_trigger < 0.1) {
                        intakeJimmy.setPower(-0.2);
                        shooterJimmy.shooterPower = ShooterJimmy.nearShooterPower;
                        shooterJimmy.setShooterPower(shooterJimmy.shooterPower, telemetry);
                        rampUpTimer = getRuntime() + 2.0;  // 2-second spin-up timer
                        currentState = State.RAMPING;
                    }

                    // TODO: Far shot — left trigger pulled, right trigger not pulled
                    // Use the near shot code above as a guide and fill in the if statement below.
                    if (false) { // TODO: replace 'false' with the correct condition
                        // TODO: set shooterJimmy.shooterPower to ShooterTimmy.farShooterPower
                        // TODO: call shooterJimmy.setShooterPower(...)
                        // TODO: set tiltPosition to shooterJimmy.farTiltPosition
                        // TODO: set rampUpTimer to getRuntime() + 2.0
                        // TODO: set currentState to State.RAMPING
                    }
                    break;

                // RAMPING — waiting for flywheel to reach speed
                case RAMPING:
                    shooterJimmy.setShooterPower(shooterJimmy.shooterPower, telemetry);//applies power to the launch motors
                    if (getRuntime() > rampUpTimer) {//creates a 2 second delay, remember the value of the variable rampUpTimer is 2 seconds
                        currentState = State.LAUNCHING;//changes the state to LAUNCHING
                        launchStep = 0;//sets the launch step to 0
                        carouselJimmy.setHomePositionKicker();//sets the kicker to the home (down) position
                        stepStartTime = getRuntime();//sets the timer for the delay between each launch step
                    }
                    break;

                // LAUNCHING — timed step machine, one step per loop iteration
                case LAUNCHING:
                    switch (launchStep) {

                        // STEP 0 — rotate carousel to launch position 1
                        case 0:
                            if (getRuntime() - stepStartTime > launchStepDelay) {//creates a delay
                                carouselJimmy.spinCarouselLaunchOne();//rotates the carousel to launch position one
                                stepStartTime = getRuntime();//resets the timer
                                launchStep++;//increases the launch step
                            }
                            break;

                        // STEP 1 — full kicker (launch ball 1)
                        case 1:
                            if (getRuntime() - stepStartTime > launchStepDelay) {//creates a delay
                                carouselJimmy.setFullKicker();//lifts the kicker the whole way
                                stepStartTime = getRuntime();//resets the timer
                                launchStep++;//increases the launch step
                            }
                            break;

                        // STEP 2 — lower kicker
                        case 2:
                            if (false) {//TODO: remove the word false and insert the condition to test -hint: if (getRuntime() - stepStartTime > launchStepDelay)
                                //TODO: lower the kicker, hint: call carouselJimmy.setHomePositionKicker()
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 3 — rotate carousel to launch position 2
                        case 3:
                            if (false) {//TODO: remove the word false and insert the condition to test
                                //TODO: rotate carousel to launch position 2
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 4 — full kicker (launch ball 2)
                        case 4:
                            if (false) {//TODO: remove the word false and insert the condition to test
                                //TODO: lift the kicker the whole way
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 5 — lower kicker
                        case 5:
                            if (false) {//TODO: remove the word false and insert the condition to test
                                //TODO: lower the kicker to the home (down) position
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 6 — rotate carousel to launch position 3
                        case 6:
                            if (false) {//TODO: remove the word false and insert the condition to test
                                //TODO: rotate carousel to launch postion 3
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 7 — full kicker (launch ball 3)
                        case 7:
                            if (false) {//TODO: remove the word false and insert the condition to test
                                //TODO: lift the kicker the whole way
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 8 — lower kicker
                        case 8:
                            if (false) {//TODO: remove the word false and insert the condition to test
                            // TODO: lower the kicker to home (down) position
                            //TODO: reset timer
                            // TODO: increase launch step
                            }
                            break;

                        // STEP 9 — spin carousel to launch position one to get ready for next launch sequence
                        case 9:
                            if (false) {//TODO: remove the word false and insert the condition to test
                                //TODO: rotate carousel to launch position one
                                //TODO: reset timer
                                // TODO: increase launch step
                            }
                            break;

                        // STEP 10 — stop shooter motors
                        case 10:
                            if (getRuntime() - stepStartTime > launchStepDelay){
                                shooterJimmy.shooterPower = 0;
                                shooterJimmy.setShooterPower(shooterJimmy.shooterPower, telemetry);
                                intakeJimmy.setPower(0.0);
                                currentState = State.IDLE; // return to idle — keep this line
                            }
                            break;
                    }
                    break;
            }
            // -----------------------------------------------------------------------
            //  END STATE MACHINE
            // -----------------------------------------------------------------------

            // KILL SWITCH — stops shooter and returns to IDLE immediately
            if (gamepad2.back) {
                currentState = State.IDLE;
                launchStep = 0;
                shooterJimmy.shooterMotorLeft.setPower(0);
                shooterJimmy.shooterMotorRight.setPower(0);
            }

            // KICKER TESTING (Gamepad 2)
            if (gamepad2.b) {
                carouselJimmy.setHomePositionKicker();
            }
            if (gamepad2.y) {
                carouselJimmy.setFullKicker();
            }

            // INTAKE (Gamepad 2 bumpers)
            if (gamepad2.left_bumper) {
                intakeJimmy.setPower(1);
            } else if (gamepad2.right_bumper) {
                intakeJimmy.setPower(-1);
            } else if (gamepad2.leftBumperWasReleased()||gamepad2.rightBumperWasReleased()){
                intakeJimmy.setPower(0);
            }

            // CAROUSEL TESTING (Gamepad 2 D-pad)
            if (gamepad2.dpad_left) {
                carouselJimmy.spinCarouselHome();
            }
            if (gamepad2.dpad_up) {
                carouselJimmy.spinCarouselIntakeOne();
            }
            if (gamepad2.dpad_right) {
                carouselJimmy.spinCarouselIntakeTwo();
            }
            if (gamepad2.dpad_down) {
                carouselJimmy.spinCarouselIntakeThree();
            }

            // CAROUSEL LAUNCH POSITIONS (Gamepad 1 D-pad)
            if (gamepad1.dpad_up) {
                carouselJimmy.spinCarouselLaunchOne();
            }
            if (gamepad1.dpad_right) {
                carouselJimmy.spinCarouselLaunchTwo();
            }
            if (gamepad1.dpad_down) {
                carouselJimmy.spinCarouselLaunchThree();
            }

            // -----------------------------------------------------------------------
            //  INTAKE SEQUENCE (Gamepad 1 right trigger)
            //  Each trigger pull advances the carousel one slot (cycles through all 3).
            // -----------------------------------------------------------------------
            boolean triggerPressed = gamepad1.right_trigger > 0.25;

            // Detect a new trigger pull (rising edge only)
            if (triggerPressed && !triggerHeld) {
                triggerHeld = true;
                if (intakeStep == 0) {
                    carouselJimmy.spinCarouselIntakeOne();
                    intakeStep = 1;
                } else if (intakeStep == 1) {
                    carouselJimmy.spinCarouselIntakeTwo();
                    intakeStep = 2;
                } else if (intakeStep == 2) {
                    carouselJimmy.spinCarouselIntakeThree();
                    intakeStep = 0;
                }
            }

            // Detect trigger release
            if (!triggerPressed && triggerHeld) {
                triggerHeld = false;
            }
            // -----------------------------------------------------------------------
            //  END INTAKE SEQUENCE
            // -----------------------------------------------------------------------

            // DRIVE
            drive.setDrivePowers(new PoseVelocity2d(
                    new Vector2d(
                            -gamepad1.left_stick_y,
                            -gamepad1.left_stick_x
                    ),
                    -gamepad1.right_stick_x
            ));
            drive.updatePoseEstimate();

            // DASHBOARD FIELD OVERLAY
            Pose2d pose = drive.localizer.getPose();
            TelemetryPacket packet = new TelemetryPacket();
            packet.fieldOverlay().setStroke("#3F51B5");
            Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);

            // TELEMETRY
            telemetry.addData("Shooter Left Power",  shooterJimmy.shooterMotorLeft.getPower());
            telemetry.addData("Shooter Right Power", shooterJimmy.shooterMotorRight.getPower());
            telemetry.addData("Shooter Set Power",   shooterJimmy.shooterPower);
            telemetry.update();
        }
    }

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING
    }
}
