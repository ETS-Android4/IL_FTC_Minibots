package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.Enums.ShooterState;
import org.firstinspires.ftc.teamcode.Enums.WobbleTargetZone;
import org.firstinspires.ftc.teamcode.Subsystems.Drivetrain_v3;
import org.firstinspires.ftc.teamcode.Subsystems.Elevator;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Wobblegoal;

import java.util.List;

@Autonomous(name="BLUE - Left Line Wobble and Shoot 3", group="Autonomous")
@Disabled
/////////////////////////////////////////
// Does not work yet - do not use
//////////////////////////////////////////




public class Auto_BLUE_Left_Line extends BasicAutonomous {
    /* Declare OpMode members. */
    //public Drivetrain_v3        drivetrain  = new Drivetrain_v3(false);   // Use subsystem Drivetrain
    //public Shooter              shooter     = new Shooter();
    //public Intake               intake      = new Intake();
    //public Wobblegoal           wobble      = new Wobblegoal();
    //public Elevator             elevator    = new Elevator();
    //public Orientation          lastAngles  = new Orientation();
/*
    // Timers and time limits for each timer
    public ElapsedTime          PIDtimer    = new ElapsedTime(); // PID loop timer
    public ElapsedTime          drivetime   = new ElapsedTime(); // timeout timer for driving
    public ElapsedTime          tfTime      = new ElapsedTime(); // timer for tensor flow
    public ElapsedTime          autoShootTimer  = new ElapsedTime(); //auto shooter timer (4 rings)
    private static double       autoShootTimeAllowed = 7; //  seconds allows 4 shoot cycles in case one messes up
    private static double       tfSenseTime          = 4; // needs a couple seconds to process the image and ID the target

    // These constants define the desired driving/control characteristics
    // The can/should be tweaked to suit the specific robot drive train.
    public static final double     DRIVE_SPEED             = 0.6;     // Nominal speed for better accuracy.
    public static final double     TURN_SPEED              = 0.50;    // 0.4 for berber carpet. Check on mat too

    public static final double     HEADING_THRESHOLD       = 1.5;      // As tight as we can make it with an integer gyro
    public static final double     Kp_TURN                 = 0.0275;   //0.025 to 0.0275 on mat seems to work
    public static final double     Ki_TURN                 = 0.003;   //0.0025 to 0.004 on a mat works. Battery voltage matters
    public static final double     Kd_TURN                 = 0.0;   //leave as 0
    public static final double     Kp_DRIVE                = 0.05;   //0.05 Larger is more responsive, but also less stable
    public static final double     Ki_DRIVE                = 0.005;   // 0.005 Larger is more responsive, but also less stable
    public static final double     Kd_DRIVE                = 0.0;   // Leave as 0 for now


    private double                 globalAngle; // not used currently
    // PID values for gyroDrive in order to reach target heading
    public double                  lasterror;
    public  double                 totalError;

    // STATE Definitions from the ENUM package

    ShooterState mShooterState = ShooterState.STATE_SHOOTER_OFF; // default condition
    WobbleTargetZone Square = WobbleTargetZone.BLUE_A; // Default // default target zone

    //// Vuforia Content
    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";
    private String StackSize = "None";

*/
    private static final String VUFORIA_KEY =
            "AQXVmfz/////AAABmXaLleqhDEfavwYMzTtToIEdemv1X+0FZP6tlJRbxB40Cu6uDRNRyMR8yfBOmNoCPxVsl1mBgl7GKQppEQbdNI4tZLCARFsacECZkqph4VD5nho2qFN/DmvLA0e1xwz1oHBOYOyYzc14tKxatkLD0yFP7/3/s/XobsQ+3gknx1UIZO7YXHxGwSDgoU96VAhGGx+00A2wMn2UY6SGPl+oYgsE0avmlG4A4gOsc+lck55eAKZ2PwH7DyxYAtbRf5i4Hb12s7ypFoBxfyS400tDSNOUBg393Njakzcr4YqL6PYe760ZKmu78+8X4xTAYSrqFJQHaCiHt8HcTVLNl2fPQxh0wBmLvQJ/mvVfG495ER1A";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /**
     * {@link #tfod} is the variable we will use to store our instance of the TensorFlow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;

    @Override
    public void runOpMode() {

        initVuforia();
        initTfod();
        if (tfod != null) {
            tfod.activate();

            // The TensorFlow software will scale the input images from the camera to a lower resolution.
            // This can result in lower detection accuracy at longer distances (> 55cm or 22").
            // If your target is at distance greater than 50 cm (20") you can adjust the magnification value
            // to artificially zoom in to the center of image.  For best results, the "aspectRatio" argument
            // should be set to the value of the images used to create the TensorFlow Object Detection model
            // (typically 1.78 or 16/9).

            // Uncomment the following line if you want to adjust the magnification and/or the aspect ratio of the input images.
            tfod.setZoom(2.5, 1.78);
        }

        // Call init methods in the various subsystems
        // if "null exception" occurs it is probably because the hardware init is not called below.
        drivetrain.init(hardwareMap);
        wobble.init(hardwareMap);
        shooter.init(hardwareMap);
        // intake.init(hardwareMap); not necessary in Auto at this time
        // elevator .....also not necessary

        // move implements to start position. Note, 18x18x18 inch cube has to be maintained
        // until start is pressed. Position servos and motors here so human error and set-up is not
        // as critical. Team needs to focus on robot alignment to the field.

        shooter.shooterReload(); // reload = flipper back, stacker mostly down, shooter off
        // nothing here for wobble goal yet. Gravity will take care of most of it.
        // the wobble gripper is automatically opened during the wobble init.

        // Gyro set-up
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

        parameters.mode                = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled      = false;

        // Init gyro parameters then calibrate
        drivetrain.imu.initialize(parameters);

        // Ensure the robot it stationary, then reset the encoders and calibrate the gyro.
        // Encoder rest is handled in the Drivetrain init in Drivetrain class

        // Calibrate gyro

        telemetry.addData("Mode", "calibrating...");
        telemetry.update();

        // make sure the gyro is calibrated before continuing
        while (!isStopRequested() && drivetrain.imu.isGyroCalibrated())  {
            sleep(50);
            idle();
        }

        telemetry.addData(">", "Robot Ready.");    //
        telemetry.update();

        telemetry.addData("Mode", "waiting for start");
        telemetry.addData("imu calib status", drivetrain.imu.getCalibrationStatus().toString());
        /** Wait for the game to begin */
        telemetry.addData("Square", Square);

