package org.firstinspires.ftc.teamcode.robot;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;

import org.firstinspires.ftc.teamcode.Drawing;

/**
 * Color-sensor auto-selecting Near autonomous OpMode.
 *
 * <p>Uses a REV Color/Distance Sensor V2 ({@code "colorSensor"}) to detect the alliance
 * color during init. The detected color is displayed on telemetry so drivers can verify
 * before pressing Start.
 *
 * <p>On start, the robot drives backward 6 inches (by time), executes the full 3-ball
 * near-shot launch sequence, then strafes toward the field wall:
 * <ul>
 *   <li><b>Blue:</b> strafes right (+0.5 y power, 0.75 s)</li>
 *   <li><b>Red:</b> strafes left (-0.5 y power, 0.7 s)</li>
 * </ul>
 *
 * <p>Robot must be placed with the rear wells flush against the target to start.
 *
 * @see SpiritAutoBlueNear
 * @see SpiritAutoRedNear
 */
@Config
@Autonomous(name = "SpiritAutoNear", group = "Teleop")
public class SpiritAutoNear extends LinearOpMode {
    public double launchSequenceTimer = 0;
    public double driveTimer = 0;

    int launchStep = 0;
    double stepStartTime = 0;
    static public double defaultLaunchStepDelay = .6;//.4 will work for competition
    static public double tiltToLaunchDelay = .6;
    static public double flyWheelDelay = 1.0;
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

        State currentState = State.IDLE;

       double tiltPosition = 0;//this is only used for testing, not in production


        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        IntakeTimmy intake = new IntakeTimmy(hardwareMap);//instantiate a new intake motor
        ShooterTimmy shooterTimmy = new ShooterTimmy(hardwareMap);//instantiate a new shooter
        CarouselTimmy timmyCarouselTimmy = new CarouselTimmy(hardwareMap);//instantiate a new carousel
        telemetry = new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());

        // Color sensor for auto-detecting alliance color
        ColorSensor colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
        boolean isBlue = true; // default to blue

        shooterTimmy.setHomeTiltPosition();

        // During init, continuously read the color sensor and display detected color
        while (!isStarted() && !isStopRequested()) {
            int red = colorSensor.red();
            int blue = colorSensor.blue();
            isBlue = blue > red;

            telemetry.addData("Detected Alliance", isBlue ? "BLUE" : "RED");
            telemetry.addData("Red Value", red);
            telemetry.addData("Blue Value", blue);
            telemetry.addData("Status", "Waiting for Start...");
            telemetry.update();
        }

        while (opModeIsActive())
