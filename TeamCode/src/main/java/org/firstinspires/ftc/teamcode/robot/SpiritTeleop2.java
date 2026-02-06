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

@Config
@TeleOp(name = "SpiritTeleop2", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {
    public double launchSequenceTimer = 0;
    //public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int launchStep = 0;
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

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
        Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
        Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel
        Lift lift = new Lift(hardwareMap);
        telemetry = new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());


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
                intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
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
                        shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                        tiltPosition = shooter.nearTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;   // 2-second spin-up
                        currentState = State.RAMPING;

                    }

                    // Far shot (left trigger)
                    if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1){
                        shooter.shooterMotorLeft.setPower(.425);
                        shooter.shooterMotorRight.setPower(.425);
                        shooter.shooterVelocity = Shooter.farShooterVelocity;
                        shooter.setShooterVelocity(Shooter.shooterVelocity, telemetry);
                        tiltPosition = shooter.farTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;
                        currentState = State.RAMPING;
                    }
                    break;

                // ------------------------------------------------------
                //  RAMPING — waiting for flywheel to reach speed
                // ------------------------------------------------------
                case RAMPING:
                    shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                    if (getRuntime() > rampUpTimer) {
                        // Begin launch sequence
                        currentState = State.LAUNCHING;
                        launchStep = 0;
                        //carousel.spinCarouselLaunchOne();


                        shooter.setHomeTiltPosition();//moved here from IDLE
                        carousel.setHomePositionKicker();//moved here from IDLE
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
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay +.2) {
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 2 — tilt shooter
                        case 2:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 3 — full kicker (launch ball)
                        case 3:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                carousel.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 4 — reset tilt + kicker
                        case 4:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 5 — rotate carousel to position 2
                        case 5:
                                shooter.setShooterVelocity(shooter.shooterVelocity,telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.spinCarouselLaunchTwo();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 6 — second tiny kicker
                        case 6:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 7 — tilt again
                        case 7:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 8 — second full kicker
                        case 8:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                carousel.setFullKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 9 — reset tilt + kicker again
                        case 9:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 10 — rotate to position 3
                        case 10:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.spinCarouselLaunchThree();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 11 — tiny kicker third time
                        case 11:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 12 — tilt third time
                        case 12:
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                shooter.setTiltPosition(tiltPosition);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 13 — full send #3
                        case 13:
                            if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
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
                                carousel.spinCarouselLaunchOne();//add
                                shooter.shooterVelocity = 0;
                                shooter.setShooterVelocity(shooter.shooterVelocity, telemetry);
                                currentState = State.IDLE;
                            }
                            break;
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