        telemetry.update();

        /////////////////////////////////////////////////////////////////////////////////////////////
        waitForStart();
        ////////////////////////////////////////////////////////////////////////////////////////////
        tfTime.reset(); //  reset the TF timer
        while (tfTime.time() < tfSenseTime) { // need to let TF find the target so timer runs to let it do this
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    // step through the list of recognitions and display boundary info.
                    int i = 0;
                    for (Recognition recognition : updatedRecognitions) {
                        telemetry.addData(String.format("label (%d)", i), recognition.getLabel());
                        telemetry.addData(String.format("  left,top (%d)", i), "%.03f , %.03f",
                                recognition.getLeft(), recognition.getTop());
                        telemetry.addData(String.format("  right,bottom (%d)", i), "%.03f , %.03f",
                                recognition.getRight(), recognition.getBottom());
                        ///
                        StackSize = recognition.getLabel();
                        //telemetry.addData("Target", Target);
                        if (StackSize == "Quad") {
                            Square = WobbleTargetZone.BLUE_C;
                            telemetry.addData("Square", Square);
                        } else if (StackSize == "Single") {
                            Square = WobbleTargetZone.BLUE_B;
                            telemetry.addData("Square", Square);

                        }

                    }
                    telemetry.update();
                }
            }
            if (tfod != null) {
                tfod.shutdown();
            }
        }
        // Pick up the Wobble Goal before moving.
        // Sleep statements help let things settle before moving on.
        wobble.GripperOpen();
        wobble.ArmExtend();
        sleep(1000);
        wobble.GripperClose();
        sleep(500);
        wobble.ArmCarryWobble();
        sleep(500);

        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
        // Put a hold after each turn
        // This is currently set up or field coordinates NOT RELATIVE to the last move
        drivetime.reset(); // reset because time starts when TF starts and time is up before we can call gyroDrive
        // Drive paths are initially all the same to get to the shooter location
        gyroDrive(DRIVE_SPEED, 55.0, 0.0, 10);
        gyroTurn(TURN_SPEED,-10,3);
        mShooterState = ShooterState.STATE_SHOOTER_ACTIVE;
        shoot3Rings();   // call method to start shooter and launch 3 rings
        drivetime.reset(); // reset because time starts when TF starts and time is up before we can call gyroDrive


        switch(Square){
            case BLUE_A: // This is the basic op mode. Put real paths in designated opmodes
                telemetry.addData("Going to RED A", "Target Zone");
                gyroTurn(TURN_SPEED*.5,20,3);
                gyroDrive(DRIVE_SPEED, 8.0, 20.0, 5);
                sleep(1000);
                wobble.GripperOpen();
                wobble.ArmExtend();
                break;
            case BLUE_B:
                telemetry.addData("Going to RED B", "Target Zone");
                //gyroTurn(TURN_SPEED*.5,20,3);
                gyroDrive(DRIVE_SPEED, 30.0, -15.0, 5);
                sleep(1000);
                wobble.GripperOpen();
                wobble.ArmContract();
                sleep(500);
                drivetime.reset();
                gyroDrive(DRIVE_SPEED, -18.0, -15, 5);
                break;
            case BLUE_C:
                telemetry.addData("Going to RED C", "Target Zone");
                gyroTurn(TURN_SPEED,0,3);
                gyroDrive(DRIVE_SPEED, 48, 0.0, 5);
                sleep(1000);
                wobble.GripperOpen();
                wobble.ArmExtend();
                sleep(1000);
                drivetime.reset();
                gyroDrive(DRIVE_SPEED, -48.0, 0, 5);
                break;
        }



        //gyroTurn( TURN_SPEED, 90.0, 3);         // Turn  CCW to -45 Degrees
       //gyroHold( TURN_SPEED, -45.0, 0.5);    // Hold -45 Deg heading for a 1/2 second
        telemetry.addData("Path", "Complete");
        telemetry.update();
    }



    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }

    public double getError(double targetAngle) {

        double robotError;

        // calculate error in -179 to +180 range  (
        // instantiate an angles object from the IMU
        Orientation angles = drivetrain.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        // pull out the first angle which is the Z axis for heading and use to calculate the error
        // Positive robot rotation is left so positive error means robot needs to turn right.
        robotError = angles.firstAngle - targetAngle; //lastAngles.firstAngle;
        telemetry.addData("Robot Error", robotError);
        telemetry.addData("Target Angle", targetAngle);
        while (robotError > 180)  robotError -= 360;
        while (robotError <= -180) robotError += 360;
        return robotError;
    }
    public void gyroDrive ( double speed,
                            double distance,
                            double angle, double timeout) {

        int     newLeftTarget;
        int     newRightTarget;
        int     moveCounts;
        double  max;
        double  error;
        double  steer;
        double  leftSpeed;
        double  rightSpeed;
        totalError = 0;
        lasterror = 0;
        telemetry.addData("gyroDrive Activated", "Complete");
        // Ensure that the opmode is still active
        // Use timeout in case robot gets stuck in mid path.
        // Also a way to keep integral term from winding up to bad.
        if (opModeIsActive() & drivetime.time() < timeout) {

            // Determine new target position in ticks/ counts then pass to motor controller
            moveCounts = (int)(distance * Drivetrain_v3.COUNTS_PER_INCH);
            newLeftTarget = drivetrain.leftFront.getCurrentPosition() + moveCounts;
            newRightTarget = drivetrain.rightFront.getCurrentPosition() + moveCounts;

            // Set Target using the calculated umber of ticks/counts

            drivetrain.leftFront.setTargetPosition(newLeftTarget);
            drivetrain.rightFront.setTargetPosition(newRightTarget);
            // Tell motor control to use encoders to go to target tick count.

            drivetrain.leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            drivetrain.rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // start motion.
            // Up to now this is all the same as a drive by encoder opmode.
            speed = Range.clip(Math.abs(speed), 0.0, 1.0);
            drivetrain.leftFront.setPower(speed);
            drivetrain.rightFront.setPower(speed);

            // keep looping while we are still active, and BOTH motors are running.
            // once one motor gets to the target number of ticks it is no longer "busy"
            // and isbusy in false causing the loop to end.
            while (opModeIsActive() &&
                    (drivetrain.leftFront.isBusy() && drivetrain.rightFront.isBusy())) {

                // adjust relative speed based on heading error.
                // Positive angle means drifting to the left so need to steer to the
                // right to get back on track.
                error = getError(angle);
                steer = getSteer(error, Kp_DRIVE, Ki_DRIVE, Kd_DRIVE);

                // if driving in reverse, the motor correction also needs to be reversed
                if (distance < 0)
                    steer *= -1.0;

                leftSpeed = speed + steer;
                rightSpeed = speed - steer;

                // Normalize speeds if either one exceeds +/- 1.0;
                max = Math.max(Math.abs(leftSpeed), Math.abs(rightSpeed));
                if (max > 1.0)
                {
                    leftSpeed /= max;
                    rightSpeed /= max;
                }

                drivetrain.leftFront.setPower(leftSpeed);
                drivetrain.rightFront.setPower(rightSpeed);

                // Display drive status for the driver.
                telemetry.addData("Err/St",  "%5.1f/%5.1f",  error, steer);
                telemetry.addData("Target",  "%7d:%7d",      newLeftTarget,  newRightTarget);
                telemetry.addData("Actual",  "%7d:%7d",      drivetrain.leftFront.getCurrentPosition(),
                        drivetrain.rightFront.getCurrentPosition());
                telemetry.addData("Speed",   "%5.2f:%5.2f",  leftSpeed, rightSpeed);
                telemetry.update();


            }

            // Stop all motion;
            drivetrain.leftFront.setPower(0);
            drivetrain.rightFront.setPower(0);

            // Turn off RUN_TO_POSITION
            drivetrain.leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            drivetrain.rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        drivetime.reset(); // reset the timer for the next function call
    }
}
