package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Subsystem controlling the dual-flywheel ball shooter.
 *
 * <p>The shooter uses two motors ({@code "shooterMotorLeft"} and {@code "shooterMotorRight"})
 * to launch balls. Both motors run in the FORWARD direction.
 *
 * <p>Fields annotated with {@code @Config} are tunable via FTC Dashboard.
 *
 * @see CarouselJimmy
 */
@Config
public class ShooterJimmy {

    public final DcMotorEx shooterMotorLeft;
    public final DcMotorEx shooterMotorRight;

    public static double shooterPower     = 0;
    public static double nearShooterPower = 0.5;
    public static double farShooterPower  = 0.5;//TODO: Figure out the value of power needed to launch from the back wall

    /**
     * Constructs a Shooter subsystem and maps the motors from hardware.
     *
     * @param hardwareMap the robot's hardware map containing {@code "shooterMotorLeft"}
     *                    and {@code "shooterMotorRight"}
     */
    public ShooterJimmy(HardwareMap hardwareMap) {
        shooterMotorLeft  = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");

        shooterMotorLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        shooterMotorRight.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Sets the power on both shooter motors.
     * Passing 0 immediately stops both motors.
     *
     * @param shooterPower desired motor power (0.0 to 1.0)
     * @param telemetry    telemetry instance for logging diagnostics
     */
    public void setShooterPower(double shooterPower, Telemetry telemetry) {
        if (shooterPower == 0) {
            shooterMotorLeft.setPower(0);
            shooterMotorRight.setPower(0);
            return;
        }
        shooterMotorLeft.setPower(shooterPower);
        shooterMotorRight.setPower(shooterPower);
    }
}
