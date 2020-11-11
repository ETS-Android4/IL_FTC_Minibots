package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
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
import org.firstinspires.ftc.teamcode.Enums.WobbleTargetZone;
import org.firstinspires.ftc.teamcode.Subsystems.Drivetrain_v3;
import org.firstinspires.ftc.teamcode.Subsystems.Elevator;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Wobblegoal;

import java.util.List;

@Autonomous(name="Basic Autonomous for Test", group="Test")

//////////////////////////////////////////////////////////
    // EXTEND this class to create multiple drive paths.
    // use this for test but not competiton
        ///////////////////////////////////////////////////



public class BasicAutonomous extends LinearOpMode {
    /* Declare OpMode members. */
    public Drivetrain_v3        drivetrain  = new Drivetrain_v3(false);   // Use subsystem Drivetrain
    public Shooter              shooter     = new Shooter();
    public Intake               intake      = new Intake();
    public Wobblegoal           wobble  = new Wobblegoal();
    public Elevator             elevator    = new Elevator();

    public Orientation          lastAngles  = new Orientation();
    public ElapsedTime          PIDtimer    = new ElapsedTime(); // PID loop timer
    public ElapsedTime          drivetime     = new ElapsedTime(); // timeout timer
    public ElapsedTime          tfTime     = new ElapsedTime(); // timeout timer

    // These constants define the desired driving/control characteristics
    // The can/should be tweaked to suit the specific robot drive train.
    public static final double     DRIVE_SPEED             = 0.6;     // Nominal speed for better accuracy.
    public static final double     TURN_SPEED              = 0.50;    // 0.4 for berber carpet. Check on mat too

    public static final double     HEADING_THRESHOLD       = 2;      // As tight as we can make it with an integer gyro
    public static final double     Kp_TURN                 = 0.0275;   //0.025 to 0.0275 on mat seems to work
    public static final double     Ki_TURN                 = 0.003;   //0.0025 to 0.004 on a mat works. Battery voltage matters
    public static final double     Kd_TURN                 = 0.0;   //leave as 0
    public static final double     Kp_DRIVE                = 0.05;   //0.05 Larger is more responsive, but also less stable
    public static final double     Ki_DRIVE                = 0.005;   // 0.005 Larger is more responsive, but also less stable
    public static final double     Kd_DRIVE                = 0.0;   // Leave as 0 for now


    private double                  globalAngle; // not used currently
    public double                  lasterror;
    public  double                 totalError;

    //// Vuforia Content
    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";
    private String StackSize = "None";

    // STATE Definitions

    WobbleTargetZone Square = WobbleTargetZone.BLUE_A; // Default

    private static double tfSenseTime = 4; // needs a couple seconds to process the imagee an ID the target

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

        drivetrain.init(hardwareMap);
        wobble.init(hardwareMap);
        shooter.init(hardwareMap);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

        parameters.mode                = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled      = false;

        // Calibrate
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
        while (tfTime.time() < tfSenseTime) {
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

        switch(Square){
            case BLUE_A: // This is the basic op mode. Put real paths in designated opmodes
                telemetry.addData("Going to RED A", "Target Zone");
                gyroDrive(DRIVE_SPEED, 56.0, 0.0, 10);    // Drive FWD 110 inches
                gyroTurn(TURN_SPEED,-10,4);
                //Start Shooter
                shooter.shootoneRingHigh();
                shooter.flipperForward();
                sleep(250);
                shooter.flipperBackward();
                sleep(250);
                wobble.GripperOpen();
                wobble.ArmExtend();
                break;
            case BLUE_B:
                telemetry.addData("Going to RED B", "Target Zone");
                gyroDrive(DRIVE_SPEED, 70.0, 0.0, 5);    // Drive FWD 110 inches
                gyroTurn(TURN_SPEED,-45,3);
                gyroDrive(DRIVE_SPEED,15,-45,2);
                sleep(1000);
                wobble.GripperOpen();
                sleep(1000);
                wobble.ArmExtend();
                sleep(500);
                gyroDrive(DRIVE_SPEED,-12,-45,2);
                break;
            case BLUE_C:
                telemetry.addData("Going to RED C", "Target Zone");
                gyroDrive(DRIVE_SPEED, 115.0, 0.0, 5);    // Drive FWD 110 inches
                wobble.GripperOpen();
                wobble.ArmExtend();
                break;
                
        }



        //gyroTurn( TURN_SPEED, 90.0, 3);         // Turn  CCW to -45 Degrees
       //gyroHold( TURN_SPEED, -45.0, 0.5);    // Hold -45 Deg heading for a 1/2 second
        telemetry.addData("Path", "Complete");
        telemetry.update();
    }


