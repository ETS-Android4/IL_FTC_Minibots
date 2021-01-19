/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Subsystems.Wobblegoal;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="Wobble Mover Test", group="Linear Opmode")
@Disabled
public class  WobbleMover_Test extends LinearOpMode {

    // Declare OpMode members.
    private Wobblegoal wobble = new Wobblegoal();



    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        wobble.init(hardwareMap);
        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).


        waitForStart();


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            //========================================
            // GAME PAD 1
            //========================================

            if (gamepad1.x) {
                wobble.wobbleWristDown();
                // wobble.resetWobble();
                sleep(500); // pause for servos to move
                telemetry.addData("Wrist Down", "Complete ");
            }

            if (gamepad1.b) {
                wobble.wobbleWristUp();
                //wobble.readyToGrabGoal();
                sleep(500);
                telemetry.addData("Wrist p", "Complete ");
            }


            //========================================
            // GAME PAD 2
            //========================================

            if (gamepad2.x) {
                wobble.GripperOpen();
               // wobble.resetWobble();
                sleep(500); // pause for servos to move
                telemetry.addData("Stowing Wobble Mover", "Complete ");
            }

            if (gamepad2.y) {
                wobble.GripperClose();
                //wobble.readyToGrabGoal();
                sleep(500);
                telemetry.addData("Gripper Close", "Complete ");
            }
            if (gamepad2.a) {
                wobble.ArmExtend();
                sleep(500);
                telemetry.addData("Arm Extend", "Complete ");
            }
            if (gamepad2.b) {
                wobble.ArmCarryWobble();
                sleep(500);
                telemetry.addData("Carry Wobble", "Complete ");
            }
            if (gamepad2.left_bumper) {
                wobble.ArmContract();
                telemetry.addData("Reset Arm", "Complete ");
            }
            if (gamepad2.left_trigger > 0.25) {
                wobble.LiftRise();
                telemetry.addData("Lifting Slide", "Complete ");
            }
            if (gamepad2.right_trigger > 0.25) {
                wobble.LiftLower();
                telemetry.addData("Lifting Slide", "Complete ");
            }
            if (gamepad1.left_trigger > 0.25) {
                wobble.lowerWobbleClamp();
                telemetry.addData("Lifting Wobble Clamp", "Complete ");
            }
            if (gamepad1.right_trigger > 0.25) {
                wobble.raiseWobbleClamp();
                telemetry.addData("Lowering Wobble Clamp", "Complete ");
            }

        }
    }
}
