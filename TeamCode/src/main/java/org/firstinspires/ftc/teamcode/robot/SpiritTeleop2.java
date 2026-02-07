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
 *   <li>D-pad left — home the lift</li>
 * </ul>
 *
 * <p><b>Gamepad 2 (operator):</b>
 * <ul>
 *   <li>Left/right bumper — intake in / eject</li>
 *   <li>Right trigger — near shot launch sequence</li>
 *   <li>Left trigger — far shot launch sequence</li>
 *   <li>A — engage lift</li>
 *   <li>B / X / Y — kicker home / tiny / full (testing)</li>
 *   <li>D-pad — carousel to intake/home positions (testing)</li>
 * </ul>
 *
 * <p>The launch sequence is a timed state machine (IDLE &rarr; RAMPING &rarr;
 * LAUNCHING) that spins up the flywheels, then loops through all three carousel
 * positions — rotating, tilting, kicking, and resetting for each ball.
 */
@Config
@TeleOp(name = "SpiritTeleop2", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {
    public double launchSequenceTimer = 0;
    //public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int ballNumber = 0;
    int launchSubStep = 0;
    double stepStartTime = 0;
    static public double defaultLaunchStepDelay = 1;//.4 will work for competition
    static public double tiltToLaunchDelay = 1.0;

    //VARIABLES USED IN INTAKE SEQUENCE--------------------------------------
    int intakeStep = 0;                 // 0 → 1 → 2
    boolean triggerHeld = false;        // edge detection
    double intakePower = 1.0;
    //-------------------------------------------
    @Override
    public void runOpMode() {
//        telemetry.setAutoClear(false);
        telemetry.clear();
        double rampUpTimer = 0;
        double driveTimer = 0;
        boolean rampUpFlag = false;

        //double shooterPower = 0;//power for shooter which is set for near or far shot based on which trigger is pulled
        State currentState = State.IDLE;

        double tiltPosition = 0;
        //double homeTiltPosition = 0;

        telemetry = new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);
        Shooter shooter = new Shooter(hardwareMap, telemetry);
        Carousel carousel = new Carousel(hardwareMap);
        Lift lift = new Lift(hardwareMap);


        shooter.setHomeTiltPosition();
        lift.homeLift();
        waitForStart();

        while (opModeIsActive()){
/*
                //KILL BUTTON*****************************************************************
                if(gamepad1.a){
                    drive.setDrivePowers(new PoseVelocity2d(
                            new Vector2d(0, 0

                            ),
                            0
                    ));
                }
//END KILL BUTTON************************************************************
*/
//LIFT ROBOT OFF OF MATT****************************************************

            if (gamepad2.a){//(gamepad2.x  && gamepad2.b ){
                lift.engageLift();

            }

            //reset lift position
            if (gamepad1.dpad_left) {
                lift.homeLift();
            }

            telemetry.addData("Test", 0);
            //Operate Intake: left bumper is intake and right bumper is eject
            if (gamepad2.left_bumper) {
                intake.setPower(1);
            } else if (gamepad2.right_bumper) {
                intake.setPower(-1);
            } else {
                intake.setPower(0);
            }

            //JUST FOR TESTING POSITON OF KICKER SERVO-------

            if (gamepad2.b) {
                carousel.setHomePositionKicker();
            }
            if (gamepad2.x) {
                carousel.setTinyKicker();
            }
            if (gamepad2.y) {
                carousel.setFullKicker();
            }
            //JUST FOR TESTING POSITION OF TILT SERVO
            if (gamepad1.a) {
                shooter.setNearTiltPosition(shooter.nearTiltPosition);

            }

            if (gamepad1.b) {
                shooter.setFarTiltPosition(shooter.farTiltPosition);

            }
            if (gamepad1.x) {
                shooter.setHomeTiltPosition();
            }

//--------------------------------END CODE FOR TESTING POSITION OF TILT SERVO

//------------------JUST FOR TESTING POSITION OF CAROUSEL
            //if (gamepad2.dpad_left) {
            //carousel.spinCarouselMin();//this is 0

            //}
            if (gamepad2.dpad_left) {
                carousel.spinCarouselHome();//

            }
            // if (gamepad2.dpad_up) {
            // carousel.spinCarouselMax();//this is 1

            //}
            if (gamepad2.dpad_up) {
                carousel.spinCarouselIntakeOne();//

            }

            if (gamepad2.dpad_right) {
                carousel.spinCarouselIntakeTwo();//

            }

            if (gamepad2.dpad_down) {
                carousel.spinCarouselIntakeThree();//

            }

            if (gamepad1.dpad_up) {
                carousel.spinCarouselLaunchOne();

            }
            if (gamepad1.dpad_right) {
                carousel.spinCarouselLaunchTwo();
            }

            if (gamepad1.dpad_down) {
                carousel.spinCarouselLaunchThree();
            }
            //--------------------------END TESTING OF CAROUSEL

//TEST LIFT
            if (gamepad1.dpad_left) {
                lift.homeLift();
            }



                // ---------------- INTAKE + CAROUSEL SEQUENCE (GAMEPAD 1 RIGHT TRIGGER) ----------------
                /*
                This section of the code is an intake sequence. It cycles 3 times.  Upon pulling the trigger
                on gamepad 1, the intake begins spinning and the carousel spins to intakePositionOne.
                On the second trigger pull, the intake spins and the carousel advances to intakePositionTwo.
                On the third trigger pull, the intake spins and the carousel advances to intakePositionThree,
                then resets for the next cycle.
                */

            boolean triggerPressed = gamepad1.right_trigger > 0.25;

            // While trigger is held, keep intake running
            if (triggerPressed) {
                intake.setPower(intakePower);
            }

            // Detect NEW trigger pull (rising edge)
            if (triggerPressed && !triggerHeld) {
                triggerHeld = true;

                // Advance carousel one position per pull
                if (intakeStep == 0) {
                    intake.setPower(intakePower);
                    carousel.spinCarouselIntakeOne();
                    intakeStep = 1;
                } else if (intakeStep == 1) {
                    intake.setPower(intakePower);
                    carousel.spinCarouselIntakeTwo();
                    intakeStep = 2;
                } else if (intakeStep == 2) {
                    intake.setPower(intakePower);
                    carousel.spinCarouselIntakeThree();
                    intakeStep = 0;
                }


            }

            // Detect trigger release
            if (!triggerPressed && triggerHeld) {
                triggerHeld = false;
                intake.setPower(0);   // Stop intake when released
            }
            //END INTAKE SEQUENCE---------------------------------------

            //MANUAL LOAD-----------------------------
            if (gamepad2.a) {
                carousel.setHomePositionKicker();
                carousel.setHomePositionKicker();
                carousel.spinCarouselLaunchOne();
                carousel.setTinyKicker();
            }
            //END MANUAL LOAD-----------------------------------------------

            if (gamepad2.b) {
                carousel.setHomePositionKicker();
            }

            //CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------
            switch (currentState) {

                // ------------------------------------------------------
                //  IDLE — waiting for trigger input
                // ------------------------------------------------------
                case IDLE:
                    // Near shot (right trigger)
                    if (gamepad2.right_trigger > 0.25 && gamepad2.left_trigger < 0.1) {
                        //shooter.shooterVelocity = shooter.nearShooterVelocity;
                        shooter.shooterMotorLeft.setPower(.3);
                        shooter.shooterMotorRight.setPower(.3);
                        shooter.shooterVelocity = Shooter.nearShooterVelocity;
                        shooter.update();
                        tiltPosition = shooter.nearTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;   // 2-second spin-up
                        currentState = State.RAMPING;

                    }

                    // Far shot (left trigger)
                    if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1){
                        shooter.shooterMotorLeft.setPower(.425);
                        shooter.shooterMotorRight.setPower(.425);
                        shooter.shooterVelocity = Shooter.farShooterVelocity;
                        shooter.update();
                        tiltPosition = shooter.farTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;
                        currentState = State.RAMPING;
                    }
                    break;

                // ------------------------------------------------------
                //  RAMPING — waiting for flywheel to reach speed
                // ------------------------------------------------------
                case RAMPING:
                    shooter.update();
                    if (getRuntime() > rampUpTimer) {
                        // Begin launch sequence
                        currentState = State.LAUNCHING;
                        ballNumber = 0;
                        launchSubStep = 0;

                        shooter.setHomeTiltPosition();
                        carousel.setHomePositionKicker();
                        stepStartTime = getRuntime();

                    }
                    break;

                // ------------------------------------------------------
                //  LAUNCHING — 3-ball launch loop + cleanup
                // ------------------------------------------------------
                case LAUNCHING:
                    shooter.update();

                    if (ballNumber < 3) {
                        // Per-ball launch sequence (5 sub-steps; last ball skips sub-step 4)
                        switch (launchSubStep) {
                            case 0: // Rotate carousel to launch position
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    switch (ballNumber) {
                                        case 0: carousel.spinCarouselLaunchOne(); break;
                                        case 1: carousel.spinCarouselLaunchTwo(); break;
                                        case 2: carousel.spinCarouselLaunchThree(); break;
                                    }
                                    stepStartTime = getRuntime();
                                    launchSubStep++;
                                }
                                break;

                            case 1: // Tiny kicker
                                double kickerDelay = (ballNumber == 0)
                                        ? defaultLaunchStepDelay + .2
                                        : defaultLaunchStepDelay;
                                if (getRuntime() - stepStartTime > kickerDelay) {
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchSubStep++;
                                }
                                break;

                            case 2: // Tilt shooter
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchSubStep++;
                                }
                                break;

                            case 3: // Full kicker (launch ball)
                                if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    if (ballNumber == 2) {
                                        // Last ball — skip reset, go to cleanup
                                        ballNumber = 3;
                                        launchSubStep = 0;
                                    } else {
                                        launchSubStep++;
                                    }
                                }
                                break;

                            case 4: // Reset tilt + kicker (first two balls only)
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setHomeTiltPosition();
                                    carousel.setHomePositionKicker();
                                    stepStartTime = getRuntime();
                                    ballNumber++;
                                    launchSubStep = 0;
                                }
                                break;
                        }
                    } else {
                        // Cleanup phase after all 3 balls launched
                        switch (launchSubStep) {
                            case 0: // Home tilt
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
                                    shooter.setHomeTiltPosition();
                                    stepStartTime = getRuntime();
                                    launchSubStep++;
                                }
                                break;
                            case 1: // Home kicker
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {
                                    carousel.setHomePositionKicker();
                                    stepStartTime = getRuntime();
                                    launchSubStep++;
                                }
                                break;
                            case 2: // Reset carousel and stop shooter
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {
                                    carousel.spinCarouselLaunchOne();
                                    shooter.shooterVelocity = 0;
                                    shooter.update();
                                    currentState = State.IDLE;
                                }
                                break;
                        }
                    }
                    break;
            }

            //------------------------END OF TRIGGER CONTROL-----------------------------------

            drive.setDrivePowers(new PoseVelocity2d(
                    new Vector2d(
                            -gamepad1.left_stick_y,
                            -gamepad1.left_stick_x
                    ),
                    -gamepad1.right_stick_x
            ));

            drive.updatePoseEstimate();

            Pose2d pose = drive.localizer.getPose();
           // telemetry.addData("x", pose.position.x);
           // telemetry.addData("y", pose.position.y);
           // telemetry.addData("heading (deg)", Math.toDegrees(pose.heading.toDouble()));
           // telemetry.update();


            TelemetryPacket packet = new TelemetryPacket();
            packet.fieldOverlay().setStroke("#3F51B5");
            Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
           telemetry.addData("Actual Left Shooter Velocity  ", shooter.shooterMotorLeft.getVelocity());
           telemetry.addData("Actual Right Shooter Velocity  ", shooter.shooterMotorRight.getVelocity());
           telemetry.addData("shooterVelocity ", shooter.shooterVelocity);
           telemetry.addData("shooterPower ", shooter.shooterPower);
           telemetry.addData(" smoothing vel left ", shooter.smoothActualLeftShooterVelocity);
            telemetry.addData(" smoothing vel right", shooter.smoothActualRightShooterVelocity);
            telemetry.addData("actualMotorPower ", shooter.actualMotorPower);
           telemetry.update();
        }

    }

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
    }
}

