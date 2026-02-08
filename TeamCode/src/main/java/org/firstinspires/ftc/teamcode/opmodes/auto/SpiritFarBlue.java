package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Blue-alliance Far autonomous OpMode.
 *
 * @see SpiritFarRed
 * @see BaseFarAuto
 */
@Config
@Autonomous(name = "SpiritFarBlue", group = "Autonomous")
public class SpiritFarBlue extends BaseFarAuto {
    public SpiritFarBlue() {
        super(true);
    }
}
