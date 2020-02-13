/*
I Herby declare this code to by Copyrighted by ME! © Porter T. Dansie 2019
*/

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;

@TeleOp(name="Teleop1.0", group="Linear Opmode")
//@Disabled
public class TeleOp1_0 extends LinearOpMode {

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
    private Orientation lastAngles = new Orientation();
    private double globalAngle;


    @Override
    public void runOpMode() throws InterruptedException {
        //Add initialized status to telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables.
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

        //Initialize all Encoders
        resetEncoders(scissorDrive1);
        resetEncoders(scissorDrive2);
        double scissorPower = 0;

        // Reverse the motor that runs backwards
        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);

        //Constants for multiplying stuff by
        double DRIVE_FACTOR = .5;
        double CORRECTION_FACTOR = .3;

        //constants for acceleration
        double ACCEL = .1;
        int Y_ACCEL_TIME = 100;
        int X_ACCEL_TIME = 150;

        //ensure power variables are initialized in case something goes terribly wrong
        double leftPower = 0;
        double rightPower = 0;
        double backPower = 0;

        //Initialize status string for telemetry
        String driveStatus = "None";

        // Wait for the game to start and take care of some business
        waitForStart();

        runtime.reset();

        //insure power variables have a value
        double yPower = gamepad1.left_stick_y;
        double xPower = gamepad1.left_stick_x;

        //insure position value has value
        int position = 0;

        //claw servo
        boolean clawToggle = false;
        clawServo.setPosition(0);

