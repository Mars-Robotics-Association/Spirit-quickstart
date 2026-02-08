package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatic kS/kV tuning for each flywheel motor independently.
 *
 * <p>The opmode runs two phases per motor:
 * <ol>
 *   <li><b>Stiction detection</b> — ramp power from 0 until the flywheel starts spinning
 *       (velocity exceeds {@link #MOVING_THRESHOLD} ticks/s). The applied voltage at that
 *       point is the stiction voltage (static friction).</li>
 *   <li><b>kS/kV regression</b> — ramp power from the stiction point to 1.0, collecting
 *       (voltage, velocity) samples. A least-squares linear regression yields kS (dynamic
 *       friction intercept, volts) and kV (volts per tick/s).</li>
 * </ol>
 *
 * <p>The feedforward model uses kS and kV from regression:
 * {@code voltage = kS + kV * velocity_ticks_per_sec}. The stiction voltage is reported
 * separately for use in kick-starting motors from a dead stop.
 *
 * <p>The left motor runs first, then the right motor.
 */
@TeleOp(name = "FlywheelsFeedforwardTuning", group = "Tuning")
public class FlywheelsFeedforwardTuning extends FlywheelsTuningBase {

    /**
     * Velocity (ticks/s) above which we consider the flywheel to be moving.
     */
    private static final double MOVING_THRESHOLD = 5.0;

    /**
     * How much to increase power each loop iteration during ramping phases.
     */
    private static final double POWER_STEP = 0.001;

    /**
     * Milliseconds to pause between power steps so the motor can respond.
     */
    private static final long STEP_DELAY_MS = 30;

    /**
     * Milliseconds to let the motor settle at each power level during kV ramp.
     */
    private static final long KV_SETTLE_MS = 150;

    /**
     * Power increment between kV sample points.
     */
    private static final double KV_SAMPLE_STEP = 0.02;

    @Override
    public void runOpMode() throws InterruptedException {
        initHardware();
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready. Press START to begin tuning.");
        telemetry.update();

        waitForStart();

        // --- Tune right motor ---
        double[] rightResult = tuneMotor(rightMotor, voltageSensor, "Right");

        // --- Tune left motor ---
        double[] leftResult = tuneMotor(leftMotor, voltageSensor, "Left");

        while (opModeIsActive()) {
            // --- Report final results ---
            //telemetry.clear();
            telemetry.addData("now", System.nanoTime());
            telemetry.addLine("=== TUNING COMPLETE ===");
            telemetry.addLine("");
            if (leftResult != null) {
                telemetry.addData("Left stiction (V)", "%.4f", leftResult[0]);
                telemetry.addData("Left kS (V)", "%.4f", leftResult[1]);
                telemetry.addData("Left kV (V/(t/s))", "%.6f", leftResult[2]);
            } else {
                telemetry.addLine("Left: FAILED");
            }
            telemetry.addLine("");
            if (rightResult != null) {
                telemetry.addData("Right stiction (V)", "%.4f", rightResult[0]);
                telemetry.addData("Right kS (V)", "%.4f", rightResult[1]);
                telemetry.addData("Right kV (V/(t/s))", "%.6f", rightResult[2]);
            } else {
                telemetry.addLine("Right: FAILED");
            }
            telemetry.addLine("");
            telemetry.addLine("voltage = kS + kV * velocity_ticks_per_sec");
            telemetry.update();
        }
    }

    /**
     * Tunes a single motor, finding stiction voltage, kS (volts), and kV (volts per tick/s).
     *
     * @return {stiction, kS, kV} in volts, or null if the opmode was stopped early.
     */
    private double[] tuneMotor(DcMotorEx motor, VoltageSensor voltageSensor, String label) {
        // --- Phase 1: Find kS ---
        telemetry.addLine(label + ": Finding kS (ramping until movement)...");
        telemetry.update();

        double kSVolts = -1;
        double power = 0;
        while (opModeIsActive() && power <= 1.0) {
            motor.setPower(power);

            sleep(STEP_DELAY_MS);

            double velocity = Math.abs(motor.getVelocity());
            double voltage = power * voltageSensor.getVoltage();
            telemetry.addData(label + " Phase", "Finding kS");
            telemetry.addData(label + " Power", "%.4f", power);
            telemetry.addData(label + " Voltage", "%.2f V", voltage);
            telemetry.addData(label + " Velocity", "%.1f ticks/s", velocity);
            telemetry.update();

            if (velocity > MOVING_THRESHOLD) {
                kSVolts = voltage;
                break;
            }
            power += POWER_STEP;
        }

        motor.setPower(0);

        if (!opModeIsActive()) return null;

        if (kSVolts < 0) {
            telemetry.addLine(label + ": ERROR - motor never started moving!");
            telemetry.update();
            return null;
        }

        double stictionPower = power; // remember the duty cycle where it started moving
        telemetry.addData(label + " stiction", "%.4f V", kSVolts);
        telemetry.update();

        // Brief pause to let motor stop before kV ramp
        sleep(500);

        // --- Phase 2: Collect (voltage, velocity) samples for kV ---
        telemetry.addLine(label + ": Collecting kV samples...");
        telemetry.update();

        List<double[]> samples = new ArrayList<>();

        for (double p = stictionPower; p <= 1.0 && opModeIsActive(); p += KV_SAMPLE_STEP) {
            motor.setPower(p);

            // Let the motor settle at this power level
            sleep(KV_SETTLE_MS);

            // Take a few readings and average velocity and voltage
            double velSum = 0;
            double voltSum = 0;
            int count = 0;
            for (int i = 0; i < 5 && opModeIsActive(); i++) {
                velSum += Math.abs(motor.getVelocity());
                voltSum += p * voltageSensor.getVoltage();
                count++;
                sleep(20);
            }
            double avgVelocity = count > 0 ? velSum / count : 0;
            double avgVoltage = count > 0 ? voltSum / count : 0;

            if (avgVelocity > MOVING_THRESHOLD) {
                samples.add(new double[]{avgVoltage, avgVelocity});
            }

            telemetry.addData(label + " Phase", "Collecting kV samples");
            telemetry.addData(label + " Power", "%.4f", p);
            telemetry.addData(label + " Voltage", "%.2f V", avgVoltage);
            telemetry.addData(label + " Velocity", "%.1f ticks/s", avgVelocity);
            telemetry.addData(label + " Samples", samples.size());
            telemetry.update();
        }

        telemetry.addLine("Loop complete");
        telemetry.update();

        motor.setPower(0);

        if (samples.size() < 3) {
            telemetry.addLine(label + ": ERROR - not enough samples for regression!");
            telemetry.update();
            return null;
        }

        telemetry.addLine("start regression");
        telemetry.update();

        // --- Least-squares regression: voltage = kS + kV * velocity ---
        // x = velocity, y = voltage
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        int n = samples.size();
        for (double[] s : samples) {
            double x = s[1]; // velocity
            double y = s[0]; // voltage
            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumXY += x * y;
        }
        double kV = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - kV * sumX) / n; // refined kS in volts

        telemetry.addData(label + " kS (regression)", "%.4f V", intercept);
        telemetry.addData(label + " kV", "%.6f V/(t/s)", kV);
        telemetry.update();

        return new double[]{kSVolts, intercept, kV};
    }
}
