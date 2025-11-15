package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.Carousel;
import org.firstinspires.ftc.teamcode.robot.Intake;
import org.firstinspires.ftc.teamcode.robot.MecanumDrive;
import org.firstinspires.ftc.teamcode.robot.Shooter;
import org.firstinspires.ftc.teamcode.tuning.TuningOpModes;

@TeleOp(name = "Spirit", group = "Teleop")
public class SpiritTeleop extends LinearOpMode {
    @Override
    public void runOpMode() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        if (TuningOpModes.DRIVE_CLASS.equals(MecanumDrive.class)) {
            MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
            Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
            Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
            Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

            waitForStart();

            while (opModeIsActive()) {
                if (gamepad2.left_bumper) {
                    intake.setPower(1);
                } else if (gamepad2.right_bumper) {
                    intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
                }

                if (gamepad2.left_trigger > 0.25) {
                    shooter.setPower(1);
                } else if (gamepad2.right_trigger > 0.25) {
                    shooter.setPower(0.5);
                }

                if (gamepad2.y) {
                    carousel.flipFeedServo();
                }

                if(gamepad2.x) {
                    carousel.unflipFeedServo();
                }

                if (gamepad2.b) {
                    carousel.flipShooterServo();
                }

                if(gamepad2.a) {
                    carousel.unflipShooterServo();
                }
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
        }
    }
