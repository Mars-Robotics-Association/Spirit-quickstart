package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Subsystem controlling the dual-flywheel ball shooter and its tilt servo.
 *
 * <p>The shooter uses two motors ({@code "shooterMotorLeft"} and {@code "shooterMotorRight"})
 * spinning in opposite directions to launch balls, and a servo ({@code "tiltServo"}) to
 * adjust the launch angle for near vs. far targets.
 *
 *
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see CarouselTimmy
 */
@Config
public class ShooterTimmy {

    public final DcMotorEx shooterMotorLeft;
    public final DcMotorEx shooterMotorRight;
    public final Servo tiltServo;
    static public double nearTiltPosition = .03;//for testing
    static public double farTiltPosition = .15;//for testing
    static public double homeTiltPosition = 0;//for testing

    static public double shooterPower = 0;
    static public double nearShooterPower = .3;//tps for use with encoders to set shooter speed
    //ToDo: find the correct value of farShooterPower to shoot from the back wall.  .1 is a place holder
    static public double farShooterPower = .1;//tps for use with encoders to set shooter speed

    //*********************************************

    /**
     * Constructs a Shooter subsystem and maps the motors and tilt servo from hardware.
     * The right motor is reversed so both flywheels spin inward.
     *
     * @param hardwareMap the robot's hardware map containing {@code "shooterMotorLeft"},
     *                    {@code "shooterMotorRight"}, and {@code "tiltServo"}
     */
    public ShooterTimmy(HardwareMap hardwareMap) {

        shooterMotorLeft = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        //enable the encoders on the shooter motors
       // shooterMotorLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
       // shooterMotorRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");//controls angle of shooters

        shooterMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);//one of the launchers has to spin in opposite direction
        shooterMotorLeft.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Runs the feedforward + feedback velocity control loop for both shooter motors.
     *
     * <p>Applies exponential smoothing to both the target and actual velocities, then
     * computes a proportional correction term added to the static feedforward power.
     * If {@code shooterVelocity} is 0, both motors are stopped immediately.
     *
     * @param shooterPower desired flywheel velocity in ticks per second
     * @param telemetry       telemetry instance for logging velocity diagnostics
     */
    public void setShooterPower(double shooterPower, Telemetry telemetry) {
        shooterMotorLeft.setPower(shooterPower);
        shooterMotorRight.setPower(shooterPower);

        //fail safe
        if (shooterPower == 0){
           shooterMotorLeft.setPower(0);
           shooterMotorRight.setPower(0);
           return;
       }
    }



    /**
     * Sets the tilt servo to an arbitrary position.
     *
     * @param tiltPositon servo position (0.0 to 1.0)
     */
    public void setTiltPosition(double tiltPositon) {
        tiltServo.setPosition(tiltPositon);
    }

    /**
     * Sets the tilt servo to the near-target launch angle.
     *
     * @param nearTiltPosition servo position for near shots
     */
    public void setNearTiltPosition(double nearTiltPosition) {
        tiltServo.setPosition(nearTiltPosition);
    }

    /**
     * Sets the tilt servo to the far-target launch angle.
     *
     * @param farTiltPosition servo position for far shots
     */
    public void setFarTiltPosition(double farTiltPosition) {
        tiltServo.setPosition(farTiltPosition);
    }

    /** Resets the tilt servo to the home (flat) position. */
    public void setHomeTiltPosition() {
        tiltServo.setPosition(homeTiltPosition);
    }

}



