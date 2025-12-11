package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;
import org.firstinspires.ftc.teamcode.utils.CustomEdgeDetection;
import org.firstinspires.ftc.teamcode.utils.StateAction;

import java.util.ArrayList;
import java.util.List;

@Config
@TeleOp(name = "Spirit", group = "Teleop")
public class SpiritTeleop2 extends LinearOpMode {
    public static class Config {
        public double nearShooterPower = .4;//power for shooter if bot is near target
        public double farShooterPower = .6;//power for shooter if bot is far from target
        public double nearTiltPosition = .35;
        public double farTiltPosition = .4;
        // public double carouselPosition1 = .2;//carousel position if moved forward (for testing)
        // public double carouselPosition2 = .4;//carousel position if moved backward (for testing)
    }

    public static Config config = new Config();

    CustomEdgeDetection rightTrigger = new CustomEdgeDetection(150);
    CustomEdgeDetection leftTrigger = new CustomEdgeDetection(150);

    @Override
    public void runOpMode() {
        List<Action> runningActions = new ArrayList<>();
        List<Action> newActions = new ArrayList<>();

        telemetry.setAutoClear(false);
        telemetry.clear();

        double shooterPower = 0;//power for shooter which is set to nearShooterPower or farShooterPower based on which trigger is pulled
        double tiltPosition = 0;


        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
        Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
        Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

        waitForStart();

        while (opModeIsActive() && !isStopRequested()) {
            TelemetryPacket packet = new TelemetryPacket();

            // run any pending actions,
            // any that aren't done get carried over to the next iteration of the while loop
            for (Action action : runningActions) {
                action.preview(packet.fieldOverlay());
                if (action.run(packet)) {
                    newActions.add(action);
                }
            }

            // make newActions the next runningActions, and clear newActions
            List<Action> swapTemp = runningActions;
            runningActions = newActions;
            swapTemp.clear();
            newActions = swapTemp;


            //Operate Intake: left bumper is intake and right bumper is eject
            if (gamepad2.left_bumper) {
                intake.setPower(1);
            } else if (gamepad2.right_bumper) {
                intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
            } else {
                intake.setPower(0);
            }

//----------------------JUST FOR TESTING POSITON OF KICKER SERVO-------
            if (gamepad2.bWasPressed()) {
                carousel.setHomePositionKicker();
            }
            if (gamepad2.xWasPressed()) {
                carousel.setTinyKicker();
            }
            if (gamepad2.yWasPressed()) {
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

            rightTrigger.check(gamepad2.right_trigger > 0.25 && gamepad2.left_trigger < 0.1);
            leftTrigger.check(gamepad2.left_trigger > 0.25 && gamepad2.right_trigger < 0.1);

            boolean enqueueShooterAction = false;

            if (leftTrigger.wasPressed()) {
                shooterPower = config.farShooterPower;
                tiltPosition = config.farTiltPosition;
                enqueueShooterAction = true;
            } else if (rightTrigger.wasPressed()) {
                shooterPower = config.nearShooterPower;
                tiltPosition = config.nearTiltPosition;
                enqueueShooterAction = true;
            }

            if (enqueueShooterAction) {
                runningActions.add(
                        new SequentialAction(
                                // StateAction ensures that the value of shooterPower is captured at the time the Action is created
                                new StateAction<>(shooterPower, (p) -> new InstantAction(() -> shooter.setShooterPower(p))),
                                new SleepAction(2.0),
                                new InstantAction(carousel::spinCarouselLaunchOne),
                                new SleepAction(0.2),
                                new InstantAction(carousel::setTinyKicker),
                                new SleepAction(0.2),
                                new StateAction<>(tiltPosition, (p) -> new InstantAction(() -> shooter.setTiltPosition(p))),
                                new SleepAction(0.2),
                                new InstantAction(carousel::setFullKicker),
                                new SleepAction(0.2),
                                new InstantAction(shooter::setHomeTiltPosition), // with no sleep in between, servos will move simultaneously
                                new InstantAction(carousel::setHomePositionKicker),
                                new SleepAction(0.2),
                                new InstantAction(carousel::spinCarouselLaunchThree),
                                new SleepAction(0.2),
                                new InstantAction(carousel::setTinyKicker),
                                new SleepAction(0.2),
                                new StateAction<>(tiltPosition, (p) -> new InstantAction(() -> shooter.setTiltPosition(p))),
                                new SleepAction(0.2),
                                new InstantAction(carousel::setFullKicker),
                                new SleepAction(0.2),
                                new InstantAction(shooter::setHomeTiltPosition),
                                new InstantAction(carousel::setHomePositionKicker),
                                new InstantAction(() -> shooter.setShooterPower(0))
                        )
                );
            }

            telemetry.addData("Shooter Power", shooterPower);
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

            packet.fieldOverlay().setStroke("#3F51B5");
            //Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
        }

        shooter.setShooterPower(0);
        intake.setPower(0);

        telemetry.update();
    }
}

