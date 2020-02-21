
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name="Build Zone - Park", group="Linear Opmode")
@Disabled

public class Park extends Auto2_0 {


    @Override
    public void runOpMode() throws InterruptedException {

        initialize();

        // run until the end of the match
        if (opModeIsActive()) {

            driveWithNoEncoders(1,90,700);
        }
    }
}
