package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Blue-alliance Near autonomous OpMode.
 *
 * @see RedNear
 * @see AutoDetectAllianceNear
 * @see BaseNearAuto
 */
@Config
@Autonomous(name = "Blue Near", group = "Autonomous")
public class BlueNear extends BaseNearAuto {
    public BlueNear() {
        super(true);
    }
}
