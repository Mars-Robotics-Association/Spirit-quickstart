package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Blue-alliance Far autonomous OpMode.
 *
 * @see RedFar
 * @see BaseFarAuto
 */
@Config
@Autonomous(name = "Blue Far", group = "Autonomous")
public class BlueFar extends BaseFarAuto {
    public BlueFar() {
        super(true);
    }
}
