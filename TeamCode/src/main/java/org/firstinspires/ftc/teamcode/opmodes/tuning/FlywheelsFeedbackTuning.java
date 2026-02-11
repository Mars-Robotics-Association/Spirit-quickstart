package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

/**
 * Interactive tuning opmode for flywheel kP and IIR low-pass filter alpha values.
 *
 * <p>Paste kS and kV values from {@link FlywheelsFeedforwardTuning} into the static fields
 * below (or edit them live via FTC Dashboard). Then adjust {@link #targetTPS},
 * {@link #targetAlpha}, {@link #velocityAlpha}, and {@link #kP} while watching the telemetry
 * graphs to find values that track the target velocity quickly without excessive oscillation.
 *
 * <p>Set {@link #useLeftMotor} to {@code true} for the left motor or {@code false} for the
 * right motor. The feedforward model is {@code voltage = kS + kV * smoothedTarget}, and the
 * feedback term is {@code kP * (smoothedTarget - smoothedVelocity)}.
 */
@Config
@TeleOp(name = "Flywheels Feedback Tuning", group = "Tuning")
public class FlywheelsFeedbackTuning extends FlywheelsTuningBase {

    public static boolean useLeftMotor = true;
    public static double targetTPS = 0;
    public static double targetAlpha = 0.02;
    public static double velocityAlpha = 0.05;
    public static double kP = 0.002;

    public static double leftKS = 1.5109;
    public static double leftKV = 0.005178;
    public static double rightKS = 1.3725;
    public static double rightKV = 0.004908;

    @Override
    protected void runOpModeInternal() throws InterruptedException {
        telemetry.addLine("Ready. Press START to begin.");
        telemetry.addLine("Adjust values via FTC Dashboard.");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        double smoothedTarget = 0;
        double smoothedVelocity = 0;

        while (opModeIsActive()) {
            DcMotorEx activeMotor = motors[useLeftMotor ? 0 : 1];
            DcMotorEx idleMotor = motors[useLeftMotor ? 1 : 0];
            double kS = useLeftMotor ? leftKS : rightKS;
            double kV = useLeftMotor ? leftKV : rightKV;

            idleMotor.setPower(0);

            double rawVelocity = Math.abs(activeMotor.getVelocity());

            smoothedTarget = targetAlpha * targetTPS + (1 - targetAlpha) * smoothedTarget;
            // An IIR filter approaches its target asymptotically (never truly arrives).
            // Snap to the exact target once we're within 1% to avoid lingering error.
            if (targetTPS != 0 && Math.abs(smoothedTarget - targetTPS) / targetTPS < 0.01) {
                smoothedTarget = targetTPS;
            }
            smoothedVelocity = velocityAlpha * rawVelocity + (1 - velocityAlpha) * smoothedVelocity;

            if (targetTPS == 0) {
                activeMotor.setPower(0);
                smoothedTarget = 0;
                smoothedVelocity = 0;
            } else {
                double feedforward = kS + kV * smoothedTarget;
                double feedback = kP * (smoothedTarget - smoothedVelocity);
                double voltage = feedforward + feedback;
                double batteryVoltage = module.getInputVoltage(VoltageUnit.VOLTS);
                activeMotor.setPower(voltage / batteryVoltage);
            }

            telemetry.addData("Motor", useLeftMotor ? "Left" : "Right");
            telemetry.addData("Raw Target", "%.1f", targetTPS);
            telemetry.addData("Smoothed Target", "%.1f", smoothedTarget);
            telemetry.addData("Raw Velocity", "%.1f", rawVelocity);
            telemetry.addData("Smoothed Velocity", "%.1f", smoothedVelocity);
            telemetry.update();
        }
    }
}
