package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Subsystem controlling the ball intake mechanism.
 *
 * <p>Uses a single motor ({@code "intakeMotor"}) to spin a roller that pulls balls
 * into the carousel. Positive power intakes balls; negative power ejects them.
 *
 * @see CarouselJimmy
 */
public class IntakeJimmy {
    private final DcMotorEx intakeMotor;
   // public static double intakePower = 0.3;

    /**
     * Constructs an Intake subsystem and maps the motor from hardware.
     *
     * @param hardwareMap the robot's hardware map containing {@code "intakeMotor"}
     */
    public IntakeJimmy(HardwareMap hardwareMap) {
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    /**
     * Sets the intake motor power.
     *
     * @param intakePower motor power from -1.0 (eject) to 1.0 (intake)
     */
    public void setPower(double intakePower) {
        intakeMotor.setPower(intakePower);
    }
}

