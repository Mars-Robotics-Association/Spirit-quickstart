package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Shooter {
    private final DcMotorEx shooterMotorLeft;
    private final DcMotorEx shooterMotorRight;

    public Shooter(HardwareMap hardwareMap) {
        shooterMotorLeft = hardwareMap.get(DcMotorEx.class, "shooterMotor");
        shooterMotorRight = hardwareMap.get(DcMotorEx.class, "shooterMotor");
        shooterMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);
        shooterMotorLeft.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Set the power to the left and right motors
     */
        public void setPower(double power) {
        shooterMotorLeft.setPower(power);
        shooterMotorRight.setPower(power);
    }
}

