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

                    // Far shot: left trigger pulled, right trigger not pulled
                    // (The condition is written this way so nothing happens if both are pulled at once)
                    if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1) {
                        intakeJimmy.setPower(-0.2);
                        shooterJimmy.shooterPower = ShooterJimmy.farShooterPower;
                        shooterJimmy.setShooterPower(ShooterJimmy.shooterPower, telemetry);
                        rampUpTimer = getRuntime() + 2.0;  // 2-second spin-up timer
                        currentState = State.RAMPING;
                    }
                    break;

                // RAMPING — waiting for flywheel to reach speed
                case RAMPING:
                    shooterJimmy.setShooterPower(shooterJimmy.shooterPower, telemetry);
                    if (getRuntime() > rampUpTimer) {
                        currentState = State.LAUNCHING;
                        launchStep = 0;
                        carouselJimmy.setHomePositionKicker();
                        stepStartTime = getRuntime();
                    }
                    break;

                // LAUNCHING — timed step machine, one step per loop iteration
                case LAUNCHING:
                    switch (launchStep) {

                        // STEP 0 — rotate carousel to launch position 1
                        case 0:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.spinCarouselLaunchOne();
                                //shooterJimmy.setShooterPower(shooterJimmy.shooterPower, telemetry);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 1 — full kicker (launch ball 1)
                        case 1:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 2 — lower kicker
                        case 2:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 3 — rotate carousel to launch position 2
                        case 3:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.spinCarouselLaunchTwo();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 4 — full kicker (launch ball 2)
                        case 4:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 5 — lower kicker
                        case 5:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 6 — rotate carousel to launch position 3
                        case 6:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.spinCarouselLaunchThree();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 7 — full kicker (launch ball 3)
                        case 7:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 8 — lower kicker and spin carousel home
                        case 8:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 9 — lower kicker and spin carousel home
                        case 9:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
                                carouselJimmy.spinCarouselLaunchOne();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 10 — stop shooter motors
                        case 10:
                            if (getRuntime() - stepStartTime > launchStepDelay) {
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
