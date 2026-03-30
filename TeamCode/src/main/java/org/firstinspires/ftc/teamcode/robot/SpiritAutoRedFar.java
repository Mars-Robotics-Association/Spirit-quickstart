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

import org.firstinspires.ftc.teamcode.Drawing;

/**
 * Red-alliance Far autonomous OpMode.
 *
 * <p>Executes the full 3-ball far-shot launch sequence, then drives forward off the
 * tape (by time) to park.
 *
 * <p><b>Starting position:</b> the robot must be placed at an angle against the back wall
 * with the right front wheel against the wall and the right rear wheel at the 7th nub
 * of the floor mat.
 *
 * @see SpiritAutoBlueFar
 */
@Config
@Autonomous(name = "SpiritAutoRedFar", group = "Teleop")
public class SpiritAutoRedFar extends LinearOpMode {
    public double launchSequenceTimer = 0;
    //public int counter = 0;//variable to use to cause a waiting period in the launch sequence
    int launchStep = 0;
    double stepStartTime = 0;
    static public double defaultLaunchStepDelay = .6;//.4 will work for competition
    static public double tiltToLaunchDelay = .6;
    static public double flyWheelDelay = 1;

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
        ShooterTimmy shooterTimmy = new ShooterTimmy(hardwareMap);//instantiate a new shooter
        CarouselTimmy timmyCarouselTimmy = new CarouselTimmy(hardwareMap);//instantiate a new carousel
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());


        shooterTimmy.setHomeTiltPosition();
        waitForStart();

        while (opModeIsActive())
            //CODE FOR WHEN THE AUTONOMOUS STARTS----------------------------------
            switch (currentState) {

                // ------------------------------------------------------
                //  IDLE
                // ------------------------------------------------------
                case IDLE:
                    //Far shot red target
                    // set shooter power and apply to motors to get them spinning. set tilt, set timer, set ramp up timer

                    shooterTimmy.shooterMotorLeft.setPower(.425);
                    shooterTimmy.shooterMotorRight.setPower(.425);
                    shooterTimmy.shooterPower = ShooterTimmy.farShooterPower;
                    shooterTimmy.setShooterPower(ShooterTimmy.shooterPower, telemetry);
                    tiltPosition = shooterTimmy.farTiltPosition;
                    rampUpTimer = getRuntime() + 2.0;
                    currentState = State.RAMPING;

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
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {
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
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);
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

                                //rotate to the left a tiny bit
                                driveTimer = getRuntime() + .3;
                                while (getRuntime() < driveTimer) {
                                    drive.setDrivePowers(new PoseVelocity2d(
                                            new Vector2d(0, 0

                                            ),
                                            .4
                                    ));
                                }
                                drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0)); // stop rotation

                                //strafe to the right and out of alliance partner's access 3rd row of artifacts
                                //note: x of -1, y of 0 and angVel of 0  drives it forward
                                driveTimer = getRuntime() + .75;
                                while (getRuntime() < driveTimer) {
                                    drive.setDrivePowers(new PoseVelocity2d(
                                            new Vector2d(0, 1

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

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
        DONE,
    }
}

