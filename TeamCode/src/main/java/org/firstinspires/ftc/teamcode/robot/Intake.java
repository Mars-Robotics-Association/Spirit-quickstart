package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Subsystem controlling the ball intake mechanism.
 *
 * <p>Uses a single motor ({@code "intakeMotor"}) to spin a roller that pulls balls
 * into the carousel. Call {@link #run} to intake, {@link #eject} to reverse, and
 * {@link #stop} to halt.
 *
 * @see Carousel
 */
public class Intake {
    private final DcMotorEx intakeMotor;

    /**
     * Constructs an Intake subsystem and maps the motor from hardware.
     *
     * @param hardwareMap the robot's hardware map containing {@code "intakeMotor"}
     */
    public Intake(HardwareMap hardwareMap) {
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
    }

    /** Spins the intake roller to pull balls in. */
    public void run() {
        intakeMotor.setPower(1);
    }

    /** Reverses the intake roller to eject balls. */
    public void eject() {
        intakeMotor.setPower(-1);
    }

    /** Stops the intake roller. */
    public void stop() {
        intakeMotor.setPower(0);
    }
}
