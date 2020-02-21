
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;
import static java.lang.Math.max;
import static java.lang.Math.round;

@TeleOp(name="TeleOp 2.0", group="Linear Opmode")
//@Disabled

public class TeleOp2_0 extends LinearOpMode {

    // Declare Timer
    private ElapsedTime runtime = new ElapsedTime();

    //Declare Hardware Objects
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor scissorDrive = null;
    private Servo turret = null;
    private DcMotor armSlide = null;
    private Servo omegaPinch = null;
    private Servo omegaPivot = null;

    // Declare Gyro Members
    private Orientation lastAngles = new Orientation();
    private double globalAngle;

    //Declare variables for Mechanum drive
    double frontRightPower = 0;
    double frontLeftPower = 0;
    double backRightPower = 0;
    double backLeftPower = 0;

    //scissor variables
    double scissorPower = 0;
    int servoPosition = 0;

    //toggle values
    boolean omegaPivotToggle = false;
    boolean omegaPinchToggle = false;

    //constants
    private final int TURRET_SLEEP_TIME = 10;
    private final int TICKS_PER_REV_TORQUENADO = 1440;
    private final double SCISSOR_GEAR_REDUCTION = 3/1;
    private final double LEAD = .315;
    private final double SCISSOR_STANDOFF = 1.125;


    @Override
    public void runOpMode() throws InterruptedException {

        //Telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        //Initialize Motors
        frontLeftDrive  = hardwareMap.get(DcMotor.class, "left_drive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        scissorDrive = hardwareMap.get(DcMotor.class, "scissor_drive_1");
        turret = hardwareMap.get(Servo.class, "turret_servo");
        omegaPivot = hardwareMap.get(Servo.class, "pivot");
        omegaPinch = hardwareMap.get(Servo.class, "claw_servo");

        //Make Motors have correct direction
        frontLeftDrive.setDirection(FORWARD);
        backLeftDrive.setDirection(FORWARD);
        frontRightDrive.setDirection(REVERSE);
        backRightDrive.setDirection(REVERSE);

        //Init Scissor motor
        stopAndResetEncoder(scissorDrive);

        //Init turret
        turret.setPosition(0);

        // Wait for start
        waitForStart();
        runtime.reset();

        // run until the end of the match
        while (opModeIsActive()) {

            drive();

            scissor();

            turret();

            clawOMEGA();

            //Show Telemetry Data
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Drive Motor Powers","Front Left: %.2f, Front Right: %.2f, Back Left: %.2f, Back Right: %.2f", frontLeftPower, frontRightPower, backLeftPower, backRightPower);
            telemetry.addData("Scissor Lift Motor Power,", scissorDrive.getPower());
            telemetry.update();
        }
    }
    private void drive() {
        //mechanum drive
        //Assign inputs to variables
        double rotationInput = gamepad1.right_stick_x;
        double xInput = gamepad1.left_stick_x;
        double yInput = -gamepad1.left_stick_y;

        //Turn inputs into wheel powers
        frontLeftPower = rotationInput + xInput + yInput;
        frontRightPower = -rotationInput + - xInput + yInput;
        backLeftPower = rotationInput + - xInput + yInput;
        backRightPower = -rotationInput + xInput + yInput;

        //Find the highest input to see if we need to scale it.
        double highestPower = max(frontLeftPower, max(frontRightPower, max(backRightPower, backLeftPower)));

        //Scale down input if it is too great
        if(highestPower > 1) {

            double scaleFactor = 1 / highestPower;

            backRightPower *= scaleFactor;
            backLeftPower *= scaleFactor;
            frontRightPower *= scaleFactor;
            frontLeftPower *= scaleFactor;
        }
        //Send power to wheels
        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower((backRightPower));
    }

    private void scissor() throws InterruptedException {
        if (gamepad2.right_trigger != 0 || gamepad2.left_trigger != 0) {
            if (gamepad2.right_trigger != 0) {
                scissorDrive.setTargetPosition(scissorDrive.getCurrentPosition()-14);
                scissorDrive.setPower(1);
                Thread.sleep(TURRET_SLEEP_TIME);

            } else if (gamepad2.left_trigger != 0) {
                scissorDrive.setTargetPosition(scissorDrive.getCurrentPosition()+14);
                scissorDrive.setPower(1);
                Thread.sleep(TURRET_SLEEP_TIME);
            } else {
                scissorPower = 0;
            }
        }
        if (gamepad2.a) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(1));
        }
        if (gamepad2.b) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(2));
        }
        if(gamepad2.y) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(3));
        }
        if(gamepad2.x) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(4));
        }
        if(gamepad2.dpad_right) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(5));
        }
        if(gamepad2.dpad_up) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(6));
        }
        if (gamepad2.dpad_left) {
            scissorDrive.setTargetPosition(getScissorRaiseRevs(7));
        }

        if(gamepad2.dpad_down) {
            scissorDrive.setTargetPosition(0);
        }
        scissorDrive.setPower(1);

    }

    private void turret() throws InterruptedException {
        if (gamepad2.left_bumper) {
            double currentPosition = turret.getPosition();
            turret.setPosition(currentPosition+.01);
            Thread.sleep(TURRET_SLEEP_TIME);
        }
        if (gamepad2.right_bumper) {
            double currentPosition = turret.getPosition();
            turret.setPosition(currentPosition - .01);
            Thread.sleep(TURRET_SLEEP_TIME);
        }
    }

    private void stopAndResetEncoder(DcMotor dcMotor) {
        dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        dcMotor.setTargetPosition(0);
        dcMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    private int getScissorRaiseRevs(int blockHeight) {
        double deltaHeight = 4*(blockHeight-1) + 3;
        if (blockHeight > 1) deltaHeight += 1;
        double deltaLead = ((deltaHeight - 2*SCISSOR_STANDOFF)/5);
        double leadScrewRevs = deltaLead/LEAD;
        double motorRevs = leadScrewRevs / SCISSOR_GEAR_REDUCTION;
        int motorTicks = (int)round(motorRevs * TICKS_PER_REV_TORQUENADO);
        return motorTicks;
    }

    private void clawOMEGA() throws InterruptedException {
        if (gamepad1.a && !omegaPinchToggle) {
            omegaPinch.setPosition(1);
            Thread.sleep(200);
            omegaPinchToggle=true;
        }
        if (gamepad1.a && omegaPinchToggle) {
            omegaPinch.setPosition(0);
            Thread.sleep(200);
            omegaPinchToggle=false;
            }

        if (gamepad1.b && !omegaPivotToggle) {
            omegaPivot.setPosition(1);
            Thread.sleep(200);
            omegaPivotToggle = true;
        }
        if (gamepad1.b && omegaPivotToggle) {
            omegaPivot.setPosition(0);
            Thread.sleep(200);
            omegaPivotToggle = false;
        }
    }
}