package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;

@Autonomous(name="Auto", group="Linear Opmode")
@Disabled
public class Auto extends Auto2_0 {
    //Height Values
    int ONE = 2501;
    int TWO = 3706;
    int THREE = 5005;
    int FOUR = 6203;
    int FIVE = 7643;

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;
    private DcMotor backRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor scissorDrive1 = null;
    private DcMotor scissorDrive2 = null;
    private DcMotor turret = null;
    private DcMotor armSlide = null;
    private Servo clawServo = null;
    private Servo pivot = null;
    private ColorSensor colorSensor = null;
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        leftDrive  = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        scissorDrive1 = hardwareMap.get(DcMotor.class, "scissor_drive_1");
        scissorDrive2 = hardwareMap.get(DcMotor.class, "scissor_drive_2");
        turret = hardwareMap.get(DcMotor.class, "turret");
        armSlide = hardwareMap.get(DcMotor.class, "arm_slide");
        pivot = hardwareMap.get(Servo.class, "pivot");
        clawServo = hardwareMap.get(Servo.class, "claw_servo");
        colorSensor = hardwareMap.get(ColorSensor.class, "color_sensor");

        scissorDrive1.setMode(STOP_AND_RESET_ENCODER);
        scissorDrive1.setTargetPosition(0);
        scissorDrive1.setMode(RUN_TO_POSITION);


        scissorDrive2.setMode(STOP_AND_RESET_ENCODER);
        scissorDrive2.setTargetPosition(0);
        scissorDrive2.setMode(RUN_TO_POSITION);

        turret.setMode(STOP_AND_RESET_ENCODER);
        turret.setTargetPosition(0);
        turret.setMode(RUN_TO_POSITION);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            scissorControl(TWO);

            drive(750, 1);
            rotate(500, -1);
            Thread.sleep(100);
            if(colorSensor.green() < 100) {
                scissorControl(0);
                toggleClaw(false);
                scissorControl(ONE);
                drive(1000, 1);
            }
            else {
                drive (250, 1);
                if (colorSensor.green() < 100) {
                    scissorControl(0);
                    toggleClaw(false);
                    scissorControl(ONE);
                    drive (750, 1);
                }
                else {
                    drive (250,1 );
                    scissorControl(0);
                    toggleClaw(false);
                    scissorControl(ONE);
                    drive (500, 1);
                }
            }

            drive(1000, 1);
            toggleClaw(true);
            scissorControl(TWO);
            drive(1000, -1);

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.update();
        }
    }
    private void drive(int time, double power) throws InterruptedException {
        leftDrive.setPower(power);
        rightDrive.setPower(-power);
        Thread.sleep (time - 200);
        leftDrive.setPower(-power);
        rightDrive.setPower(-power);
        Thread.sleep (200);
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }
    private void rotate (int time,double power) throws InterruptedException {
        leftDrive.setPower(power);
        rightDrive.setPower(power);
        backLeftDrive.setPower(power);
        backRightDrive.setPower(power);
        Thread.sleep(time-300);
        leftDrive.setPower(0);
        rightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
    }
    private void scissorControl (int position) {
        scissorDrive1.setTargetPosition(position);
        scissorDrive1.setPower(1);
        scissorDrive2.setTargetPosition(position);
        scissorDrive2.setPower(1);

    }
    private void setTurret (int position) {
        turret.setTargetPosition(position);
        turret.setPower(1);
    }
    private void toggleClaw (boolean toggle) {
        if (toggle) {
            clawServo.setPosition(1);
        }
        if (!toggle) {
            clawServo.setPosition(0);
        }
    }
}
