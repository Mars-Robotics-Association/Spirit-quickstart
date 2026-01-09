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
import org.firstinspires.ftc.teamcode.tuning.TuningOpModes;

@Config
@TeleOp(name = "Spirit", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {
    public double launchSequenceTimer = 0;
    public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int launchStep = 0;
    double stepStartTime = 0;
    static public double defaultLaunchStepDelay = 1;//.4 will work for competition
    static public double tiltToLaunchDelay = 1.0;
    //public static double nearTiltPosition = .9; //this is already in the Shooter class
    //public static double farTiltPosition = .8; //this is already in the shooter Class
    static public double nearShooterPower = .4;//power for shooter if bot is near target
    static public double farShooterPower = .8;//power for shooter if bot is far from target


    //VARIABLES USED IN INTAKE SEQUENCE--------------------------------------
    int intakeStep = 0;                 // 0 → 1 → 2
    boolean triggerHeld = false;        // edge detection
    double intakePower = 1.0;
//-------------------------------------------
    @Override
    public void runOpMode() {
        telemetry.setAutoClear(false);
        telemetry.clear();
        double rampUpTimer = 0;
        boolean rampUpFlag = false;

        double shooterPower = 0;//power for shooter which is set to nearShooterPower or farShooterPower based on which trigger is pulled
        State currentState = State.IDLE;

        double tiltPosition = 0;
        double homeTiltPosition = 0;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
        Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
        Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

            /*carousel.spinCarouselHome();//
            telemetry.addData("carouselHome ", carousel.carouselPositionHome);
            telemetry.update();

            sleep(2000);
            carousel.spinCarouselIntakeOne();
            sleep(5000);
            telemetry.addData("carouselIntakeOne ", carousel.carouselPositionIntakeOne);
            telemetry.update();


            carousel.spinCarouselIntakeTwo();
            sleep(2000);
            telemetry.addData("carouselIntakeTwo ", carousel.carouselPositionIntakeTwo);
            telemetry.update();

            carousel.spinCarouselIntakeThree();
            sleep(2000);
            telemetry.addData("carouselIntakeThree ", carousel.carouselPositionIntakeThree);
            telemetry.update();

            carousel.spinCarouselLaunchOne();
            sleep(2000);
            telemetry.addData("carouselLauncheOne ", carousel.carouselPositionLaunchOne);
            telemetry.update();

            carousel.spinCarouselLaunchTwo();
            sleep(2000);
            telemetry.addData("carouselLauncheTwo ", carousel.carouselPositionLaunchTwo);
            telemetry.update();

            carousel.spinCarouselLaunchThree();
            sleep(2000);
            telemetry.addData("carouselLauncheThree ", carousel.carouselPositionLaunchThree);
            telemetry.update();
*/
            shooter.setHomeTiltPosition();
            waitForStart();

            while (opModeIsActive())

            {
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
                    telemetry.addData("Near Tile ", shooter.nearTiltPosition);
                    telemetry.update();
                }

                if (gamepad1.b) {
                    shooter.setFarTiltPosition(shooter.farTiltPosition);
                    telemetry.addData("far Tilt ", shooter.farTiltPosition);
                    telemetry.update();
                }
                if (gamepad1.x) {
                    shooter.setHomeTiltPosition();
                }

//--------------------------------END CODE FOR TESTING POSITION OF TILT SERVO

//------------------JUST FOR TESTING POSITION OF CAROUSEL
                //if (gamepad2.dpad_left) {
                    //carousel.spinCarouselMin();//this is 0
                    //telemetry.addData("carouselHome ", carousel.carouselPositionMin);
                   // telemetry.update();
                //}
                if (gamepad2.dpad_left) {
                    carousel.spinCarouselHome();//
                    telemetry.addData("carouselHome ", carousel.carouselPositionHome);
                    telemetry.update();
                }
               // if (gamepad2.dpad_up) {
                   // carousel.spinCarouselMax();//this is 1
                   // telemetry.addData("carouselIntakeOne ", carousel.carouselPositionMax);
                   // telemetry.update();
                //}
                if (gamepad2.dpad_up) {
                    carousel.spinCarouselIntakeOne();//
                    telemetry.addData("carouselIntakeOne ", carousel.carouselPositionIntakeOne);
                    telemetry.update();
                }

                if (gamepad2.dpad_right) {
                    carousel.spinCarouselIntakeTwo();//
                    telemetry.addData("carouselIntakeTwo ", carousel.carouselPositionIntakeTwo);
                    telemetry.update();
                }

                if (gamepad2.dpad_down) {
                    carousel.spinCarouselIntakeThree();//
                    telemetry.addData("carouselIntakeThree ", carousel.carouselPositionIntakeThree);
                    telemetry.update();
                }

                if (gamepad1.dpad_up) {
                    carousel.spinCarouselLaunchOne();//
                    telemetry.addData("carouselLaunchOne ", carousel.carouselPositionLaunchOne);
                    telemetry.update();
                }
                if (gamepad1.dpad_right) {
                    carousel.spinCarouselLaunchTwo();//
                    telemetry.addData("carouselLaunchTwo ", carousel.carouselPositionLaunchTwo);
                    telemetry.update();
                }

                if (gamepad1.dpad_down) {
                    carousel.spinCarouselLaunchThree();//
                    telemetry.addData("carouselLaunchThree ", carousel.carouselPositionLaunchThree);
                    telemetry.update();
                }
                //---------------------------END TESTING OF CAROUSEL

                //_____________________________INTAKE SEQUENCE____________________________



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

                    telemetry.addData("Intake Step", intakeStep);
                    telemetry.update();
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
                            shooterPower = nearShooterPower;
                            shooter.setShooterPower(shooterPower);
                            tiltPosition = shooter.nearTiltPosition;
                            rampUpTimer = getRuntime() + 2.0;   // 2-second spin-up
                            currentState = State.RAMPING;
                            telemetry.addData("Right Trigger ", gamepad2.right_trigger);
                            telemetry.update();

                        }

                        // Far shot (left trigger)
                        if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1){
                            shooterPower = farShooterPower;
                            shooter.setShooterPower(shooterPower);
                            tiltPosition = shooter.farTiltPosition;
                            rampUpTimer = getRuntime() + 2.0;
                            currentState = State.RAMPING;
                            telemetry.addData("Left Trigger ", gamepad2.left_trigger);
                            telemetry.update();
                        }

                        break;

                    // ------------------------------------------------------
                    //  RAMPING — waiting for flywheel to reach speed
                    // ------------------------------------------------------
                    case RAMPING:
                        if (getRuntime() > rampUpTimer) {
                            // Begin launch sequence
                            currentState = State.LAUNCHING;
                            launchStep = 0;
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

                            // STEP 0 — rotate carousel to position 1
                            case 0:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    carousel.spinCarouselLaunchOne();
                                    shooter.setShooterPower(shooterPower);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 1 — tiny kicker
                            case 1:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay +.2) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 2 — tilt shooter
                            case 2:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 3 — full kicker (launch ball)
                            case 3:
                                if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 4 — reset tilt + kicker
                            case 4:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    shooter.setHomeTiltPosition();
                                    carousel.setHomePositionKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 5 — rotate carousel to position 2
                            case 5:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.spinCarouselLaunchTwo();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 6 — second tiny kicker
                            case 6:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 7 — tilt again
                            case 7:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 8 — second full kicker
                            case 8:
                                if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 9 — reset tilt + kicker again
                            case 9:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    shooter.setHomeTiltPosition();
                                    carousel.setHomePositionKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 10 — rotate to position 3
                            case 10:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.spinCarouselLaunchThree();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 11 — tiny kicker third time
                            case 11:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 12 — tilt third time
                            case 12:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 13 — full send #3
                            case 13:
                                if (getRuntime() - stepStartTime > tiltToLaunchDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 14 — reset everything, end sequence
                            case 14:
                                if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {
                                    shooter.setShooterPower(shooterPower);
                                    shooter.setHomeTiltPosition();
                                    carousel.setHomePositionKicker();
                                    shooter.setShooterPower(0);
                                    currentState = State.IDLE;
                                }
                                break;
                        }

                        break;
                }
               // telemetry.addData("State", currentState);
                telemetry.addData("Shooter Power", shooterPower);
                //telemetry.addData("Servo Pos", carousel.getPosition());
                telemetry.update();
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
                telemetry.addData("x", pose.position.x);
                telemetry.addData("y", pose.position.y);
                telemetry.addData("heading (deg)", Math.toDegrees(pose.heading.toDouble()));
                telemetry.update();


                TelemetryPacket packet = new TelemetryPacket();
                packet.fieldOverlay().setStroke("#3F51B5");
                Drawing.drawRobot(packet.fieldOverlay(), pose);
                FtcDashboard.getInstance().sendTelemetryPacket(packet);
            }
       // }//TRY REMOVING THIS
        //telemetry.update();
    }

     enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
    }
}

