package org.firstinspires.ftc.teamcode.robot;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.tuning.TuningOpModes;

@TeleOp(name = "Spirit", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {
    public double launchSequenceTimer = 0;
    public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int launchStep = 0;
    double stepStartTime = 0;

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
        double nearTiltPosition = .35;
        double farTiltPosition = .4;
        double tiltPosition = 0;
        double homeTiltPosition = 0;
        // double carouselPosition1 = .2;//carousel position if moved forward (for testing)
        //  double carouselPosition2 = .4;//carousel position if moved backward (for testing)

        if (TuningOpModes.DRIVE_CLASS.equals(MecanumDrive.class)) {
            MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
            Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
            Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
            Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

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
                if (gamepad2.b) {
                    carousel.setHomePositionKicker();
                }
                if (gamepad2.x) {
                    carousel.setTinyKicker();
                }
                if (gamepad2.y) {
                    carousel.setFullKicker();
                }
/*
                //------------------JUST FOR TESTING POSITION OF TILT SERVO
                if (gamepad1.a) {
                    shooter.setNearTiltPosition();
                }
                if (gamepad1.b) {
                    shooter.setFarTiltPosition();
                }
                if (gamepad2.a) {
                    shooter.setHomeTiltPosition();
                }
*/

//------------------JUST FOR TESTING POSITION OF CAROUSEL


                if (gamepad1.x) {
                    carousel.spinCarouselForward();//this is set to 0 for now
                }
                if (gamepad1.y) {
                    carousel.spinCarouselBackward();//this is set to 1 for now
                }

                //--------------------------------END CODE FOR TESTING POSITION OF SERVOS
//------------------CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------


                switch (currentState) {

                    // ------------------------------------------------------
                    //  IDLE — waiting for trigger input
                    // ------------------------------------------------------
                    case IDLE:
                        shooter.setShooterPower(0);
                        shooter.setHomeTiltPosition();
                        carousel.setHomePositionKicker();
                        carousel.spinCarouselHome();

                        // Near shot (right trigger)
                        if (gamepad2.right_trigger > 0.25 && gamepad2.left_trigger < 0.1) {
                            shooterPower = nearShooterPower;
                            tiltPosition = nearTiltPosition;

                            shooter.setShooterPower(shooterPower);
                            rampUpTimer = getRuntime() + 2.0;   // 2-second spin-up
                            currentState = State.RAMPING;
                        }

                        // Far shot (left trigger)
                        if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1) {
                            shooterPower = farShooterPower;
                            tiltPosition = farTiltPosition;

                            shooter.setShooterPower(shooterPower);
                            rampUpTimer = getRuntime() + 2.0;
                            currentState = State.RAMPING;
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
                                stepStartTime = getRuntime();
                                launchStep++;
                                break;

                            // STEP 1 — tiny kicker
                            case 1:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 2 — tilt shooter
                            case 2:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 3 — full kicker (launch ball)
                            case 3:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 4 — reset tilt + kicker
                            case 4:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    shooter.setHomeTiltPosition();
                                    carousel.setHomePositionKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 5 — rotate carousel to position 2
                            case 5:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.spinCarouselLaunchTwo();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 6 — second tiny kicker
                            case 6:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 7 — tilt again
                            case 7:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 8 — second full kicker
                            case 8:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 9 — reset tilt + kicker again
                            case 9:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    shooter.setHomeTiltPosition();
                                    carousel.setHomePositionKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 10 — rotate to position 3
                            case 10:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.spinCarouselLaunchThree();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 11 — tiny kicker third time
                            case 11:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.setTinyKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 12 — tilt third time
                            case 12:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    shooter.setTiltPosition(tiltPosition);
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 13 — full send #3
                            case 13:
                                if (getRuntime() - stepStartTime > 0.20) {
                                    carousel.setFullKicker();
                                    stepStartTime = getRuntime();
                                    launchStep++;
                                }
                                break;

                            // STEP 14 — reset everything, end sequence
                            case 14:
                                if (getRuntime() - stepStartTime > 0.20) {
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
                //Drawing.drawRobot(packet.fieldOverlay(), pose);
                FtcDashboard.getInstance().sendTelemetryPacket(packet);
            }
        }
        telemetry.update();
    }

     enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
    }
}

