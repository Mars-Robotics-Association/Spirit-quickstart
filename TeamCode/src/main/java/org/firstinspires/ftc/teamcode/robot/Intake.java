package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Subsystem controlling the ball intake mechanism.
 *
 * <p>Uses a single motor ({@code "intakeMotor"}) to spin a roller that pulls balls
 * into the carousel. Positive power intakes balls; negative power ejects them.
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

    /**
     * Sets the intake motor power.
     *
     * @param power motor power from -1.0 (eject) to 1.0 (intake)
     */
    public void setPower(double power) {
        intakeMotor.setPower(power);
    }
}
