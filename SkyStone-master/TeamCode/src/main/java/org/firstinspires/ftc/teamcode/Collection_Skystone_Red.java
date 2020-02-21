
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name="Build Zone - Park", group="Linear Opmode")


public class Collection_Skystone_Red extends Auto2_0 {


    @Override
    public void runOpMode() throws InterruptedException {

        initialize();

        // run until the end of the match
        if (opModeIsActive()) {
            SkytonePostion blockPosition =filterPositions(false);

            if (blockPosition == SkytonePostion.RIGHT) {

            }
        }
    }
}
