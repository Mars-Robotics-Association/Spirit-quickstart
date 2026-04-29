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
 * Main teleop OpMode for Team Spirit's robot.
 *
 * <p><b>Gamepad 1 (driver):</b>
 * <ul>
 *   <li>Left stick — mecanum drive (forward/back and strafe)</li>
 *   <li>Right stick X — rotation</li>
 *   <li>Right trigger — intake + carousel advance sequence (3-step cycle)</li>
 *   <li>A / B / X — tilt servo to near / far / home</li>
 *   <li>D-pad up/right/down — carousel to launch positions 1/2/3</li>
 * </ul>
 *
 * <p><b>Gamepad 2 (operator):</b>
 * <ul>
 *   <li>Right trigger — near shot launch sequence</li>
 *   <li>Left trigger — far shot launch sequence (TODO)</li>
 *   <li>B / X / Y — kicker home / tiny / full (testing)</li>
 *   <li>D-pad — carousel to intake/home positions (testing)</li>
 *   <li>Back button — kill switch (stops shooter, returns to IDLE)</li>
 * </ul>
 *
 * <p>The launch sequence is a 17-step timed state machine (IDLE → RAMPING → LAUNCHING)
 * that spins up the flywheels, then cycles through all three carousel positions —
 * tilting, kicking, and resetting for each ball.
 */
@Config
@TeleOp(name = "TeleopTimmy", group = "Teleop")
public class TeleopTimmy extends LinearOpMode {

    int launchStep = 0;//controls progress through the launch sequence
    double stepStartTime = 0;//a timer used to create a delay in each step of the launch sequence
    public static double launchStepDelay = .8;//the delay between each step in the launch sequence in seconds

    // VARIABLES USED IN INTAKE SEQUENCE
    int intakeStep = 0;          // cycles 0 → 1 → 2 → 0
    boolean triggerHeld = false; // edge detection

