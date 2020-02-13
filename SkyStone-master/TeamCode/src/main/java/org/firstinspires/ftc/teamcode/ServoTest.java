package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Servo Test", group="Linear Opmode")
//@Disabled
public class ServoTest extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private Servo test = null;

    String status = "none";
    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        test = hardwareMap.get(Servo.class, "test");

        if(gamepad1.a) {
            test.setPosition(1);
            status = "1";
        }
        if(gamepad1.b) {
            test.setPosition(.5);
            status = ".5";
        }
        if (!gamepad1.a && !gamepad2.b) {
            status = "none";
        }
        waitForStart();
        runtime.reset();



        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            if(gamepad1.a) {
                test.setPosition(1);
                status = "1";
            }
            if(gamepad1.b) {
                test.setPosition(.4);
                status = ".4";
            }
            if (!gamepad1.a && !gamepad2.b) {
                status = "none";
            }



            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Status", status);
            telemetry.update();
        }
    }
}
