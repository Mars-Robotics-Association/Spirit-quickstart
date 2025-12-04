package org.firstinspires.ftc.teamcode.robot;
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
public class SpiritTeleop2 extends LinearOpMode {
    @Override
    public void runOpMode() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        double rampUpTimer = 0;
        boolean rampUpFlag = false;
        int i = 0;//a variable to give make the code "wait" until the required number of seconds have passed to begin launching the artifact
        int carouselCounter = 0;//counts the number of times the carousel has advanced 120 degrees. After 3 advances, we need to rest the carousel's position
double nearShooterPower = .4;
double farShooterPower = .6;
double shooterPower = 0;
        State currentState = State.IDLE;
        double stateStartTime = 0;

        if (TuningOpModes.DRIVE_CLASS.equals(MecanumDrive.class)) {
            MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
            Intake intake = new Intake(hardwareMap);//instantiate a new intake motor
            Shooter shooter = new Shooter(hardwareMap);//instantiate a new shooter
            Carousel carousel = new Carousel(hardwareMap);//instantiate a new carousel

            waitForStart();

            while (opModeIsActive())
            {
                if (gamepad2.left_bumper) {
                    intake.setPower(1);
                } else if (gamepad2.right_bumper) {
                    intake.setPower(-1);//sets the power on the intake motor based on the values from the bumps
                } else {
                    intake.setPower(0);
                }
//------------------CODE FOR WHEN THE TRIGGERS ARE PRESSED----------------------------------
                //--------------LEFT TRIGGER PULLED (CLOSE TO TARGET) ------------------------------

                switch (currentState) {

                    case IDLE:
                        //handleIdle();
                        shooter.setShooterPower(0.00);//stop shooter
                        carousel.releaseShooterServo();//move shooter feeder to down position

                        if ((gamepad2.right_trigger >.25) && (gamepad2.left_trigger <.01))
                         {
                           shooterPower = nearShooterPower;
                           shooter.setShooterPower(shooterPower);//set power to shoot near target
                            currentState = State.RAMPING;
                            rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                        }
                        if ((gamepad2.left_trigger >.25) && (gamepad2.right_trigger <.01))
                        {
                            shooterPower=farShooterPower;
                            shooter.setShooterPower(shooterPower);//set power to shoot far from target
                            currentState = State.RAMPING;
                            rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                        }
                        break;

                    case RAMPING:
                        if(getRuntime() > rampUpTimer) {
                            shooter.setShooterPower(shooterPower);//set power to shooter
                            currentState = State.LAUNCHING;
                        }
                        break;

                    case LAUNCHING:
                        if(getRuntime() > rampUpTimer) {
                            shooter.setShooterPower(shooterPower);//reapply power to shooter
                            //add line to lift
                            carousel.engageShooterServo();
                            carousel.releaseShooterServo();

                            //carousel.resetCarousel();
                            currentState = State.IDLE;
                        }
                        break;


                }
                telemetry.addData("State", currentState);
                telemetry.addData("Shooter Power", shooterPower);
                //telemetry.addData("Servo Pos", carousel.getPosition());
                telemetry.update();
 //------------------------END OF SHOOTER CONTROL-----------------------------------


  /*  THIS CODE IS NOT NEEDED AND WILL BE DELETED AFTER THE CLASS IS REWRITTEN
//------------------------------------ CODE FOR RIGHT TRIGGER ----------------------------
                    if ((gamepad2.right_trigger >.25) && (gamepad2.left_trigger <.01)) {
                        if (rampUpFlag == false) {
                            rampUpFlag = true;
                            rampUpTimer = getRuntime() + 2;//set a timer to the length of time the op has been running + 2 seconds
                            shooter.setPower(.4);//ramp up the shooter
                        }else{
                            shooter.setPower(0);
                            rampUpFlag = false;
                        }
                        //check to see if the shooter has ramped up for the required number of seconds.
                        //if so, reapply power, advance carousel, engage teardrop, reset teardrop,
                        //and count how many times the carousel has advanced (at 3, reset it to original position)
                        if (rampUpFlag == true) {
                            shooter.setPower(.4);
                            if (getRuntime() > rampUpTimer) {
                                if (carouselCounter < 5) {
                                    //add code to advance carousel by 120 degrees which is .06 (120 degrees/1800 total degrees)
                                    //carousel.flipShooterServo();
                                    //carousel.unflipShooterServo();
                                    carouselCounter++;
                                } else {
                                    //carousel.resetCarousel(0);
                                    carouselCounter = 0;
                                }
//this is a loop to make the code "wait" until the require number of seconds has passed (if statement cannot be empty so we gave it a task to do
                            } else {
                                i++;
                            }
                        } //else {
                          //  shooter.setPower(0);
                          //  shooterFlag = false;
                       // }

//------------------END CODE FOR TRIGGERS--------------------------------------
*/
                if (gamepad2.y) {
                    // carousel.flipFeedServo();
                }

                if (gamepad2.x) {
                    //  carousel.unflipFeedServo();
                }

                if (gamepad2.a) {
                    if (carouselCounter <= 5) {
                        // carousel.advanceCarousel();
                        carouselCounter++;
                    } else {
                        //
                        // carousel.resetCarousel();
                        carouselCounter = 0;
                    }
                }

                if (gamepad2.b) {
                    intake.setPower(0);
                    telemetry.addData("button b", "button b");
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
                //Drawing.drawRobot(packet.fieldOverlay(), pose);
                FtcDashboard.getInstance().sendTelemetryPacket(packet);
            }
        }
        telemetry.update();


        }

    private enum State {
        IDLE,
        RAMPING,
        LAUNCHING,

    }
}

