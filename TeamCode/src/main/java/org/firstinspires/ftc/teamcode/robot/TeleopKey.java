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

/**
 * Main teleop OpMode for Team Spirit's robot.
 *
 *
 * <p><b>Gamepad 1 (driver):</b>
 * <ul>
 *   <li>Left stick — mecanum drive (forward/back and strafe)</li>
 *   <li>Right stick X — rotation</li>
 *   <li>Right trigger — intake + carousel advance sequence (3-step cycle)</li>
 *   <li>A / B / X — tilt servo to near / far / home</li>
 *   <li>D-pad up/right/down — carousel to launch positions 1/2/3</li>
 *   <li>D-pad left — home the lift</li>
 * </ul>
 *
 * <p><b>Gamepad 2 (operator):</b>
 * <ul>
 *   <li>Left/right bumper — intake in / eject</li>
 *   <li>Right trigger — near shot launch sequence</li>
 *   <li>Left trigger — far shot launch sequence</li>
 *   <li>A — engage lift</li>
 *   <li>B / X / Y — kicker home / tiny / full (testing)</li>
 *   <li>D-pad — carousel to intake/home positions (testing)</li>
 * </ul>
 *
 * <p>The launch sequence is a 16-step timed state machine (IDLE &rarr; RAMPING &rarr;
 * LAUNCHING) that spins up the flywheels, then cycles through all three carousel
 * positions — tilting, kicking, and resetting for each ball.
 */
@Config
@TeleOp(name = "TeleopKey", group = "Teleop")
public class TeleopKey extends LinearOpMode {
    public double launchSequenceTimer = 0;
    int launchStep = 0;
    double stepStartTime = 0;
    static public double defaultLaunchStepDelay = .6;//
    static public double tiltDelay = .6;
    static public double flyWheelDelay = 1.0;

