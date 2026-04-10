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
 * @see CarouselJimmy
 */
@Config
public class ShooterJimmy {

    public DcMotorEx shooterMotorLeft;
    public DcMotorEx shooterMotorRight;


    static public double shooterPower = 0;
    static public double nearShooterPower = 0.5;//tps for use with encoders to set shooter speed
    static public double farShooterPower = 0.7;//tps for use with encoders to set shooter speed

    //*********************************************

    /**
     * Constructs a Shooter subsystem and maps the motors and tilt servo from hardware.
     * The right motor is reversed so both flywheels spin inward.
     *
     * @param hardwareMap the robot's hardware map containing {@code "shooterMotorLeft"},
     *                    {@code "shooterMotorRight"}, and {@code "tiltServo"}
     */
    public ShooterJimmy(HardwareMap hardwareMap) {

        shooterMotorLeft = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        shooterMotorRight.setDirection(DcMotorSimple.Direction.FORWARD);//one of the launchers has to spin in opposite direction
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
}



