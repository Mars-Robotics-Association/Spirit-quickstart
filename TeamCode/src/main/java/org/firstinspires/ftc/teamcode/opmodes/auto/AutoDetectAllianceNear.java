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
import com.qualcomm.robotcore.hardware.ColorSensor;

import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.LaunchSequence;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;

/**
 * Color-sensor auto-selecting Near autonomous OpMode.
 *
 * <p>Uses a REV Color/Distance Sensor V2 ({@code "colorSensor"}) to detect the alliance
 * color during init. The detected color is displayed on telemetry so drivers can verify
 * before pressing Start.
 *
 * <p>On start, the robot drives backward 6 inches (by time), executes the full 3-ball
 * near-shot launch sequence, then strafes toward the field wall:
 * <ul>
 *   <li><b>Blue:</b> strafes right (+0.5 y power, 0.75 s)</li>
 *   <li><b>Red:</b> strafes left (-0.5 y power, 0.7 s)</li>
 * </ul>
 *
 * <p>Robot must be placed with the rear wells flush against the target to start.
 *
 * @see BlueNear
 * @see RedNear
 */
@Config
@Autonomous(name = "Auto-Detect Alliance Near", group = "Autonomous")
public class AutoDetectAllianceNear extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.clear();

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        Intake intake = new Intake(hardwareMap);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        Shooter shooter = new Shooter(hardwareMap, telemetry);
        Carousel carousel = new Carousel(hardwareMap);
        LaunchSequence launchSequence = new LaunchSequence(shooter, carousel, this::getRuntime);

        // Color sensor for auto-detecting alliance color
        ColorSensor colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
        boolean isBlue = true; // default to blue

        shooter.setHomeTiltPosition();

        // During init, continuously read the color sensor and display detected color
        while (!isStarted() && !isStopRequested()) {
            int red = colorSensor.red();
            int blue = colorSensor.blue();
            isBlue = blue > red;

            telemetry.addData("Detected Alliance", isBlue ? "BLUE" : "RED");
            telemetry.addData("Red Value", red);
            telemetry.addData("Blue Value", blue);
            telemetry.addData("Status", "Waiting for Start...");
            telemetry.update();
        }

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
            telemetry.addData("Alliance", isBlue ? "BLUE" : "RED");

            switch (currentState) {
                case LAUNCHING:
                    launchSequence.update();
                    if (launchSequence.isDone()) {
                        currentState = State.PARKING;
                        // Strafe to the field wall — direction based on detected color
                        double strafePower = isBlue ? 0.5 : -0.5;
                        double strafeDuration = isBlue ? 0.75 : 0.7;
                        driveTimer = getRuntime() + strafeDuration;
                        while (opModeIsActive() && getRuntime() < driveTimer) {
                            drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, strafePower), 0));
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
