package org.firstinspires.ftc.teamcode.robot;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.PwmControl.PwmRange;

import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;
import org.firstinspires.ftc.teamcode.tuning.TuningOpModes;

@TeleOp(name = "Spirit", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {
    @Override
    public void runOpMode() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        double rampUpTimer = 0;
        boolean rampUpFlag = false;
        int carouselCounter = 0;//counts the number of times the carousel has advanced 120 degrees. After 3 advances, we need to rest the carousel's position
       double nearShooterPower = .4;//power for shooter if bot is near target
       double farShooterPower = .6;//power for shooter if bot is far from target
        double shooterPower = 0;//power for shooter which is set to nearShooterPower or farShooterPower based on which trigger is pulled
        double nearTiltPosition = 0;//position of tilt for shooting near the target
        double farTiltPosition = 0;//position of the tilt for shooting far from the target
        double tiltPosition = 0;//position of the tile. Will be set to NearTiltPosition or farTiltPosition in the code's IDLE case

        State currentState = State.IDLE;


        if (TuningOpModes.DRIVE_CLASS.equals(MecanumDrive.class)) {
            MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
            Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
            Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
            Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

            waitForStart();

            while (opModeIsActive())
            //Operate Intake: left bumper is intake and right bumper is eject
            {
                if (gamepad2.left_bumper) {
                    intake.setPower(1);
                } else if (gamepad2.right_bumper) {
                    intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
                } else {
                    intake.setPower(0);
                }


//----------------------JUST FOR TESTING POSITON OF SHOOTER SERVO-------
                if (gamepad2.a) {
                    carousel.setDownPositionKickerServo();

                }
                if (gamepad2.x) {
                    carousel.tinyLiftKickerServo();
                }
                if (gamepad2.y) {
                    carousel.fullLiftKickerServo();
                }
//------------------JUST FOR TESTING POSITION OF TILT SERVO
                if (gamepad1.a) {
                    shooter.setNearTiltServo();
                }
                if (gamepad1.b) {
                    shooter.setFarTiltServo();
                }
//------------------JUST FOR TESTING POSITION OF CAROUSEL

                //spin carousel forward
                if (gamepad1.x) {
                    carousel.spinCarousel();
                }
                //spin carousel backward
                if (gamepad1.y) {
                    carousel.spinCarousel();
                }
                //--------------------------------END CODE FOR TESTING POSITION OF SERVOS

            }
//------------------CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------
            //--------------LEFT TRIGGER PULLED (CLOSE TO TARGET) ------------------------------

            switch (currentState) {

                case IDLE:
                    //handleIdle();
                    shooter.setShooterPower(0.00);//stop shooter
                    carousel.setDownPositionKickerServo();//make sure kicker is in down position
                    tiltPosition = 0;//make sure shooter is not tilted (prevents kicker from lifting)
                    shooter.setTiltServo(tiltPosition);//make sure shooter is not tilted (prevents kicker from lifting)
                    //Right trigger pulled:
                    if ((gamepad2.right_trigger > .25) && (gamepad2.left_trigger < .01)) {
                        shooterPower = nearShooterPower;
                        tiltPosition = nearTiltPosition;
                        shooter.setShooterPower(shooterPower);//set power to shoot near target
                        currentState = State.RAMPING;
                        rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                    }
                    if ((gamepad2.left_trigger > .25) && (gamepad2.right_trigger < .01)) {
                        shooterPower = farShooterPower;
                        tiltPosition = farTiltPosition;
                        shooter.setShooterPower(shooterPower);//set power to shoot far from target
                        currentState = State.RAMPING;
                        rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                    }
                    break;

                case RAMPING:
                    if (getRuntime() > rampUpTimer) {
                        shooter.setShooterPower(shooterPower);//set power to shooter
                        currentState = State.LAUNCHING;
                    }
                    break;

                case LAUNCHING:
                    if (getRuntime() > rampUpTimer) {

                        for (int i = 1; 1 <= 3; i++) {
                            carousel.spinCarousel();//spin carousel
                            carousel.tinyLiftKickerServo();//lift kicker a tiny bit & cup the artifact so the tilt can happen
                            shooter.setTiltServo(tiltPosition);//tilt for near or far shooting based on value set in IDLE case
                            carousel.fullLiftKickerServo();//lift kicker the whole way to launch
                            sleep(2000);//pause to launch
                            //Reset tilt and kicker
                            tiltPosition = 0;
                            shooter.setTiltServo(tiltPosition);//reset tilt to home position
                            carousel.setDownPositionKickerServo();//lower the kicker the whole way

                            currentState = State.IDLE;
                        }
                    }
                        //set carousel back to home position
                        carousel.spinCarouselHome();
                        break;


                    }
                    telemetry.addData("State", currentState);
                    telemetry.addData("Shooter Power", shooterPower);
                    //telemetry.addData("Servo Pos", carousel.getPosition());
                    telemetry.update();
                    //------------------------END OF SHOOTER CONTROL-----------------------------------


                    //-------END CODE FOR TRIGGERS--------------------------------------

                    //if (gamepad2.y) {
                        //
                    // }

                   // if (gamepad2.x) {
                        //
                    // }

                    if (gamepad1.x) {
                        if (carouselCounter <= 3) {
                            carousel.spinCarousel();
                            carouselCounter++;
                        } else {
                            carousel.spinCarouselHome();
                            carouselCounter = 0;
                        }
                    }

                    if (gamepad2.b) {
                        //
                    }
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


    }

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
    }



