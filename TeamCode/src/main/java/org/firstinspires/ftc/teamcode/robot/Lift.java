package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Subsystem controlling the lift mechanism used to raise the robot off the mat.
 *
 * <p>Uses a single encoder-controlled motor ({@code "liftMotor"}) with
 * {@link com.qualcomm.robotcore.hardware.DcMotorEx.RunMode#RUN_TO_POSITION RUN_TO_POSITION}
 * to move between a home position (0 ticks) and a fully extended position
 * ({@link #targetTicks} ticks). The motor brakes at zero power to hold position.
 */
public class Lift {
    /** Encoder target for the fully raised position. */
    int targetTicks = 1300; // 28 * 188 ticks = one motor revolution
    public final DcMotorEx liftMotor;

    /**
     * Constructs a Lift subsystem, resets the encoder, and configures the motor.
     *
     * @param hardwareMap the robot's hardware map containing {@code "liftMotor"}
     */
    public Lift(HardwareMap hardwareMap) {
        liftMotor = hardwareMap.get(DcMotorEx.class, "liftMotor");

        liftMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        liftMotor.setDirection(DcMotorEx.Direction.FORWARD);
        liftMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    }

    /** Lowers the lift to the home position (encoder position 0) at low power. */
    public void homeLift() {
        liftMotor.setTargetPosition(0);
        liftMotor.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.2);
    }

    /** Raises the lift to the target position at full power. */
    public void engageLift(){

        liftMotor.setTargetPosition(targetTicks);
        liftMotor.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(1);

    }
}