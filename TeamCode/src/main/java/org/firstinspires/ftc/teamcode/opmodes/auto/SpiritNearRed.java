package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Red-alliance Near autonomous OpMode.
 *
 * @see SpiritNearBlue
 * @see SpiritNearDetectAlliance
 * @see BaseNearAuto
 */
@Config
@Autonomous(name = "SpiritNearRed", group = "Autonomous")
public class SpiritNearRed extends BaseNearAuto {
    public SpiritNearRed() {
        super(false);
    }
}
