package org.firstinspires.ftc.teamcode.Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Subsystems.Drivetrain_v3;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Wobblegoal;
import org.firstinspires.ftc.teamcode.Enums.DriveSpeedState;

@TeleOp(name="Meet 1 Teleop", group="Teleop")
//@Disabled
public class Meet_1_Teleop extends OpMode {


    /* Declare OpMode members. */

    private ElapsedTime runtime     = new ElapsedTime();
    Drivetrain_v3       drivetrain        = new Drivetrain_v3(true);
    //Shooter shooter                 = new Shooter();
    Intake intake                   = new Intake();
    Wobblegoal wobble               = new Wobblegoal();


    private DriveSpeedState  currDriveState;


    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
   public void init() {

        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */
        drivetrain.init(hardwareMap);
        //leftFront = hardwareMap.get(DcMotor.class, "Left_front");
        //rightFront = hardwareMap.get(DcMotor.class, "Right_front");

        // For HD Planetary Forward yields CCW rotation when shaft is facing you.

        //shooter.init(hardwareMap);
        intake.init(hardwareMap);
        wobble.init(hardwareMap);

        //newState(currDriveState);
        currDriveState = DriveSpeedState.DRIVE_FAST; // initialize robot to FAST

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Hello Driver");

        telemetry.addData("FAST DRIVE","Mode");//
        //telemetry.update();
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {


    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {

    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        double left;
        double right;
        double drive;
        double turn;
        double max;
        double speedfactor = 0.5;


        // Ultimate Goal
        //Gamepad 1
        //  Drivetrain (left/right sticks)
        //  Intake on
        //  Intake off
        //  Intake reverse
        //  Wobble (at least 4 function)

        // Gamepad 2
        // Shooter slow (mid goal)
        // Shooter fast (high goal)
        // Shooter reverse
        // stacker uo
        // stacker down
        // flipper in
        // flipper out


        //========================================
        // GAME PAD 1
        //========================================
        // left joystick is assigned to drive speed
        drive = -gamepad1.left_stick_y;
        // right joystick is for turning
        turn  =  gamepad1.right_stick_x;
        // Combine drive and turn for blended motion.
        left  = drive + turn;
        right = drive - turn;

        // Normalize the values so neither exceed +/- 1.0
        max = Math.max(Math.abs(left), Math.abs(right));
        if (max > 1.0)
        {
            left /= max; // does this to stay within the limit and keeps the ratio the same
            right /= max;
        }

       // Gamepad 1 Buttons
        if (gamepad1.a) {
        }
        if (gamepad1.b) {
        }
        if (gamepad1.x) {
        }
        if (gamepad1.y) {
        }
        // Gamepad 1 Bumpers - for Speed Control
        // set-up drive speed states on bumpers
        if (gamepad1.left_bumper)
        {
            currDriveState = DriveSpeedState.DRIVE_FAST;
        }
        if (gamepad1.right_bumper)
        {
            currDriveState =  DriveSpeedState.DRIVE_SLOW;
        }


        //========================================
        // GAME PAD 2
        //========================================

        if (gamepad2.a) {
        }
        if (gamepad2.b) {
        }
        if (gamepad2.x) {
        }
        if (gamepad2.y) {
        }
        // Gamepad 2 Bumpers

        if (gamepad2.left_bumper)
        {

        }
        if (gamepad2.right_bumper)
        {

        }


       // switch case to determine what mode the arm needs to operate in.


        // switch case for the drive speed state

        switch(currDriveState) {

            case DRIVE_FAST:
                telemetry.addData("Drive Speed",currDriveState);
                drivetrain.leftFront.setPower(left);
                drivetrain.rightFront.setPower(right);
                //leftFront.setPower(left);
                //rightFront.setPower(right);

                // Send telemetry message to signify robot running;
                telemetry.addData("left",  "%.2f", left);
                telemetry.addData("right", "%.2f", right);
                break;

            case DRIVE_SLOW:
                telemetry.addData("Drive Speed",currDriveState);
                drivetrain.leftFront.setPower(left*speedfactor);
                drivetrain.rightFront.setPower(right*speedfactor);
                //leftFront.setPower(left*speedfactor);
                //rightFront.setPower(right*speedfactor);

                // Send telemetry message to signify robot running;
                telemetry.addData("left",  "%.2f", left);
                telemetry.addData("right", "%.2f", right);
                break;
        }


    }
    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }
    //===================================================================
    // Helper Methods
    //==================================================================

}
