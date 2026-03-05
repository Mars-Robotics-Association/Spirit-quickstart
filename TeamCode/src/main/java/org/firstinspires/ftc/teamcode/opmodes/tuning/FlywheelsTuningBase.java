package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.utils.DashboardTelemetryPacketAccess;
import org.firstinspires.ftc.teamcode.utils.HubHelper;

public abstract class FlywheelsTuningBase extends LinearOpMode {
    public static boolean MOTORS_COUPLED = false;
    public static String[] MOTOR_NAMES = {"shooterMotorLeft", "shooterMotorRight"};
    public static DcMotorSimple.Direction[] MOTOR_DIRECTIONS = {
            DcMotorSimple.Direction.FORWARD, DcMotorSimple.Direction.REVERSE
    };

    protected DcMotorEx[] motors;
    protected DashboardTelemetryPacketAccess packetAccess;
    protected LynxModule module;

    protected void initHardware() {
        motors = new DcMotorEx[MOTOR_NAMES.length];
        for (int i = 0; i < MOTOR_NAMES.length; i++) {
            motors[i] = hardwareMap.get(DcMotorEx.class, MOTOR_NAMES[i]);
            motors[i].setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
            motors[i].setDirection(MOTOR_DIRECTIONS[i]);
            motors[i].setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        }

        packetAccess = new DashboardTelemetryPacketAccess();
        telemetry = new MultipleTelemetry(telemetry, packetAccess.dashboardTelemetry);

        module = HubHelper.getHubForMotor(motors[0], hardwareMap);
        module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
    }

    public void stopMotors() {
        for (DcMotorEx m : motors) m.setPower(0);
    }

    @Override
    public final void runOpMode() throws InterruptedException {
        initHardware();
        runOpModeInternal();
    }

    protected abstract void runOpModeInternal() throws InterruptedException;
}