    //VARIABLES USED IN INTAKE SEQUENCE--------------------------------------
    int intakeStep = 0;                 // 0 → 1 → 2
    boolean triggerHeld = false;        // edge detection
    double intakePower = 1.0;
    //-------------------------------------------
    @Override
    public void runOpMode() {
//      telemetry.setAutoClear(false);
        telemetry.clear();
        double rampUpTimer = 0;
        double driveTimer = 0;
        boolean rampUpFlag = false;

        State currentState = State.IDLE;

        double tiltPosition = 0;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0),false);
        //Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
        ShooterTimmy shooterTimmy = new ShooterTimmy(hardwareMap);//instantiate a new shooter
        CarouselTimmy carouselTimmy = new CarouselTimmy(hardwareMap);//instantiate a new carousel
        Lift lift = new Lift(hardwareMap);
        telemetry = new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());

        shooterTimmy.setHomeTiltPosition();
        lift.homeLift();
        waitForStart();

        while (opModeIsActive()){

            //CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------
            switch (currentState) {



                // ------------------------------------------------------
                //  IDLE — waiting for trigger input
                // ------------------------------------------------------
                case IDLE:
                    // Near shot (if right trigger pulled and the left trigger is not pulled)
                    //The if condition is written this way in case someone pulls both triggers at once, nothing happens
                    if (gamepad2.right_trigger > 0.25 && gamepad2.left_trigger < 0.1) {
                        shooterTimmy.shooterMotorLeft.setPower(.3);//get the launcher spinning
                        shooterTimmy.shooterMotorRight.setPower(.3);//get the launcher spinning
                        shooterTimmy.shooterPower = ShooterTimmy.nearShooterPower;//sets the power for a near launch
                        shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//applies the power to the launcher motors
                        tiltPosition = shooterTimmy.nearTiltPosition;//sets the tilt position to for a near launch
                        rampUpTimer = getRuntime() + 2.0;   // sets a 2-second spin-up timer
                        currentState = State.RAMPING;//lets us move to the ramping section of the code

                    }

                    // Far shot (if left trigger pulled and the right trigger is not)
                    //The if condition is written this way in case someone pulls both triggers at once, nothing happens
                    if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1){
                        shooterTimmy.shooterMotorLeft.setPower(.425);//get the launcher spinning
                        shooterTimmy.shooterMotorRight.setPower(.425);//get the launcher spinning
                        shooterTimmy.shooterPower = ShooterTimmy.farShooterPower;//set power for a far launch
                        shooterTimmy.setShooterPower(ShooterTimmy.shooterPower, telemetry);//apply power to launcher motors
                        tiltPosition = shooterTimmy.farTiltPosition;//set the tilt for a far launch
                        rampUpTimer = getRuntime() + 2.0;//sets a 2 second spin-up timer
                        currentState = State.RAMPING;//let us move to the ramping section of the code
                    }
                    break;

                // ------------------------------------------------------
                //  RAMPING — waiting for flywheel to reach speed
                // ------------------------------------------------------
                case RAMPING:
                    shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//apply power to launch motors
                    if (getRuntime() > rampUpTimer) {//the code will keep testing this if condition until 2 seconds have passed
                        // Begin launch sequence
                        currentState = State.LAUNCHING;//lets us move to the LAUNCHING SECTION OF THE CODE
                        launchStep = 0;//sets the launch step to be used in the LAUNCHING SECTION OF THE CODE

                        //get tilt and carousel ready to start launch sequence:
                        shooterTimmy.setHomeTiltPosition();//set tilt position to home (vertical position)
                        carouselTimmy.setHomePositionKicker();//set carousel to home position
                        stepStartTime = getRuntime();//set a timer to the current time

                    }
                    break;

                // ------------------------------------------------------
                //  LAUNCHING — run a timed step machine
                // ------------------------------------------------------
                case LAUNCHING:

                    switch (launchStep) {//launchStep is what will move us through the steps in order. We increase it in each step below

                        // STEP 0 — rotate carousel to launch position 1
                        case 0:
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {//check to see if delay time has passed
                                carouselTimmy.spinCarouselLaunchOne();//spin carousel to first launch position
                                shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//apply power to launch motors
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increases the launch step from 0 to 1
                            }
                            break;

                        // STEP 1 — tiny kicker
                        case 1:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay +.2) {//check to see if delay time has passed
                                carouselTimmy.setTinyKicker();//lift the kicker a tiny bit so we can tilt in the next step without hitting anything
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step. Hint: stepStartTime = getRuntime();
                                launchStep++;//increase the launchStep from 1 to 2.Hint: launchStep++;
                            }
                            break;

                        // STEP 2 — tilt shooter
                        case 2:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > tiltDelay) {//check to see if delay time has passed
                                shooterTimmy.setTiltPosition(tiltPosition);//tilt the launcher
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase the launchStep from 2 to 3
                            }
                            break;

                        // STEP 3 — full kicker (launch ball)
                        case 3:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > flyWheelDelay) {//check to see if delay time has passed
                                carouselTimmy.setFullKicker();//lift the kicker the to the launch wheels
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase the launchStep from 3 to 4
                            }
                            break;

                        // STEP 4 — reset tilt + kicker
                        case 4:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {//check to see if delay time has passed
                                shooterTimmy.setHomeTiltPosition();//set the tilt to home (vertical) position so we can lower the kicker without hitting anyting
                                carouselTimmy.setHomePositionKicker();//lower the kicker
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase the launchStep from 4 to 5
                            }
                            break;

                        // STEP 5 — rotate carousel to position 2
                        case 5:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower,telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {//check to see if delay time has passed
                                carouselTimmy.spinCarouselLaunchTwo();//spin carousel to second launch position
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase the launchStep
                            }
                            break;

                        // STEP 6 — second tiny kicker
                        case 6:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {//check to see if delay time has passed
                                carouselTimmy.setTinyKicker();//lift the kicker a tiny bit so we can tilt in the next step without hitting anything
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase the launchStep
                            }
                            break;

                        // STEP 7 — tilt again
                        case 7:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > tiltDelay) {//check to see if delay time has passed
                                shooterTimmy.setTiltPosition(tiltPosition);//tilt the launcher
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            break;

                        // STEP 8 — second full kicker
                        case 8:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > flyWheelDelay) {//check to see if delay time has passed
                                carouselTimmy.setFullKicker();//lift the kicker up to the launchers
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep

                            }
                            break;

                        // STEP 9 — reset tilt + kicker again
                        case 9:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {//check to see if delay time has passed
                                shooterTimmy.setHomeTiltPosition();//set the tilt to home(vertical) so we can lower the kicker without hitting anyting
                                carouselTimmy.setHomePositionKicker();//lower the kicker
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            break;

                        // STEP 10 — rotate to position 3
                        case 10:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {//check to see if delay time has passed
                                carouselTimmy.spinCarouselLaunchThree();//spin the carousel to the third launch position
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            break;

                        // STEP 11 — tiny kicker third time
                        case 11:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay) {//check to see if delay time has passed
                                carouselTimmy.setTinyKicker();//lift the kicker a tiny bit so we can tilt in the next step without hitting anything
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            break;

                        // STEP 12 — tilt third time
                        case 12:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > tiltDelay) {//check to see if delay time has passed
                                shooterTimmy.setTiltPosition(tiltPosition);//tilt the launcher
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            break;

                        // STEP 13 — full send #3
                        case 13:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > flyWheelDelay) {//check to see if delay time has passed
                                carouselTimmy.setFullKicker();//lift the kicker to the launchers
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            break;
                        case 14://position kicker down so it does not bump carousel
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .2) {//check to see if delay time has passed
                                shooterTimmy.setHomeTiltPosition();//tilt the launcher to home (vertical) position
                                stepStartTime = getRuntime();//reset timer so we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                            // STEP 15 — reset everything, end sequence
                        case 15:
                            shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//reapply power to launch motors
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .5) {//check to see if delay time has passed
                                carouselTimmy.setHomePositionKicker();//spin carousel to home position
                                stepStartTime = getRuntime();//reset timer timerso we can check it in the next step
                                launchStep++;//increase launchStep
                            }
                        case 16://stop launchers from spinning
                            if (getRuntime() - stepStartTime > defaultLaunchStepDelay + .75) {//check to see if delay time has passed
                                carouselTimmy.spinCarouselLaunchOne();//spin carousel to first launch position
                                shooterTimmy.shooterPower = 0;//set shooter power to 0
                                shooterTimmy.setShooterPower(shooterTimmy.shooterPower, telemetry);//set motor power to shooter power
                                currentState = State.IDLE;//return to the IDLE state (currentState = State.IDLE;)
                            }
                            break;
                    }
                    break;
            }

            //------------------------END OF LAUNCH SEQUENCE-----------------------------------