    @Override
    public void runOpMode() {
        telemetry.clear();
        double rampUpTimer = 0;//a timer used to create a delay to let the launch wheels ramp up to speed

        State currentState = State.IDLE;//the state of the launch sequence
        double tiltPosition = 0;//the vertical position of the tilt


        //Instantiating the classes that TeleopTimmy needs to use
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0), false);
        ShooterTimmy shooterTimmy   = new ShooterTimmy(hardwareMap);
        CarouselTimmy carouselTimmy = new CarouselTimmy(hardwareMap);
        Lift lift = new Lift(hardwareMap);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        //Instantiage the class used for the dashboard
        shooterTimmy.setHomeTiltPosition();
        lift.homeLift();//the home position of the lift. We will not use this in the class
        waitForStart();//makes code execution stop until "start" is pressed on the driver's station


        //this is the loop that runs continually until the robot is stopped
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
                        shooterTimmy.shooterPower = ShooterTimmy.nearShooterPower;
                        shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                        tiltPosition = shooterTimmy.nearTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;  // 2-second spin-up timer
                        currentState = State.RAMPING;
                    }

                    // TODO: Far shot — left trigger pulled, right trigger not pulled
                    // Use the near shot code above as a guide and fill in the if statement below.
                    if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1) {
                        shooterTimmy.shooterPower = ShooterTimmy.farShooterPower;
                        shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                        tiltPosition = shooterTimmy.farTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;  // 2-second spin-up timer
                        currentState = State.RAMPING;
                    }
                    break;

                // RAMPING — waiting for flywheel to reach speed
                case RAMPING:
                    shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                    if (getRuntime() > rampUpTimer) {
                        currentState = State.LAUNCHING;
                        launchStep = 0;
                        shooterTimmy.setHomeTiltPosition();
                        carouselTimmy.setHomePositionKicker();
                        stepStartTime = getRuntime();
                    }
                    break;

                // LAUNCHING — timed step machine, one step per loop iteration
                case LAUNCHING:
                    switch (launchStep) {

                        // STEP 0 — rotate carousel to launch position 1
                        case 0:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.spinCarouselLaunchOne();
                                //shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            if (getRuntime() - stepStartTime > launchStepDelay + .2) {
                                carouselTimmy.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 2 — tilt shooter
                        case 2:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                shooterTimmy.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 3 — full kicker (launch ball 1)
                        case 3:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 4 — reset tilt
                        case 4:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                shooterTimmy.setHomeTiltPosition();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;
                          // STEP 5 - reset kicker
                        case 5:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;
                        // STEP 6 — rotate carousel to launch position 2
                        case 6:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.spinCarouselLaunchTwo();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 7 — tiny kicker (2nd ball)
                        case 7:
                            if (getRuntime() - stepStartTime > launchStepDelay + .2) {
                                carouselTimmy.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 8 — tilt shooter (2nd ball)
                        case 8:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                shooterTimmy.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 9 — full kicker (launch ball 2)
                        case 9:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 10 — reset tilt
                        case 10:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                shooterTimmy.setHomeTiltPosition();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 11 — reset kicker
                        case 11:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 12 — rotate carousel to launch position 3
                        case 12:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.spinCarouselLaunchThree();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 13 — tiny kicker (3rd ball)
                        case 13:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 14 — tilt shooter (3rd ball)
                        case 14:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                shooterTimmy.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 15 — full kicker (launch ball 3)
                        case 15:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselTimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 16 — reset tilt (extra delay so kicker clears carousel)
                        case 16:
                            if (getRuntime() - stepStartTime > launchStepDelay + .2) {
                                shooterTimmy.setHomeTiltPosition();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 17 — lower kicker
                        case 17:
                            if (getRuntime() - stepStartTime > launchStepDelay + .5) {
                                carouselTimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 18 — spin carousel home
                        case 18:
                            if (getRuntime() - stepStartTime > launchStepDelay + .5) {
                                carouselTimmy.spinCarouselLaunchOne();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 19 — stop shooter motors
                        case 19:
                            if (getRuntime() - stepStartTime > launchStepDelay + .75) {
                                shooterTimmy.shooterPower = 0;
                                shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
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
                shooterTimmy.shooterMotorLeft.setPower(0);
                shooterTimmy.shooterMotorRight.setPower(0);
            }

            // TILT SERVO TESTING (Gamepad 1)
            if (gamepad1.a) {
                shooterTimmy.setNearTiltPosition(shooterTimmy.nearTiltPosition);
            }
            if (gamepad1.b) {
                shooterTimmy.setFarTiltPosition(shooterTimmy.farTiltPosition);
            }
            if (gamepad1.x) {
                shooterTimmy.setHomeTiltPosition();
            }

            // KICKER TESTING (Gamepad 2)
            if (gamepad2.b) {
                carouselTimmy.setHomePositionKicker();
            }
            if (gamepad2.x) {
                carouselTimmy.setTinyKicker();
            }
            if (gamepad2.y) {
                carouselTimmy.setFullKicker();
            }

            // CAROUSEL TESTING (Gamepad 2 D-pad)
            if (gamepad2.dpad_left) {
                carouselTimmy.spinCarouselHome();
            }
            if (gamepad2.dpad_up) {
                carouselTimmy.spinCarouselIntakeOne();
            }
            if (gamepad2.dpad_right) {
                carouselTimmy.spinCarouselIntakeTwo();
            }
            if (gamepad2.dpad_down) {
                carouselTimmy.spinCarouselIntakeThree();
            }

            // CAROUSEL LAUNCH POSITIONS (Gamepad 1 D-pad)
            if (gamepad1.dpad_up) {
                carouselTimmy.spinCarouselLaunchOne();
            }
            if (gamepad1.dpad_right) {
                carouselTimmy.spinCarouselLaunchTwo();
            }
            if (gamepad1.dpad_down) {
                carouselTimmy.spinCarouselLaunchThree();
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
                    carouselTimmy.spinCarouselIntakeOne();
                    intakeStep = 1;
                } else if (intakeStep == 1) {
                    carouselTimmy.spinCarouselIntakeTwo();
                    intakeStep = 2;
                } else if (intakeStep == 2) {
                    carouselTimmy.spinCarouselIntakeThree();
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
            telemetry.addData("Shooter Left Power",  shooterTimmy.shooterMotorLeft.getPower());
            telemetry.addData("Shooter Right Power", shooterTimmy.shooterMotorRight.getPower());
            telemetry.addData("Shooter Set Power",   shooterTimmy.shooterPower);
            telemetry.update();
        }
    }

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING
    }
}
