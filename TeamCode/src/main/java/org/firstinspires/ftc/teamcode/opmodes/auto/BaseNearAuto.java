package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.LaunchSequence;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;

/**
 * Abstract base class for Near autonomous OpModes.
 *
 * <p>Drives backward 6 inches (by time), executes the full 3-ball near-shot launch
 * sequence, then strafes toward the field wall to park. Subclasses set {@link #isBlue}
 * to control strafe direction:
 * <ul>
 *   <li><b>Blue:</b> strafes right (+0.5 y power, 0.75 s)</li>
 *   <li><b>Red:</b> strafes left (-0.5 y power, 0.7 s)</li>
 * </ul>
 *
 * <p>Robot must be placed with the rear wells flush against the target to start.
 *
 * @see BlueNear
 * @see RedNear
 * @see AutoDetectAllianceNear
 */
public abstract class BaseNearAuto extends LinearOpMode {

    protected boolean isBlue;

    protected BaseNearAuto(boolean isBlue) {
        this.isBlue = isBlue;
    }

    /**
     * Called after hardware is initialized but before {@link #waitForStart()}.
     * Subclasses can override to run init-phase logic (e.g. color sensor reading).
     */
    protected void onInit(Shooter shooter) {
        // default: no-op
    }

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

        onInit(shooter);

        waitForStart();

        // Start ramp-up immediately so it overlaps with the backward drive
        launchSequence.start(Shooter.nearShooterVelocity, Shooter.nearTiltPosition);

        // Drive backward 0.25s
        double driveTimer = getRuntime() + 0.25;
        while (opModeIsActive() && getRuntime() < driveTimer) {
            drive.setDrivePowers(new PoseVelocity2d(new Vector2d(1, 0), 0));
        }

        State currentState = State.LAUNCHING;

        while (opModeIsActive()) {
            telemetry.addData("Alliance", isBlue ? "BLUE" : "RED");

            switch (currentState) {
                case LAUNCHING:
                    launchSequence.update();
                    if (launchSequence.isDone()) {
                        currentState = State.PARKING;
                        // Strafe to the field wall — direction based on alliance
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
        LAUNCHING,
        PARKING,
        DONE,
    }
}
