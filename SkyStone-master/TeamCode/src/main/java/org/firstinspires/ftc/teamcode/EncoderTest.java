package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Encoder Test", group="Linear Opmode")
//@Disabled
public class EncoderTest extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor encoder = null;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        encoder = hardwareMap.get(DcMotor.class, "left_drive");
        encoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoder.setTargetPosition(500);
        encoder.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        waitForStart();
        runtime.reset();



        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            if (gamepad1.a) {
                encoder.setTargetPosition(750);
                encoder.setPower(.5);
            }
            if (gamepad1.b) {
                encoder.setTargetPosition(0);
                encoder.setPower(.5);
            }

            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.update();
        }
    }
}
