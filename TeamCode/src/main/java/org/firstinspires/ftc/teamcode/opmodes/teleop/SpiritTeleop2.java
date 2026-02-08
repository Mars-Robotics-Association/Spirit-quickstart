package org.firstinspires.ftc.teamcode.opmodes.teleop;
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
import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.LaunchSequence;
import org.firstinspires.ftc.teamcode.robot.Lift;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;

/**
 * Main teleop OpMode for Team Spirit's robot.
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
 * <p>The launch sequence is a timed state machine (IDLE &rarr; RAMPING &rarr;
 * LAUNCHING) that spins up the flywheels, then loops through all three carousel
 * positions — rotating, tilting, kicking, and resetting for each ball.
 */
@Config
@TeleOp(name = "SpiritTeleop2", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {

    //VARIABLES USED IN INTAKE SEQUENCE--------------------------------------
    int intakeStep = 0;                 // 0 → 1 → 2
    boolean triggerHeld = false;        // edge detection

    //-------------------------------------------
    @Override
    public void runOpMode() {
        telemetry.clear();

        telemetry = new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);
        Shooter shooter = new Shooter(hardwareMap, telemetry);
        Carousel carousel = new Carousel(hardwareMap);
        Lift lift = new Lift(hardwareMap);
        LaunchSequence launchSequence = new LaunchSequence(shooter, carousel, this::getRuntime);

        shooter.setHomeTiltPosition();
        lift.homeLift();
        waitForStart();

        while (opModeIsActive()){

            if (gamepad2.a){
                lift.engageLift();
            }

            //reset lift position
            if (gamepad1.dpad_left) {
                lift.homeLift();
            }

            telemetry.addData("Test", 0);
            //Operate Intake: left bumper is intake and right bumper is eject
            if (gamepad2.left_bumper) {
                intake.run();
            } else if (gamepad2.right_bumper) {
                intake.eject();
            } else {
                intake.stop();
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
            if (gamepad2.dpad_left) {
                carousel.spinCarouselHome();//

            }
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
                intake.run();
            }

            // Detect NEW trigger pull (rising edge)
            if (triggerPressed && !triggerHeld) {
                triggerHeld = true;

                // Advance carousel one position per pull
                if (intakeStep == 0) {
                    intake.run();
                    carousel.spinCarouselIntakeOne();
                    intakeStep = 1;
                } else if (intakeStep == 1) {
                    intake.run();
                    carousel.spinCarouselIntakeTwo();
                    intakeStep = 2;
                } else if (intakeStep == 2) {
                    intake.run();
                    carousel.spinCarouselIntakeThree();
                    intakeStep = 0;
                }


            }

            // Detect trigger release
            if (!triggerPressed && triggerHeld) {
                triggerHeld = false;
                intake.stop();
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

            //CODE FOR LAUNCH SEQUENCE VIA TRIGGERS----------------------------------
            if (!launchSequence.isRunning()) {
                // Near shot (right trigger)
                if (gamepad2.right_trigger > 0.25 && gamepad2.left_trigger < 0.1) {
                    launchSequence.start(Shooter.nearShooterVelocity, Shooter.nearTiltPosition);
                }
                // Far shot (left trigger)
                if (gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1) {
                    launchSequence.start(Shooter.farShooterVelocity, Shooter.farTiltPosition);
                }
            }
            launchSequence.update();
            if (launchSequence.isDone()) {
                launchSequence.reset();
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

            TelemetryPacket packet = new TelemetryPacket();
            packet.fieldOverlay().setStroke("#3F51B5");
            Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
           telemetry.addData("shooterVelocity ", shooter.shooterVelocity);
           telemetry.addData("shooterPower ", shooter.shooterPower);
           telemetry.update();
        }

    }
}
