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

                    case IDLE:
                        //handleIdle();
                        telemetry.addData("State", currentState);
                        telemetry.update();
                        shooter.setHomeTiltPosition();
                        carousel.setHomePositionKicker();
                        carousel.spinCarouselHome();
                       // telemetry.addData("Carousel Home Pos", carousel.carouselPositionHome);
                      //  telemetry.update();
                        shooter.setShooterPower(0.00);//stop shooter

                        //if left trigger pulled shoot near target
                        if ((gamepad2.right_trigger > .25) && (gamepad2.left_trigger < .01)) {
                            shooterPower = nearShooterPower;
                            shooter.setShooterPower(shooterPower);//set power to shoot near target
                            tiltPosition = nearTiltPosition;
                            currentState = State.RAMPING;
                            telemetry.addData("State LT", currentState);
                            telemetry.update();
                            rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                        }
                        //if right trigger pulled - shoot far away
                        if ((gamepad2.left_trigger > .25) && (gamepad2.right_trigger < .01)) {
                            shooterPower = farShooterPower;
                            shooter.setShooterPower(shooterPower);//set power to shoot far from target
                            tiltPosition = farTiltPosition;
                            currentState = State.RAMPING;
                            telemetry.addData("State RT", currentState);
                            telemetry.update();
                            rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                        }
                        break;

                    case RAMPING:
                        if (getRuntime() > rampUpTimer) {
                            currentState = State.LAUNCHING;
                            telemetry.addData("State", currentState);
                            telemetry.update();

                        }
                        break;

                        /*Launching sequence is
                                // rotate carousel
                                //lift kicker tiny amount
                                //tilt shooter for near or far shot
                                //lift kicker the entire way to launch
                                //tile shooter to home position
                                //lower kicker the whole way

                        */
                    case LAUNCHING:
                        if (getRuntime() > rampUpTimer) {
                            for (int i = 0; i <= 3; i++) {


                                telemetry.addData("State", currentState);
                                telemetry.update();

                                carousel.spinCarouselLaunchOne();
                                telemetry.addData("CP LaunchOne", carousel.carouselPositionLaunchOne);
                                telemetry.update();
                                 delayLaunchSequence();
                                carousel.setTinyKicker();
                                 delayLaunchSequence();
                                shooter.setTiltPosition(tiltPosition);
                                 delayLaunchSequence();
                                carousel.setFullKicker();
                                    delayLaunchSequence();
                                shooter.setHomeTiltPosition();
                                 delayLaunchSequence();
                                carousel.setHomePositionKicker();
                                 delayLaunchSequence();

                                carousel.spinCarouselLaunchTwo();
                                telemetry.addData("CP LaunchTwo", carousel.carouselPositionLaunchTwo);
                                telemetry.update();
                                    delayLaunchSequence();
                                carousel.setTinyKicker();
                                     delayLaunchSequence();
                                shooter.setTiltPosition(tiltPosition);
                                 delayLaunchSequence();
                                carousel.setFullKicker();
                                    delayLaunchSequence();
                                shooter.setHomeTiltPosition();
                                    delayLaunchSequence();
                                carousel.setHomePositionKicker();
                                    delayLaunchSequence();

                                carousel.spinCarouselLaunchThree();
                                telemetry.addData("CP LaunchThree", carousel.carouselPositionLaunchThree);
                                telemetry.update();
                                    delayLaunchSequence();
                                carousel.setTinyKicker();
                                 delayLaunchSequence();
                                shooter.setTiltPosition(tiltPosition);
                                 delayLaunchSequence();
                                shooter.setHomeTiltPosition();
                                    delayLaunchSequence();
                                carousel.setHomePositionKicker();
                                 delayLaunchSequence();
                                shooter.setShooterPower(0.00);
                            }

                            currentState = State.IDLE;
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
        void delayLaunchSequence() {
            launchSequenceTimer = getRuntime() + 2;
            if (getRuntime() < launchSequenceTimer) {
                counter++;
            } else {
                launchSequenceTimer = 0;
            }
        }

     enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
    }
}