/*
                //KILL BUTTON*****************************************************************
                if(gamepad1.y){
                    drive.setDrivePowers(new PoseVelocity2d(
                            new Vector2d(0, 0

                            ),
                            0
                    ));
                }
//END KILL BUTTON************************************************************

 */
        {
            telemetry.addData("Test", 0);
            telemetry.addData("Alliance", isBlue ? "BLUE" : "RED");
            //Operate Intake: left bumper is intake and right bumper is eject
            if (gamepad2.left_bumper) {
                intake.setPower(1);
            } else if (gamepad2.right_bumper) {
                intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
            } else {
                intake.setPower(0);
            }

            //JUST FOR TESTING POSITON OF KICKER SERVO-------

            if (gamepad2.b) {
                timmyCarouselTimmy.setHomePositionKicker();
            }
            if (gamepad2.x) {
                timmyCarouselTimmy.setTinyKicker();
            }
            if (gamepad2.y) {
                timmyCarouselTimmy.setFullKicker();
            }
            //JUST FOR TESTING POSITION OF TILT SERVO
            if (gamepad1.a) {
                shooterTimmy.setNearTiltPosition(shooterTimmy.nearTiltPosition);

            }

            if (gamepad1.b) {
                shooterTimmy.setFarTiltPosition(shooterTimmy.farTiltPosition);

            }
            if (gamepad1.x) {
                shooterTimmy.setHomeTiltPosition();
            }

//--------------------------------END CODE FOR TESTING POSITION OF TILT SERVO

//------------------JUST FOR TESTING POSITION OF CAROUSEL
            //if (gamepad2.dpad_left) {
            //carousel.spinCarouselMin();//this is 0

            //}
            if (gamepad2.dpad_left) {
                timmyCarouselTimmy.spinCarouselHome();//

            }
            // if (gamepad2.dpad_up) {
            // carousel.spinCarouselMax();//this is 1

            //}
            if (gamepad2.dpad_up) {
                timmyCarouselTimmy.spinCarouselIntakeOne();//

            }

            if (gamepad2.dpad_right) {
                timmyCarouselTimmy.spinCarouselIntakeTwo();//

            }

            if (gamepad2.dpad_down) {
                timmyCarouselTimmy.spinCarouselIntakeThree();//

            }

            if (gamepad1.dpad_up) {
                timmyCarouselTimmy.spinCarouselLaunchOne();

            }
            if (gamepad1.dpad_right) {
                timmyCarouselTimmy.spinCarouselLaunchTwo();
            }

            if (gamepad1.dpad_down) {
                timmyCarouselTimmy.spinCarouselLaunchThree();
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
                    timmyCarouselTimmy.spinCarouselIntakeOne();
                    intakeStep = 1;
                } else if (intakeStep == 1) {
                    intake.setPower(intakePower);
                    timmyCarouselTimmy.spinCarouselIntakeTwo();
                    intakeStep = 2;
                } else if (intakeStep == 2) {
                    intake.setPower(intakePower);
                    timmyCarouselTimmy.spinCarouselIntakeThree();
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
                timmyCarouselTimmy.setHomePositionKicker();
                timmyCarouselTimmy.setHomePositionKicker();
                timmyCarouselTimmy.spinCarouselLaunchOne();
                timmyCarouselTimmy.setTinyKicker();
            }
            //END MANUAL LOAD-----------------------------------------------

            if (gamepad2.b) {
                timmyCarouselTimmy.setHomePositionKicker();
            }

            //CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------
            switch (currentState) {

                // ------------------------------------------------------
                //  IDLE — waiting for trigger input
                // ------------------------------------------------------
                case IDLE:
                    // Near shot, get shooter motors, tile and ramp up all set up and drive backwards 6 inches

                    //shooter.shooterVelocity = shooter.nearShooterVelocity;
                    shooterTimmy.shooterMotorLeft.setPower(.3);
                    shooterTimmy.shooterMotorRight.setPower(.3);
                    shooterTimmy.shooterPower = ShooterTimmy.nearShooterPower;
                    shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                    tiltPosition = shooterTimmy.nearTiltPosition;
                    rampUpTimer = getRuntime() + 2.0;   // 2-second spin-up

                    //drive backward 6 inches (i.e., .2 seconds)
                    driveTimer = getRuntime()+ .25;
                    while (getRuntime() < driveTimer) {
                        drive.setDrivePowers(new PoseVelocity2d(
                                new Vector2d(1, 0

                                ),
                                0
                        ));


                        currentState = State.RAMPING;

                    }
                    break;

                // ------------------------------------------------------
                //  RAMPING — waiting for flywheel to reach speed
                // ------------------------------------------------------
                case RAMPING:
                    shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                    if (getRuntime() > rampUpTimer) {
                        // Begin launch sequence
                        currentState = State.LAUNCHING;
                        launchStep = 0;
                        //carousel.spinCarouselLaunchOne();


                        shooterTimmy.setHomeTiltPosition();//moved here from IDLE
                        timmyCarouselTimmy.setHomePositionKicker();//moved here from IDLE
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
                                timmyCarouselTimmy.spinCarouselLaunchOne();
                                shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay +.2) {
                                timmyCarouselTimmy.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 2 — tilt shooter
                        case 2:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooterTimmy.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 3 — full kicker (launch ball)
                        case 3:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > flyWheelDelay) {
                                timmyCarouselTimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 4 — reset tilt + kicker
                        case 4:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooterTimmy.setHomeTiltPosition();
                                timmyCarouselTimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 5 — rotate carousel to position 2
                        case 5:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower,telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                timmyCarouselTimmy.spinCarouselLaunchTwo();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 6 — second tiny kicker
                        case 6:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
                                timmyCarouselTimmy.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 7 — tilt again
                        case 7:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooterTimmy.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 8 — second full kicker
                        case 8:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > flyWheelDelay) {
                                timmyCarouselTimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 9 — reset tilt + kicker again
                        case 9:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooterTimmy.setHomeTiltPosition();
                                timmyCarouselTimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 10 — rotate to position 3
                        case 10:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                timmyCarouselTimmy.spinCarouselLaunchThree();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 11 — tiny kicker third time
                        case 11:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                timmyCarouselTimmy.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 12 — tilt third time
                        case 12:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooterTimmy.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 13 — full send #3
                        case 13:
                            if (getRuntime() - stepStartTime > flyWheelDelay) {
                                shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
                                timmyCarouselTimmy.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;
                        case 14://position kicker down so it does not bump carousel
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
                                shooterTimmy.setHomeTiltPosition();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            // STEP 15 — reset everything, end sequence
                        case 15:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {
                                timmyCarouselTimmy.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                        case 16:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {
                                timmyCarouselTimmy.spinCarouselLaunchOne();//add
                                shooterTimmy.shooterPower = 0;
                                shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);

                                //strafe to the field wall — direction based on detected color
                                double strafePower = isBlue ? 0.5 : -0.5;
                                double strafeDuration = isBlue ? 0.75 : 0.7;
                                driveTimer = getRuntime() + strafeDuration;
                                while (getRuntime() < driveTimer) {
                                    drive.setDrivePowers(new PoseVelocity2d(
                                            new Vector2d(0, strafePower

                                            ),
                                            0
                                    ));
                                }

                                currentState = State.DONE;
                            }
                            break;
                    }
                    //stop robot wheels
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
            // telemetry.addData("x", pose.position.x);
            // telemetry.addData("y", pose.position.y);
            // telemetry.addData("heading (deg)", Math.toDegrees(pose.heading.toDouble()));
            // telemetry.update();


            TelemetryPacket packet = new TelemetryPacket();
            packet.fieldOverlay().setStroke("#3F51B5");
            Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
            telemetry.addData("Actual Left Shooter Velocity  ", shooterTimmy.shooterMotorLeft.getVelocity());
            telemetry.addData("Actual Right Shooter Velocity  ", shooterTimmy.shooterMotorRight.getVelocity());
            telemetry.addData("shooterVelocity ", shooterTimmy.shooterPower);
            telemetry.addData("shooterPower ", shooterTimmy.shooterPower);
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
