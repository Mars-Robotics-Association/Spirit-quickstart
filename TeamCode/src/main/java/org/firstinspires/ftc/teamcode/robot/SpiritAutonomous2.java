package org.firstinspires.ftc.teamcode.robot;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.tuning.TuningOpModes;

@Config
@Autonomous(name = "SpiritAutonomous2", group = "Robot")
public class SpiritAutonomous2 extends LinearOpMode {
    public double launchSequenceTimer = 0;
    public double driveTimer = 0;
    public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int launchStep = 0;
    double stepStartTime = 0;
    static public double tiltToLaunchDelay = 1.0;
    //public static double nearTiltPosition = .9;
    //public static double farTiltPosition = .8;


    @Override
    public void runOpMode() {
        telemetry.setAutoClear(false);
        telemetry.clear();
        double rampUpTimer = 0;
        boolean rampUpFlag = false;
        double nearShooterPower = .4;//power for shooter if bot is near target
        double farShooterPower = .6;//power for shooter if bot is far from target
        double shooterPower = 0;//power for shooter which is set to nearShooterPower or farShooterPower based on which trigger is pulled
        State currentState = State.IDLE;

        double tiltPosition = 0;
        double homeTiltPosition = 0;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
        Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
        Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

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

//----------------------JUST FOR TESTING POSITON OF KICKER SERVO-------
            //if (gamepad2.a) {

            // }
            if (gamepad2.b) {
                carousel.setHomePositionKicker();
            }
            if (gamepad2.x) {
                carousel.setTinyKicker();
            }
            if (gamepad2.y) {
                carousel.setFullKicker();
            }

            //------------------JUST FOR TESTING POSITION OF TILT SERVO
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

            //--------------------------------END CODE FOR TESTING POSITION OF SERVOS
//------------------CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------
            switch (currentState) {

                // ------------------------------------------------------
                //  IDLE — waiting for trigger input
                // ------------------------------------------------------
                case IDLE:

                    // Near shot (right trigger)
                        shooterPower = nearShooterPower;
                        shooter.setShooterPower(shooterPower);
                        tiltPosition = shooter.nearTiltPosition;
                        rampUpTimer = getRuntime() + 2.0;   // 2-second spin-up
                        currentState = State.RAMPING;

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
                            carousel.spinCarouselLaunchOne();
                            shooter.setShooterPower(shooterPower);
                            stepStartTime = getRuntime();
                            launchStep++;
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            if (getRuntime() - stepStartTime > 0.80) {
                                shooter.setShooterPower(shooterPower);
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 2 — tilt shooter
                        case 2:
                            if (getRuntime() - stepStartTime > 0.40) {
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
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 5 — rotate carousel to position 2
                        case 5:
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                carousel.spinCarouselLaunchTwo();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 6 — second tiny kicker
                        case 6:
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 7 — tilt again
                        case 7:
                            if (getRuntime() - stepStartTime > 0.40) {
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
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 10 — rotate to position 3
                        case 10:
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                carousel.spinCarouselLaunchThree();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 11 — tiny kicker third time
                        case 11:
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                carousel.setTinyKicker();
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        // STEP 12 — tilt third time
                        case 12:
                            if (getRuntime() - stepStartTime > 0.50) {
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
                            if (getRuntime() - stepStartTime > 0.40) {
                                shooter.setShooterPower(shooterPower);
                                shooter.setHomeTiltPosition();
                                carousel.setHomePositionKicker();
                                shooter.setShooterPower(0);
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        case 15:
                            if (getRuntime() - stepStartTime > 0.40) {
                                driveTimer = getRuntime()+ .5;
                                while (getRuntime() < driveTimer){
                                    drive.setDrivePowers(new PoseVelocity2d(
                                            new Vector2d(.5, 0

                                            ),
                                            0
                                    ));
                                }
                                driveTimer = getRuntime()+ 1;
                                while (getRuntime() < driveTimer) {
                                    drive.setDrivePowers(new PoseVelocity2d(
                                            new Vector2d(0, .5

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
            // telemetry.addData("State", currentState);
            telemetry.addData("Shooter Power", shooterPower);
            //telemetry.addData("Servo Pos", carousel.getPosition());
            telemetry.update();
            //------------------------END OF LAUNCH SEQUENCE-----------------------------------

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
        DONE,
    }
}