/*
//LIFT ROBOT OFF OF MAT****************************************************

            if (gamepad1.back){
                lift.engageLift();
            }

            //reset lift position
            if (gamepad1.dpad_left) {
                lift.homeLift();
            }
//END CODE FOR LIFT*********************************************************
*/
            telemetry.addData("Test", 0);
            //Operate Intake: left bumper is intake and right bumper is eject
            if (gamepad2.left_bumper) {
                // intake.setPower(1);
            } else if (gamepad2.right_bumper) {
                //intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
            } else {
                // intake.setPower(0);
            }

//JUST FOR TESTING POSITON OF KICKER SERVO-------

            if (gamepad2.b) {
                carouselTimmy.setHomePositionKicker();
            }
            if (gamepad2.x) {
                carouselTimmy.setTinyKicker();
            }
            if (gamepad2.y) {
                carouselTimmy.setFullKicker();
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

//END CODE FOR TESTING POSITION OF TILT SERVO************************************

//JUST FOR TESTING POSITION OF CAROUSEL********************************************
            //if (gamepad2.dpad_left) {
            //carousel.spinCarouselMin();//this is 0

            //}
            if (gamepad2.dpad_left) {
                carouselTimmy.spinCarouselHome();//

            }
            // if (gamepad2.dpad_up) {
            // carousel.spinCarouselMax();//this is 1

            //}
            if (gamepad2.dpad_up) {
                carouselTimmy.spinCarouselIntakeOne();//

            }

            if (gamepad2.dpad_right) {
                carouselTimmy.spinCarouselIntakeTwo();//

            }

            if (gamepad2.dpad_down) {
                carouselTimmy.spinCarouselIntakeThree();//

            }

            if (gamepad1.dpad_up) {
                carouselTimmy.spinCarouselLaunchOne();

            }
            if (gamepad1.dpad_right) {
                carouselTimmy.spinCarouselLaunchTwo();
            }

            if (gamepad1.dpad_down) {
                carouselTimmy.spinCarouselLaunchThree();
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
                //intake.setPower(intakePower);
            }

            // Detect NEW trigger pull (rising edge)
            if (triggerPressed && !triggerHeld) {
                triggerHeld = true;

                // Advance carousel one position per pull
                if (intakeStep == 0) {
                    // intake.setPower(intakePower);
                    carouselTimmy.spinCarouselIntakeOne();
                    intakeStep = 1;
                } else if (intakeStep == 1) {
                    // intake.setPower(intakePower);
                    carouselTimmy.spinCarouselIntakeTwo();
                    intakeStep = 2;
                } else if (intakeStep == 2) {
                    // intake.setPower(intakePower);
                    carouselTimmy.spinCarouselIntakeThree();
                    intakeStep = 0;
                }


            }

            // Detect trigger release
            if (!triggerPressed && triggerHeld) {
                triggerHeld = false;
                //  intake.setPower(0);   // Stop intake when released
            }
            //END INTAKE SEQUENCE---------------------------------------
/**
 //MANUAL LOAD-----------------------------
 if (ADD CONTROL HERE) {
 carousel.setHomePositionKicker();
 carousel.setHomePositionKicker();
 carousel.spinCarouselLaunchOne();
 carousel.setTinyKicker();
 }
 //END MANUAL LOAD-----------------------------------------------
 */
            if (gamepad2.b) {
                carouselTimmy.setHomePositionKicker();
            }


//  KILL BUTTON*****************************************************************
            if(gamepad2.back){
                currentState = State.IDLE;
                launchStep = 0;
                shooterTimmy.shooterMotorLeft.setPower(0);
                shooterTimmy.shooterMotorRight.setPower(0);
            }
//END KILL BUTTON************************************************************


/*
//KILL CAROUSEL AND KICKER***************************************************************

            if(gamepad1.left_bumper){
            carousel.disableCarouselAndKicker();
            currentState = State.IDLE;
            NEED TO RESET THE CASE BACK TO 0 ALSO
            carousel.enableCarouselAndKicker();
            }

//END KILL CAROUSEL AND KICKER**************************************************************

*/



            //CODE TO DRIVE

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
            telemetry.addData("Actual Left Shooter Power  ", shooterTimmy.shooterMotorLeft.getPower());
            telemetry.addData("Actual Right Shooter Power  ", shooterTimmy.shooterMotorRight.getPower());
            telemetry.addData("shooterPower ", shooterTimmy.shooterPower);
            telemetry.addData("shooterPower ", shooterTimmy.shooterPower);
            telemetry.update();
        }

    }

    enum State {
        IDLE,
        RAMPING,
        LAUNCHING,
    }
}

