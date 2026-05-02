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
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see CarouselTimmy
 */
@Config
public class ShooterTimmy {

    public final DcMotorEx shooterMotorLeft;
    public final DcMotorEx shooterMotorRight;
    public final Servo tiltServo;

    public static double nearTiltPosition = .03;
    public static double farTiltPosition  = .15;
    public static double homeTiltPosition = 0;

    public static double shooterPower     = 0;
    public static double nearShooterPower = .3;
    public static double farShooterPower  = .425;

    /**
     * Constructs a Shooter subsystem and maps the motors and tilt servo from hardware.
     * The right motor is reversed so both flywheels spin inward.
     *
     * @param hardwareMap the robot's hardware map containing {@code "shooterMotorLeft"},
     *                    {@code "shooterMotorRight"}, and {@code "tiltServo"}
     */
    public ShooterTimmy(HardwareMap hardwareMap) {
        shooterMotorLeft  = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        tiltServo = hardwareMap.get(Servo.class, "tiltServo");

        shooterMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);
        shooterMotorLeft.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Sets the power on both shooter motors.
     * Passing 0 immediately stops both motors.
     *
     * @param shooterPower desired motor power (0.0 to 1.0)
     * @param telemetry    telemetry instance for logging diagnostics
     */
    public void setShooterPower(double shooterPower) {
        if (shooterPower == 0) {
            shooterMotorLeft.setPower(0);
            shooterMotorRight.setPower(0);
            return;
        }
        shooterMotorLeft.setPower(shooterPower);
        shooterMotorRight.setPower(shooterPower);
    }

    /**
     * Sets the tilt servo to an arbitrary position.
     *
     * @param tiltPosition servo position (0.0 to 1.0)
     */
    public void setTiltPosition(double tiltPosition) {
        tiltServo.setPosition(tiltPosition);
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
