package org.firstinspires.ftc.teamcode.opmodes.auto;

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
import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.LaunchSequence;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;

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
 * @see BlueFar
 */
@Config
@Autonomous(name = "Red Far", group = "Autonomous")
public class RedFar extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.clear();

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        Shooter shooter = new Shooter(hardwareMap, telemetry);
        Carousel carousel = new Carousel(hardwareMap);
        LaunchSequence launchSequence = new LaunchSequence(shooter, carousel, this::getRuntime);

        shooter.setHomeTiltPosition();
        waitForStart();

        State currentState = State.LAUNCHING;

        // Start ramp-up immediately
        launchSequence.start(Shooter.farShooterVelocity, Shooter.farTiltPosition);

        while (opModeIsActive()) {

            switch (currentState) {
                case LAUNCHING:
                    launchSequence.update();
                    if (launchSequence.isDone()) {
                        currentState = State.PARKING;
                        // Drive forward 0.4s
                        double driveTimer = getRuntime() + 0.4;
                        while (opModeIsActive() && getRuntime() < driveTimer) {
                            drive.setDrivePowers(new PoseVelocity2d(new Vector2d(-1, 0), 0));
                        }
                        currentState = State.DONE;
                    }
                    break;

                case DONE:
                    drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0));
                    break;
            }

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

    enum State {
        LAUNCHING,
        PARKING,
        DONE,
    }
}
