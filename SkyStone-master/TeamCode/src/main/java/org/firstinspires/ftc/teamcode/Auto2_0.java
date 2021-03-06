
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.ArrayList;
import java.util.List;

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

@Autonomous(name="Template", group="Linear Opmode")
@Disabled

 public abstract class Auto2_0 extends LinearOpMode{

    enum SkytonePostion {
        LEFT, CENTER, RIGHT

    }

    // Declare Timer
    ElapsedTime runtime = new ElapsedTime();

    //Declare Hardware Objects
    DcMotor frontLeftDrive = null;
    DcMotor frontRightDrive = null;
    DcMotor backRightDrive = null;
    DcMotor backLeftDrive = null;
    DcMotor scissorDrive1 = null;
    DcMotor scissorDrive2 = null;
    DcMotor turret = null;
    DcMotor armSlide = null;
    Servo clawServo = null;
    Servo pivot = null;

    // Declare Gyro Members
    Orientation lastAngles = new Orientation();
    double globalAngle;

    //Declare variables for Mechanum drive
    double frontRightPower = 0;
    double frontLeftPower = 0;
    double backRightPower = 0;
    double backLeftPower = 0;

    //Constants for motor running
    final int TICKS_PER_REVOLUTION_GOBILDA = 104;
    final double WHEEL_DIAMETER = 3.93;
    final double WHEEL_CIRCUMFERENCE = PI * WHEEL_DIAMETER;
    final double CENTER_TO_WHEEL = 7.5;

    //Constants for Vuforia
    static final String TFOD_MODEL_ASSET = "Skystone.tflite";
    static final String LABEL_FIRST_ELEMENT = "Stone";
    static final String LABEL_SECOND_ELEMENT = "Skystone";
    static final String VUFORIA_KEY =
            "AUhd2Lb/////AAABmRv7Xr0yMki7nWpz4m/jK4Iyjz08JU9WgDPtM+e3Uma9iu3hex6sMF7gLpjUA23PbhMNA4J4paGE/H2fTZ68ZN/tvN4EooA/B63SLAMPFk3NUrKWX4uWpP+tVy6sMbu4oBcp8oOtovCAmiB6KbcAS0WWkQ2AWxzC3V9Vt2P5425EySCTO8P2qAqB4nFzi9gmdew6odAZrYKtpTo7sl1pKCmgCGKDUXd7w13hBDiIHDJaGCyVqT7HjQmoYYV0uBJgnfhUEh7o5r+a8Sx8/uVqry0perht14zcQmFT9fD5WmokLNshyiUfdY2eBIqgR1+o4EqrywFMchzjCO8sT2hXfwdh+jkF0FCmhuNdpk30Fxbq";
    // position constatns
    final double LEFT_LEFT_THRESHOLD = 200;
    final double LEFT_RIGHT_THRESHOLD = 250;

    final double CENTER_LEFT_THRESHOLD = 250;
    final double CENTER_RIGHT_THRESHOLD = 300;

    final double RIGHT_LEFT_THRESHOLD = 300;
    final double RIGHT_RIGHT_THRESHOLD = 350;

    //Vuforia setup
    VuforiaLocalizer vuforia;
    TFObjectDetector tfod;

    void drive (double distance, int direction, double power) {

        double angle = direction * PI / 180; // 0
        double percentUnWastedRevs = abs(sin(angle+PI/4)); // sqrt2/2
        double revolutions = distance / WHEEL_CIRCUMFERENCE; // ~1.2
        double ticks = revolutions * TICKS_PER_REVOLUTION_GOBILDA;

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

        int frontLeftTicks = (int)(round(frontLeftRevs * TICKS_PER_REVOLUTION_GOBILDA));
        int frontRightTicks = (int)(round(frontRightRevs * TICKS_PER_REVOLUTION_GOBILDA));
        int backLeftTicks = (int)(round(backLeftRevs * TICKS_PER_REVOLUTION_GOBILDA));
        int backRightTicks = (int)(round(backRightRevs * TICKS_PER_REVOLUTION_GOBILDA));

        frontLeftDrive.setTargetPosition(frontLeftTicks);
        frontRightDrive.setTargetPosition(frontRightTicks);
        backLeftDrive.setTargetPosition(frontRightTicks);
        backRightDrive.setTargetPosition(frontRightTicks);

        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower(backRightPower);

    }

    void driveWithNoEncoders (double power, double direction, int time) throws InterruptedException {

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

        stopAndResetDrive();
    }

    void rotate (double power, double degrees) {
        double distance = degrees/360 * PI * 2*CENTER_TO_WHEEL;
        double revolutions = distance / WHEEL_CIRCUMFERENCE;
        int ticks = (int)round(revolutions) * TICKS_PER_REVOLUTION_GOBILDA;
        frontLeftDrive.setTargetPosition(ticks);
        backLeftDrive.setTargetPosition(ticks);
        frontRightDrive.setTargetPosition(-ticks);
        backRightDrive.setTargetPosition(-ticks);

        frontLeftDrive.setPower(power);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(power);
        backRightDrive.setPower(power);
    }

    void stopAndResetDrive () {
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

    void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;
        parameters.fillCameraMonitorViewParent = true;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minimumConfidence = 0.4;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }

    List<SkytonePostion> getSkystonePositions () {

        List <SkytonePostion> position = new ArrayList<>();

        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if ( updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());

                // step through the list of recognitions and display boundary info.
                for (int i = 0; i < updatedRecognitions.size(); i++) {

                    Recognition recognition = updatedRecognitions.get(i);

                    if (recognition.getLabel() == "Skystone") {

                        double left = recognition.getLeft();
                        double right = recognition.getRight();

                        if (left > LEFT_LEFT_THRESHOLD && right < LEFT_RIGHT_THRESHOLD ) position.add(SkytonePostion.LEFT);

                        else if (left > CENTER_LEFT_THRESHOLD && right < CENTER_RIGHT_THRESHOLD) position.add(SkytonePostion.CENTER);

                        else if (left > RIGHT_LEFT_THRESHOLD && right < RIGHT_RIGHT_THRESHOLD) position.add(SkytonePostion.RIGHT);

                    }
                }
            }
        }

        return position;
    }

    void initialize () {
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

        stopAndResetDrive();

        initVuforia();

        initTfod();

        if(tfod != null) {
            tfod.activate();
        }

        //Telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();


        // Wait for start
        waitForStart();
        runtime.reset();
    }

    SkytonePostion filterPositions( boolean isBlue ) {
        List<SkytonePostion> skystonePostions = getSkystonePositions();
        SkytonePostion position = SkytonePostion.RIGHT;

        if (skystonePostions.size() == 0) {
            if (isBlue) {
                position = SkytonePostion.LEFT;
            }
            if(!isBlue) {
                position = SkytonePostion.RIGHT;
            }
        }
        else if (skystonePostions.size() == 1) {
            position = skystonePostions.get(1);
        }

        return position;
    }
}