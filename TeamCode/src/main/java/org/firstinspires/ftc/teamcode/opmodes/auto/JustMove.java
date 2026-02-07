package org.firstinspires.ftc.teamcode.opmodes.auto;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;

/**
 * Simple autonomous OpMode that drives the robot backward approximately 6 inches
 * (by time) to move off the tape. Does not shoot.
 *
 * <p>Useful as a minimal autonomous when only move points are needed.
 */
@Config
@Autonomous(name = "Just Move", group = "Autonomous")
public class JustMove extends LinearOpMode {

    public double driveTimer = 0;

    @Override
    public void runOpMode() {
        telemetry.setAutoClear(false);
        telemetry.clear();

        State currentState = State.IDLE;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

        waitForStart();

        while (opModeIsActive())
        {

//STARTING SEQUENCE----------------------------------
            switch (currentState) {

                // ------------------------------------------------------
                //  IDLE — waiting for trigger input
                // ------------------------------------------------------
                case IDLE:
                    //drive backward 12 inches (i.e., 1.5 seconds)
                    driveTimer = getRuntime()+ .4;
                    while (getRuntime() < driveTimer){
                        drive.setDrivePowers(new PoseVelocity2d(
                                new Vector2d(-1, 0

                                ),
                                0
                        ));
                    }
                    currentState = State.DONE;
                    break;

                    //stop robot wheels
                case DONE:
                    drive.setDrivePowers(new PoseVelocity2d(
                            new Vector2d(0, 0

                            ),
                            0
                    ));
                    break;
            }

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
    }

    enum State {
        IDLE,
        DONE,
    }
}