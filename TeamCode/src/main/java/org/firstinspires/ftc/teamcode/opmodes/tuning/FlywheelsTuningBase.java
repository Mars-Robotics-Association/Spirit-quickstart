package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.VoltageSensor;

public abstract class FlywheelsTuningBase extends LinearOpMode {
    protected DcMotorEx leftMotor;
    protected DcMotorEx rightMotor;
    protected VoltageSensor voltageSensor;

    protected void initHardware() {
        leftMotor = hardwareMap.get(DcMotorEx.class, "shooterMotorLeft");
        leftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        leftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        rightMotor = hardwareMap.get(DcMotorEx.class, "shooterMotorRight");
        rightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        rightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        voltageSensor = hardwareMap.voltageSensor.iterator().next();
    }


}
