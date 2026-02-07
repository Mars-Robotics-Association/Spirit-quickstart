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
 * Red-alliance Near autonomous OpMode.
 *
 * <p>Drives backward 6 inches (by time), executes the full 3-ball near-shot launch
 * sequence, then strafes left (-0.5 y power, 0.7 s) to park against the field wall
 * and out of the way of the alliance partner.
 *
 * <p>Robot must be placed with the rear wells flush against the red target to start.
 *
 * @see BlueNear
 * @see AutoDetectAllianceNear
 */
@Config
@Autonomous(name = "Red Near", group = "Autonomous")
public class RedNear extends LinearOpMode {

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

        State currentState = State.DRIVING_BACK;

        // Start ramp-up immediately so it overlaps with the backward drive
        launchSequence.start(Shooter.nearShooterVelocity, Shooter.nearTiltPosition);

        // Drive backward 0.25s
        double driveTimer = getRuntime() + 0.25;
        while (opModeIsActive() && getRuntime() < driveTimer) {
            drive.setDrivePowers(new PoseVelocity2d(new Vector2d(1, 0), 0));
        }
        currentState = State.LAUNCHING;

        while (opModeIsActive()) {

            switch (currentState) {
                case LAUNCHING:
                    launchSequence.update();
                    if (launchSequence.isDone()) {
                        currentState = State.PARKING;
                        // Strafe to the field wall
                        driveTimer = getRuntime() + 0.7;
                        while (opModeIsActive() && getRuntime() < driveTimer) {
                            drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, -0.5), 0));
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
        DRIVING_BACK,
        LAUNCHING,
        PARKING,
        DONE,
    }
}
