package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Drawing;

@Config
@Autonomous(name = "SpiritAutonomousByTime2", group = "Autonomous")
public class TestingSpiritAutonomousByTime2 extends LinearOpMode {

    int launchStep = 0;
    int t = 0;
    double stepStartTime = 0;

    public static double tiltToLaunchDelay = 1.0;

    double shooterPower = 0.6;   // choose near/far here
    double tiltPosition;

    State currentState = State.LAUNCHING;

    @Override
    public void runOpMode() {

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        ShooterForSenecaValley shooter = new ShooterForSenecaValley(hardwareMap);
        Carousel carousel = new Carousel(hardwareMap);

        shooter.setHomeTiltPosition();
        tiltPosition = shooter.farTiltPosition;

        telemetry.addLine("Auto Ready");
        telemetry.update();

        waitForStart();
        stepStartTime = getRuntime();

        while (opModeIsActive()) {

            switch (currentState) {

                /* ---------------- LAUNCH ---------------- */
                case LAUNCHING:
                    runLaunchSequence(shooter, carousel);
                    break;

                /* ---------------- DRIVE FORWARD ---------------- */
                case DRIVE_FORWARD:
                    drive.setDrivePowers(new PoseVelocity2d(
                            new Vector2d(0.5, 0), 0
                    ));
                    if (elapsed(0.5)) {
                        stepStartTime = getRuntime();
                        currentState = State.STRAFE_RIGHT;
                    }
                    break;

                /* ---------------- STRAFE RIGHT ---------------- */
                case STRAFE_RIGHT:
                    drive.setDrivePowers(new PoseVelocity2d(
                            new Vector2d(0, -0.5), 0
                    ));
                    if (elapsed(1.0)) {

                        drive.setDrivePowers(new PoseVelocity2d(
                                        new Vector2d(-.5, -.5

                                        ),
                                        90
                                ));
                        currentState = State.DONE;
                    }
                    break;

                case DONE:
                    t++;
                    break;
            }

            drive.updatePoseEstimate();

            Pose2d pose = drive.localizer.getPose();
            TelemetryPacket packet = new TelemetryPacket();
            Drawing.drawRobot(packet.fieldOverlay(), pose);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
        }
    }

    /* ---------------- LAUNCH STEP MACHINE ---------------- */

    void runLaunchSequence(ShooterForSenecaValley shooter, Carousel carousel) {

        switch (launchStep) {

            case 0:
                shooter.setShooterPower(shooterPower);
                carousel.spinCarouselLaunchOne();
                next();
                break;

            case 1:
                if (elapsed(0.95)) {
                    carousel.setTinyKicker();
                    next();
                }
                break;

            case 2:
                if (elapsed(0.4)) {
                    shooter.setTiltPosition(tiltPosition);
                    next();
                }
                break;

            case 3:
                if (elapsed(tiltToLaunchDelay)) {
                    carousel.setFullKicker();
                    next();
                }
                break;

            case 4:
                if (elapsed(0.4)) {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                    next();
                }
                break;

            case 5:
                if (elapsed(0.4)) {
                    carousel.spinCarouselLaunchTwo();
                    next();
                }
                break;

            case 6:
                if (elapsed(0.4)) {
                    carousel.setTinyKicker();
                    next();
                }
                break;

            case 7:
                if (elapsed(0.4)) {
                    shooter.setTiltPosition(tiltPosition);
                    next();
                }
                break;

            case 8:
                if (elapsed(tiltToLaunchDelay)) {
                    carousel.setFullKicker();
                    next();
                }
                break;

            case 9:
                if (elapsed(0.4)) {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                    next();
                }
                break;

            case 10:
                if (elapsed(0.4)) {
                    carousel.spinCarouselLaunchThree();
                    next();
                }
                break;

            case 11:
                if (elapsed(0.4)) {
                    carousel.setTinyKicker();
                    next();
                }
                break;

            case 12:
                if (elapsed(0.5)) {
                    shooter.setTiltPosition(tiltPosition);
                    next();
                }
                break;

            case 13:
                if (elapsed(tiltToLaunchDelay)) {
                    carousel.setFullKicker();
                    next();
                }
                break;

            case 14:
                if (elapsed(0.4)) {
                    shooter.setHomeTiltPosition();
                    carousel.setHomePositionKicker();
                    shooter.setShooterPower(0);
                    stepStartTime = getRuntime();
                    currentState = State.DRIVE_FORWARD;
                }
                break;
        }
    }

    boolean elapsed(double t) {
        return getRuntime() - stepStartTime > t;
    }

    void next() {
        stepStartTime = getRuntime();
        launchStep++;
    }

    enum State {
        LAUNCHING,
        DRIVE_FORWARD,
        STRAFE_RIGHT,
        DONE
    }
}

