
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.sin;

@Autonomous(name="Build Zone - Park", group="Linear Opmode")
//@Disabled

public class Build_Park extends Auto2_0 {

    // Declare Timer
    private ElapsedTime runtime = new ElapsedTime();

    //Declare Hardware Objects
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor scissorDrive1 = null;
    private DcMotor scissorDrive2 = null;
    private DcMotor turret = null;
    private DcMotor armSlide = null;
    private Servo clawServo = null;
    private Servo pivot = null;

    // Declare Gyro Members
    private Orientation lastAngles = new Orientation();
    private double globalAngle;

    //Declare variables for Mechanum drive
    double frontRightPower = 0;
    double frontLeftPower = 0;
    double backRightPower = 0;
    double backLeftPower = 0;

    //Constants for motor running
    final int TICKS_PER_REVOLUTION_DRIVE = 104;
    final double WHEEL_DIAMETER = 3.93;
    final double WHEEL_CIRCUMFERENCE = PI * WHEEL_DIAMETER;
    final double CENTER_TO_WHEEL = 7.5;

    @Override
    public void runOpMode() throws InterruptedException {

        //Telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        //Initialize Motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "left_drive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        scissorDrive1 = hardwareMap.get(DcMotor.class, "scissor_drive_1");
        scissorDrive2 = hardwareMap.get(DcMotor.class, "scissor_drive_2");
        turret = hardwareMap.get(DcMotor.class, "turret");
        armSlide = hardwareMap.get(DcMotor.class, "arm_slide");
        pivot = hardwareMap.get(Servo.class, "pivot");
        clawServo = hardwareMap.get(Servo.class, "claw_servo");

        //Make Motors have correct direction
        frontLeftDrive.setDirection(FORWARD);
        backLeftDrive.setDirection(FORWARD);
        frontRightDrive.setDirection(REVERSE);
        backRightDrive.setDirection(REVERSE);

        stopAndReset();

        // Wait for start
        waitForStart();
        runtime.reset();

        // run until the end of the match
        while (opModeIsActive()) {

            driveWithNoEncoders(1,90,700);

            while(opModeIsActive()){}

            //Show Telemetry Data
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Drive Motor Powers","Front Left: %.2f, Front Right: %.2f, Back Left: %.2f, Back Right: %.2f", frontLeftPower, frontRightPower, backLeftPower, backRightPower);
            telemetry.update();
        }
    }
    public void drive (double distance, int direction, double power) {

        double angle = direction * PI / 180; // 0
        double percentUnWastedRevs = abs(sin(angle+PI/4)); // sqrt2/2
        double revolutions = distance / WHEEL_CIRCUMFERENCE; // ~1.2
        double ticks = revolutions * TICKS_PER_REVOLUTION_DRIVE;

        double y = revolutions * sin(angle) * 1/percentUnWastedRevs;
        double x = revolutions * cos(angle) * 1/percentUnWastedRevs;

        double xPower = x * power;
        double yPower = y * power;

        double frontLeftRevs = x + y;
        double frontRightRevs = -x + y;
        double backLeftRevs = -x + y;
        double backRightRevs = x + y;

        //Turn inputs into wheel powers
        frontLeftPower = xPower + yPower;
        frontRightPower = - xPower + yPower;
        backLeftPower = xPower + yPower;
        backRightPower = xPower + yPower;

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

        int frontLeftTicks = (int)(round(frontLeftRevs * TICKS_PER_REVOLUTION_DRIVE));
        int frontRightTicks = (int)(round(frontRightRevs * TICKS_PER_REVOLUTION_DRIVE));
        int backLeftTicks = (int)(round(backLeftRevs * TICKS_PER_REVOLUTION_DRIVE));
        int backRightTicks = (int)(round(backRightRevs * TICKS_PER_REVOLUTION_DRIVE));

        frontLeftDrive.setTargetPosition(frontLeftTicks);
        frontRightDrive.setTargetPosition(frontRightTicks);
        backLeftDrive.setTargetPosition(frontRightTicks);
        backRightDrive.setTargetPosition(frontRightTicks);

        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower(backRightPower);

    }

    public void driveWithNoEncoders (double power, double direction, int time) throws InterruptedException {

        frontLeftDrive.setMode(RUN_WITHOUT_ENCODER);
        frontRightDrive.setMode(RUN_WITHOUT_ENCODER);
        backLeftDrive.setMode(RUN_WITHOUT_ENCODER);
        backRightDrive.setMode(RUN_WITHOUT_ENCODER);

        direction = direction * PI / 180;
        double yPower = sin(direction) * power;
        double xPower = cos (direction) * power;

        double frontLeftPower = xPower + yPower;
        double frontRightPower = -xPower + yPower;
        double backLeftPower = -xPower + yPower;
        double backRightPower = xPower + yPower;

        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower(backRightPower);

        Thread.sleep(time);

        stopAndReset();
    }

    public void rotate (double power, double degrees) {
        double distance = degrees/360 * PI * 2*CENTER_TO_WHEEL;
        double revolutions = distance / WHEEL_CIRCUMFERENCE;
        int ticks = (int)round(revolutions) * TICKS_PER_REVOLUTION_DRIVE;
        frontLeftDrive.setTargetPosition(ticks);
        backLeftDrive.setTargetPosition(ticks);
        frontRightDrive.setTargetPosition(-ticks);
        backRightDrive.setTargetPosition(-ticks);

        frontLeftDrive.setPower(power);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(power);
        backRightDrive.setPower(power);
    }

    public void stopAndReset () {
        frontRightDrive.setMode(STOP_AND_RESET_ENCODER);
        frontLeftDrive.setMode(STOP_AND_RESET_ENCODER);
        backLeftDrive.setMode(STOP_AND_RESET_ENCODER);
        backRightDrive.setMode(STOP_AND_RESET_ENCODER);

        frontRightDrive.setTargetPosition(0);
        frontLeftDrive.setTargetPosition(0);
        backLeftDrive.setTargetPosition(0);
        backRightDrive.setTargetPosition(0);

        frontRightDrive.setMode(RUN_TO_POSITION);
        frontLeftDrive.setMode(RUN_TO_POSITION);
        backLeftDrive.setMode(RUN_TO_POSITION);
        backRightDrive.setMode(RUN_TO_POSITION);


    }
}