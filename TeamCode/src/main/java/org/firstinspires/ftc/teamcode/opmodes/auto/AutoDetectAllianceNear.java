package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.robot.AllianceSensor;
import org.firstinspires.ftc.teamcode.robot.Shooter;

/**
 * Color-sensor auto-selecting Near autonomous OpMode.
 *
 * <p>Uses an {@link AllianceSensor} to detect the alliance color during init.
 * The detected color is displayed on telemetry so drivers can verify before
 * pressing Start.
 *
 * @see BlueNear
 * @see RedNear
 * @see BaseNearAuto
 */
@Config
@Autonomous(name = "Auto-Detect Alliance Near", group = "Autonomous")
public class AutoDetectAllianceNear extends BaseNearAuto {

    public AutoDetectAllianceNear() {
        super(true); // default to blue; overridden by color sensor in onInit
    }

    @Override
    protected void onInit(Shooter shooter) {
        AllianceSensor allianceSensor = new AllianceSensor(hardwareMap);

        // During init, continuously read the color sensor and display detected color
        while (!isStarted() && !isStopRequested()) {
            AllianceSensor.Alliance alliance = allianceSensor.getAlliance();
            isBlue = (alliance == AllianceSensor.Alliance.BLUE);

            telemetry.addData("Detected Alliance", alliance);
            telemetry.addData("Red Value", allianceSensor.getRed());
            telemetry.addData("Blue Value", allianceSensor.getBlue());
            telemetry.addData("Status", "Waiting for Start...");
            telemetry.update();
        }
    }
}