    /**
     *  Method to drive on a fixed compass bearing (angle), based on encoder counts.
     *  Move will stop if either of these conditions occur:
     *  1) Move gets to the desired position
     *  2) Driver stops the opmode running.
     *  3) Timeout time is reached - prevents robot from getting stuck
     *
     * @param speed      Target speed for forward motion.  Should allow for _/- variance for adjusting heading
     * @param distance   Distance (in inches) to move from current position.  Negative distance means move backwards.
     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     */
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

    /**
     *  Method to spin on central axis to point in a new direction.
     *  Move will stop if either of these conditions occur:
     *  1) Move gets to the heading (angle)
     *  2) Driver stops the opmode running.
     *  3) Timeout time has elapsed - prevents getting stuck
     *
     * @param speed Desired speed of turn.
     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     * @param timeout max time allotted to complete each call to gyroTurn
     */
    public void gyroTurn (  double speed, double angle, double timeout) {
        totalError = 0;
        lasterror = 0;
        // keep looping while we are still active, and not on heading.
        while (opModeIsActive() && !onHeading(speed, angle, Kp_TURN, Ki_TURN, Kd_TURN) && drivetime.time() < timeout) {
            // Update telemetry & Allow time for other processes to run.
            //onHeading(speed, angle, P_TURN_COEFF);
            telemetry.update();
        }
       drivetime.reset(); // reset after we are done with the while loop
    }

    /**
     *  Method to obtain & hold a heading for a finite amount of time
     *  Move will stop once the requested time has elapsed
     *
     * @param speed      Desired speed of turn.
     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     * @param holdTime   Length of time (in seconds) to hold the specified heading.
     */
    public void gyroHold( double speed, double angle, double holdTime) {

        ElapsedTime holdTimer = new ElapsedTime();

        // keep looping while we have time remaining.
        holdTimer.reset();
        while (opModeIsActive() && (holdTimer.time() < holdTime)) {
            // Update telemetry & Allow time for other processes to run.
            onHeading(speed, angle, Kp_TURN, Ki_TURN, Kd_TURN);
            telemetry.update();
        }

        // Stop all motion;
        drivetrain.leftFront.setPower(0);
        drivetrain.rightFront.setPower(0);
    }

    /**
     * Perform one cycle of closed loop heading control.
     *
     * @param speed     Desired speed of turn.
     * @param angle     Absolute Angle (in Degrees) relative to last gyro reset.
     *                  0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                  If a relative angle is required, add/subtract from current heading.
     *
     * @return
     */
    boolean onHeading(double speed, double angle, double P_TURN_COEFF , double I_TURN_COEFF, double D_TURN_COEFF) {
        double   error ;
        double   steer ;
        boolean  onTarget = false ;
        double leftSpeed;
        double rightSpeed;

        // determine turn power based on +/- error
        error = getError(angle);

        if (Math.abs(error) <= HEADING_THRESHOLD) {
            steer = 0.0;
            leftSpeed  = 0.0;
            rightSpeed = 0.0;
            onTarget = true;
        }
        else {
            steer = getSteer(error, P_TURN_COEFF , I_TURN_COEFF, D_TURN_COEFF);
            rightSpeed  = -speed * steer;
            leftSpeed   = -rightSpeed;
        }

        // Send desired speeds to motors.
        drivetrain.leftFront.setPower(leftSpeed);
        drivetrain.rightFront.setPower(rightSpeed);

        // Display it for the driver.
        telemetry.addData("Target", "%5.2f", angle);
        telemetry.addData("Err/St", "%5.2f/%5.2f", error, steer);
        telemetry.addData("Speed.", "%5.2f:%5.2f", leftSpeed, rightSpeed);

        return onTarget;
    }

    /**
     * getError determines the error between the target angle and the robot's current heading
     * @param   targetAngle  Desired angle (relative to global reference established at last Gyro Reset).
     * @return  error angle: Degrees in the range +/- 180. Centered on the robot's frame of reference
     *          +ve error means the robot should turn LEFT (CCW) to reduce error.
     *
     */
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

    /**
     * returns desired steering force.  +/- 1 range.  +ve = steer left
     * @param error   Error angle in robot relative degrees
     * @param PCoeff  Proportional Gain Coefficient
     * @param ICoef Integration coefficient to apply to the area under the error-time curve
     * @param DCoef Derivative coefficient to apply to the area under the error-time curve
     * @return
     */
    public double getSteer(double error, double PCoeff, double ICoef, double DCoef) {
        double errorP; // combined proportional error Kp*error
        double errorI; // combined integral error Ki * cumulative error
        double errorD; // combined derivative error Kd*change in error
        double changeInError;

        changeInError = error - lasterror; // for the integral term only. Cumulative error tracking
        errorP = PCoeff * error;
        totalError = totalError  + error * PIDtimer.time();
        errorI = ICoef * totalError;
        errorD = DCoef * (changeInError)/PIDtimer.time();
        lasterror = error;
        PIDtimer.reset();

        //return Range.clip(error *(PCoeff+ICoef+DCoef), -1, 1);
        return Range.clip((errorP + errorI + errorD),-1,1);

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
}
