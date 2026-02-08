package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Blue-alliance Near autonomous OpMode.
 *
 * @see SpiritNearRed
 * @see SpiritNearDetectAlliance
 * @see BaseNearAuto
 */
@Config
@Autonomous(name = "SpiritNearBlue", group = "Autonomous")
public class SpiritNearBlue extends BaseNearAuto {
    public SpiritNearBlue() {
        super(true);
    }
}
