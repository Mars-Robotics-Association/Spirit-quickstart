package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Red-alliance Near autonomous OpMode.
 *
 * @see BlueNear
 * @see AutoDetectAllianceNear
 * @see BaseNearAuto
 */
@Config
@Autonomous(name = "Red Near", group = "Autonomous")
public class RedNear extends BaseNearAuto {
    public RedNear() {
        super(false);
    }
}
