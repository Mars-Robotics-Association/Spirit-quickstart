/*
This class is to be used if the goal is blue. IT executes the launch sequence, drives the robot
 forward for a short period of time, strafes right to place the robot against the field wall and
 out of the way of the alliance team. Robot must be placed with the rear wells flush against the BLUDE
 target to start.
 */
package org.firstinspires.ftc.teamcode.robot;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Drawing;

@Config
@Autonomous(name = "SpiritAutoFar", group = "Robot")
public class SpiritAutoFar extends LinearOpMode {
    public double launchSequenceTimer = 0;
    public double driveTimer = 0;
    public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int launchStep = 0;
    double stepStartTime = 0;
    static public double tiltToLaunchDelay = 1.0;
    static public double defaultLaunchStepDelay = 1.0;//.4 is a good speed for competition
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

//STARTING SEQUENCE----------------------------------
            switch (currentState) {

                // ------------------------------------------------------
                //  IDLE — waiting for trigger input
                // ------------------------------------------------------
                case IDLE:

                    // Near shot
                    shooter.setHomeTiltPosition();//need tilt to be in home position to set kicker down
                    carousel.setHomePositionKicker();
                    carousel.spinCarouselLaunchOne();

                    shooterPower = nearShooterPower;
                    shooter.setShooterPower(shooterPower);
                    tiltPosition = shooter.nearTiltPosition;//to get ready to launch

                    rampUpTimer = getRuntime() + 3.0;   // 3-second spin-up
                    currentState = State.DONE;

                    //drive forward 12 inches (i.e., 1.5 seconds)
                    driveTimer = getRuntime()+ 1.5;
                    while (getRuntime() < driveTimer){
                        drive.setDrivePowers(new PoseVelocity2d(
                                new Vector2d(1, 0

                                ),
                                0
                        ));
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
                            carousel.spinCarouselLaunchOne();
                            shooter.setShooterPower(shooterPower);
                            stepStartTime = getRuntime();
                            launchStep++;
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
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
                                stepStartTime = getRuntime();
                                launchStep++;
                            }
                            break;

                        case 15:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {

                                //strafe to the field wall
                                driveTimer = getRuntime()+ 2;
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


