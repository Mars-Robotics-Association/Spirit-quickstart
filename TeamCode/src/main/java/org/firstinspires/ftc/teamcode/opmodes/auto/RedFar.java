package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Red-alliance Far autonomous OpMode.
 *
 * @see BlueFar
 * @see BaseFarAuto
 */
@Config
@Autonomous(name = "Red Far", group = "Autonomous")
public class RedFar extends BaseFarAuto {
    public RedFar() {
        super(false);
    }
}
