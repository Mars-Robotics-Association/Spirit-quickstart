package org.firstinspires.ftc.teamcode.opmodes.auto;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;

/**
 * Blue-alliance Far autonomous OpMode.
 *
 * <p>Executes the full 3-ball far-shot launch sequence, then drives forward off the
 * tape (by time) to park.
 *
 * <p><b>Starting position:</b> the robot must be placed at an angle on the back wall
 * with the right front wheel against the wall and the right rear wheel at the 7th nub
 * of the floor mat.
 *
 * @see SpiritAutoRedFar
 */
@Config
@Autonomous(name = "SpiritAutoBlueFar", group = "Teleop")
public class SpiritAutoBlueFar extends LinearOpMode {
    public double launchSequenceTimer = 0;
    int launchStep = 0;
    double stepStartTime = 0;
    static public double defaultLaunchStepDelay = 1;
    static public double tiltToLaunchDelay = 1.0;

    //VARIABLES USED IN INTAKE SEQUENCE--------------------------------------
    int intakeStep = 0;                 // 0 → 1 → 2
    boolean triggerHeld = false;        // edge detection
    double intakePower = 1.0;
    //-------------------------------------------
    @Override
    public void runOpMode() {
        telemetry.clear();
        double rampUpTimer = 0;
        double driveTimer = 0;
        boolean rampUpFlag = false;

        State currentState = State.IDLE;

        double tiltPosition = 0;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);
        telemetry = new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());
        Shooter shooter = new Shooter(hardwareMap, telemetry);
        Carousel carousel = new Carousel(hardwareMap);


        shooter.setHomeTiltPosition();
        waitForStart();

        while (opModeIsActive())
        {
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
            if (gamepad2.dpad_left) {
                carousel.spinCarouselHome();

            }
            if (gamepad2.dpad_up) {
                carousel.spinCarouselIntakeOne();

            }

            if (gamepad2.dpad_right) {
                carousel.spinCarouselIntakeTwo();

            }

            if (gamepad2.dpad_down) {
                carousel.spinCarouselIntakeThree();

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
                    // Far shot (left trigger)

                        shooter.shooterVelocity = Shooter.farShooterVelocity;
                        shooter.update();
                        tiltPosition = shooter.farTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;
                        currentState = State.RAMPING;

                    break;

                // ------------------------------------------------------
                //  RAMPING — waiting for flywheel to reach speed
                // ------------------------------------------------------
                case RAMPING:
                    shooter.update();
                    if (getRuntime() > rampUpTimer) {
                        // Begin launch sequence
                        currentState = State.LAUNCHING;
                        launchStep = 0;
                        shooter.setHomeTiltPosition();
                        carousel.setHomePositionKicker();
                        stepStartTime = getRuntime();

                    }
                    break;

                // ------------------------------------------------------
                //  LAUNCHING — run a timed step machine
                // ------------------------------------------------------
                case LAUNCHING:

                    switch (launchStep) {

                        // STEP 0 — rotate carousel to launch position 1
                        case 0:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.spinCarouselLaunchOne();
                                shooter.update();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 2 — tilt shooter
                        case 2:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 3 — full kicker (launch ball)
                        case 3:
                            shooter.update();
                            if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                carousel.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 4 — reset tilt + kicker
                        case 4:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 5 — rotate carousel to position 2
                        case 5:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.spinCarouselLaunchTwo();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 6 — second tiny kicker
                        case 6:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 7 — tilt again
                        case 7:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 8 — second full kicker
                        case 8:
                            shooter.update();
                            if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                carousel.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 9 — reset tilt + kicker again
                        case 9:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 10 — rotate to position 3
                        case 10:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.spinCarouselLaunchThree();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 11 — tiny kicker third time
                        case 11:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 12 — tilt third time
                        case 12:
                            shooter.update();
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 13 — full send #3
                        case 13:
                            if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                shooter.update();
                                carousel.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;
                        case 14://position kicker down so it does not bump carousel
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
                                shooter.setHomeTiltPosition();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            // STEP 15 — reset everything, end sequence
                        case 15:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                        case 16:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {
                                carousel.spinCarouselLaunchOne();
                                shooter.shooterVelocity = 0;
                                shooter.update();

                                //strafe to the field wall
                                driveTimer = getRuntime() + .4;
                                while (getRuntime() < driveTimer) {
                                    drive.setDrivePowers(new PoseVelocity2d(
                                            new Vector2d(-1, 0

                                            ),
                                            0
                                    ));
                                }


                                currentState = State.DONE;
                            }
                            break;
                    }
                        case DONE:
                            drive.setDrivePowers(new PoseVelocity2d(
                                    new Vector2d(0, 0

                                    ),
                                    0
                            ));
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

            TelemetryPacket packet = new TelemetryPacket();
            packet.fieldOverlay().setStroke("#3F51B5");
            Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
            telemetry.addData("shooterVelocity ", shooter.shooterVelocity);
            telemetry.addData("shooterPower ", shooter.shooterPower);
            telemetry.update();
        }

    }

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
        DONE,
    }
}