        //pivot servo
        boolean pivotToggle = false;
        pivot.setPosition(0);
        //initialize the gyros with the correct parameters
        /*BNO055IMU.Parameters gyroParameters = new BNO055IMU.Parameters();
        gyroParameters.mode = BNO055IMU.SensorMode.IMU;
        gyroParameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        gyroParameters.accelUnit =  BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        gyroParameters.loggingEnabled = false;

        gyro.initialize(gyroParameters);

        resetAngle();
        */
        while (opModeIsActive()) {

            //get robot angle for self correction drive
            //double angle = getAngle();

            //initialize factor for rotation
            double rotationFactor = .45;

            //turbo mode
            if(gamepad1.right_stick_button) {
                rotationFactor = 1;
            }

            //traction control y
            //Limit Acceleration
            if ((yPower < 1) && (gamepad1.left_stick_y > 0)) {
                if (gamepad1.left_stick_y > .5 && yPower < .5) {
                    yPower = .5;
                }
                yPower += ACCEL;
                Thread.sleep(Y_ACCEL_TIME);
            }
            if ((yPower > -1) && (gamepad1.left_stick_y < 0)) {
                if (gamepad1.left_stick_y < -.5 && yPower > -.5) {
                    yPower = -.5;
                }
               yPower -= ACCEL;
                Thread.sleep(Y_ACCEL_TIME);
            }

            //If no input detected, set power to 0
            if (gamepad1.left_stick_y == 0) {
                yPower = 0;
            }

            //ensure power does not exceed gamepad input
            if ((yPower > gamepad1.left_stick_y) && (gamepad1.left_stick_y > 0)) {
                yPower = gamepad1.left_stick_y;
            }
            if ((yPower < gamepad1.left_stick_y) && (gamepad1.left_stick_y < 0)) {
                yPower = gamepad1.left_stick_y;
            }

            //traction control x
            if ((xPower < 1) && (gamepad1.left_stick_x > 0)) {
                if ( gamepad1.left_stick_x > .5 && xPower < .5) {
                    xPower = .5;
                }
                xPower += ACCEL;
                Thread.sleep(X_ACCEL_TIME);

            }
            if ((xPower > -1) && (gamepad1.left_stick_x < 0)) {
                if ( gamepad1.left_stick_x < -.5 && xPower > -.5) {
                    xPower = -.5;
                }
                xPower -= ACCEL;
                Thread.sleep(X_ACCEL_TIME);
            }
            if (gamepad1.left_stick_x == 0) {
                xPower = 0;
            }
            if ((xPower > gamepad1.left_stick_x) && (gamepad1.left_stick_x > 0)) {
                xPower = gamepad1.left_stick_x;
            }
            if ((xPower < gamepad1.left_stick_x) && (gamepad1.left_stick_x < 0)) {
                xPower = gamepad1.left_stick_x;
            }

            //use x and y powers to send power to wheels

            //drive
            if ((gamepad1.left_stick_x != 0) || (gamepad1.left_stick_y != 0)) {

                leftPower = -yPower - CORRECTION_FACTOR * xPower;
                rightPower = -yPower + CORRECTION_FACTOR * xPower;
                backPower = xPower;
                driveStatus = "Driving";
            }

            //rotate
            if ((gamepad1.left_stick_x == 0) && (gamepad1.left_stick_y == 0) && (gamepad1.right_stick_x != 0)) {
                leftPower = rotationFactor*-gamepad1.right_stick_x;
                rightPower = rotationFactor*gamepad1.right_stick_x;
                backPower = rotationFactor*-gamepad1.right_stick_x;
                driveStatus = "Rotating";
                //resetAngle();
            }
            if ((gamepad1.left_stick_y == 0) && (gamepad1.left_stick_x == 0) && (gamepad1.right_stick_x == 0)) {
                leftPower = 0;
                rightPower = 0;
                backPower = 0;
                driveStatus = "Stopped";
            }

            //both
            if (((gamepad1.left_stick_y != 0) || (gamepad1.left_stick_x != 0)) && (gamepad1.right_stick_x != 0)) {
                leftPower = DRIVE_FACTOR * (-yPower- .2* gamepad1.left_stick_x) + rotationFactor * -gamepad1.right_stick_x;
                rightPower = DRIVE_FACTOR * (-yPower + .2 * gamepad1.left_stick_x) + rotationFactor * gamepad1.right_stick_x;
                backPower = DRIVE_FACTOR * (xPower) + rotationFactor * -gamepad1.right_stick_x;
                driveStatus = "Driving and Rotating";
                //resetAngle();
            }

            // Send calculated power to wheels
            leftDrive.setPower(leftPower);
            rightDrive.setPower(rightPower);
            backRightDrive.setPower(backPower);
            backLeftDrive.setPower(backPower);

            //scissor controls
            //manual
            /*

            if (gamepad2.left_trigger > 0) {
                position -= 200;
                scissorPower = -gamepad2.left_trigger;
            }
            if (gamepad2.right_trigger > 0) {
                position += 200;
                scissorPower = gamepad2.right_trigger;
            }
            */

            //auto
            //height picker
            if (gamepad2.a) {
                position = 2501;
            }
            if (gamepad2.b) {
                position = 3706;
            }
            if (gamepad2.y) {
                position = 5005;
            }
            if (gamepad2.x) {
                position = 6203;
            }
            if (gamepad2.dpad_right) {
                position = 7642;
            }
            if (gamepad2.dpad_up) {
                position = 9181;
            }
            if (gamepad2.dpad_left) {
                position = 10845;
            }
            //else {
                //int currentPosition = scissorDrive1.getCurrentPosition();
               // position = currentPosition;
            //}

            //return to base
            if (gamepad2.dpad_down) {
                position = 0;
            }

            if (gamepad2.right_stick_button) {
                resetEncoders(scissorDrive1);
                resetEncoders(scissorDrive2);
            }

            //run our function on motors
            runToPosition(scissorDrive1, position);
            runToPosition(scissorDrive2, position);

            //Turret
            if(gamepad2.right_bumper) {
                turret.setMode(RUN_WITHOUT_ENCODER);
                turret.setPower(1);
            }
            else if(gamepad2.left_bumper) {
                turret.setMode(RUN_WITHOUT_ENCODER);
                turret.setPower(-1);
            }
            else {
                turret.setPower(0);
                turret.setMode(STOP_AND_RESET_ENCODER);
                turret.setTargetPosition(0);
                turret.setPower(1);
                turret.setMode(RUN_TO_POSITION);
            }

            //development tool for reseting angle
            if(gamepad1.a) {
                //resetAngle();
            }

            //beta servo controls
            //Claw
            if(gamepad2.right_stick_button && !clawToggle) {
                clawServo.setPosition(0);
                clawToggle = true;
                Thread.sleep(200);
            }
            if(gamepad2.right_stick_button && clawToggle) {
                clawServo.setPosition(1);
                clawToggle = false;
                Thread.sleep(200);
            }
            
            //Pivot
            if(gamepad2.left_stick_button && !pivotToggle) {
                pivot.setPosition(0);
                pivotToggle = true;
                Thread.sleep(200);
            }
            if(gamepad2.left_stick_button && pivotToggle) {
                pivot.setPosition(.25);
                pivotToggle = false;
                Thread.sleep(200);
            }
            armSlide.setPower(gamepad2.left_stick_y);


            //Add telemetry data
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Claw Servo Position", clawServo.getPosition());
            telemetry.addData("Motors", "left (%.2f), right (%.2f),back (%.2f)", leftPower, rightPower, backPower);
            telemetry.addData("Drive Status", driveStatus);
            telemetry.update();
        }
    }
    void resetEncoders (DcMotor dcMotor) {
        dcMotor.setMode(STOP_AND_RESET_ENCODER);
        dcMotor.setTargetPosition(0);
        dcMotor.setMode(RUN_TO_POSITION);
    }
    void runToPosition (DcMotor dcMotor, int position) {
        dcMotor.setTargetPosition(position);
        dcMotor.setPower(1);
    }
    //private void resetAngle()
   // {
        //lastAngles = gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, DEGREES);

        //globalAngle = 0;
    //}
    //private double getAngle()
    //{
        // We experimentally determined the Z axis is the axis we want to use for heading angle.
        // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

       // Orientation angles = gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, DEGREES);

        //double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        //if (deltaAngle < -180)
            //deltaAngle += 360;
        //else if (deltaAngle > 180)
            //deltaAngle -= 360;

        //globalAngle += deltaAngle;

        //lastAngles = angles;

        //return globalAngle;
    //}
}
//tobias do a back flip
//okay
/*
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0xolld0NMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkc'......;o0NMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKd;............;d0WMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKo,................'l0WMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKOd,....................'oXMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNx,.........................cKMMMMMMMMMMMMM
MMMMMWXNXKWMMMMMMMMMMMMMMMMMMMMMMMMMKl............................lXMMMMMMMMMMMM
MMMXOKkdol0MMMMMMMMMMMMMMMMMMMMMMMW0:..............................oNMMMMMMMMMMM
MMW0doo;..;dXMMMMMMMMMMMMMMMMMMMMWO;................................dNMMMMMMMMMM
MMNOdl,.....,okKNWMMMMMMMMMMMMMMM0;.................................'xWMMMMMMMMM
MMMWNKkl'.......,cok0XWMMMMMMMMMKc...................................cXMMMMMMMMM
MMMMMMMKc'cxdl,......,cokKWMMWNk:...................,l:..............'o0OkddkNMM
MMMMMMWOd0WMMWXOl'.......;OMNx;...................'oKWNOl'.................'oXMM
MMMMMMMWWMMMMMMMWKx:'.....lX0;...................cOWMMMMWk,.............':xKWMMM
MMMMMMMMMMMMMMMMMMMN0l.....c:..................,xNMMMMMMMNd'............,l0WMMMM
MMMMMMMMMMMMMMMMMMMMMXc.......................lKWMMMMMMMMMNo.............:OWMMMM
MMMMMMMMMMMMMMMMMMMMMWk'....................'dNMMMMMMMMMMMNo.....',,,;coONMMMMMM
MMMMMMMMMMMMMMMMMMMMMMXc.................,ld0WMMMMMMMMMMMMMXkxxk0KXXNNWMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMWk'...............,ckWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMO,...............oXNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMM0;...............oXNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMO,...............oKNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMXl...............lKNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMXo'.............:kKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMWO;............dXNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMM0:..........lXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMWx'.........lKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMWx...........,:oKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMWO;..............oNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMWO,...............cXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMKl..............,kWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMNk:;'.......'ok0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMWWNKxl:'':dKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNXXNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
 */
//tobias just broke his neck doing a backflip I can't believe you told him to do that cooper!