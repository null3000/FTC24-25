package org.firstinspires.ftc.teamcode.teleOp;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.usb.serial.RobotUsbDeviceTty;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.Gamepad;


/*
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@TeleOp(name="drive", group="Linear OpMode")
public class drive extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor intakeMotor = null;
    private DcMotor outakeMotor1 = null;
    private DcMotor outakeMotor2 = null;

    private Servo outakeServo = null;

    private DcMotor leftFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor rightBackDrive = null;
    private CRServo intakeServo = null;

    private Servo intakeServo2 = null;



    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        intakeServo = hardwareMap.get(CRServo.class, "intakeServo");
        intakeServo2 = hardwareMap.get(Servo.class, "intakeServo2");


        outakeMotor1 = hardwareMap.get(DcMotor.class, "outakeMotor1");
        outakeMotor2 = hardwareMap.get(DcMotor.class, "outakeMotor2");
        outakeServo = hardwareMap.get(Servo.class, "outakeServo");
        outakeServo.setPosition(.65);

        leftFrontDrive  = hardwareMap.get(DcMotor.class, "lf");
        leftBackDrive  = hardwareMap.get(DcMotor.class, "lb");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rf");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rb");




        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);
        intakeMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intakeMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);



        outakeMotor1.setDirection(DcMotor.Direction.FORWARD);
        outakeMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        outakeMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outakeMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        outakeMotor2.setDirection(DcMotor.Direction.REVERSE);
        outakeMotor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        outakeMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

//        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
//        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
//        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
//        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);


        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();



        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            drive(gamepad1);
            intake(gamepad2);
            outake(gamepad2);
            telemetry.update();
        }

    }

    public void drive(Gamepad gp){
        double max;
        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        double leftFrontPower  = axial + lateral + yaw;
        double rightFrontPower = axial - lateral - yaw;
        double leftBackPower   = axial - lateral + yaw;
        double rightBackPower  = axial + lateral - yaw;

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower  /= max;
            rightFrontPower /= max;
            leftBackPower   /= max;
            rightBackPower  /= max;
        }

        boolean speedModeActive = gp.right_bumper;
        double speedModifier;
        if (speedModeActive) {
            speedModifier = 1;
        } else {
            speedModifier = .75;
        }

        // This is test code:
        //
        // Uncomment the following code to test your motor directions.
        // Each button should make the corresponding motor run FORWARD.
        //   1) First get all the motors to take to correct positions on the robot
        //      by adjusting your Robot Configuration if necessary.
        //   2) Then make sure they run in the correct direction by modifying the
        //      the setDirection() calls above.
        // Once the correct motors move in the correct direction re-comment this code.

            /*
            leftFrontPower  = gamepad1.x ? 1.0 : 0.0;  // X gamepad
            leftBackPower   = gamepad1.a ? 1.0 : 0.0;  // A gamepad
            rightFrontPower = gamepad1.y ? 1.0 : 0.0;  // Y gamepad
            rightBackPower  = gamepad1.b ? 1.0 : 0.0;  // B gamepad
            */

        // Send calculated power to wheels
        leftFrontDrive.setPower(leftFrontPower * speedModifier);
        rightFrontDrive.setPower(rightFrontPower * speedModifier);
        leftBackDrive.setPower(leftBackPower * speedModifier);
        rightBackDrive.setPower(rightBackPower * speedModifier);
        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.addData("Front left/Right", "%4.2f, %4.2f", leftFrontPower, rightFrontPower);
        telemetry.addData("Back  left/Right", "%4.2f, %4.2f", leftBackPower, rightBackPower);
        telemetry.addData("Stick", "x (%.2f), y (%.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
    }

    public void intake(Gamepad gp){
        double power = 0;
        double position = intakeMotor.getCurrentPosition();
        // Send calculated power to wheels
        //Max is 4200m min is 0

//            3000 to be incredibly conservative, 4200 is a more realistic limit but we never have to extend that far anyways
        int intakeMaxPosition = 1500;
//            100 is a very good limit, limit of zero causes "jitters" on the way back down quickly
        int intakeMinPosition = 75;

        if(position >= intakeMaxPosition){
            power = Range.clip(-gp.left_stick_y, -1.0, 0);
        } else if(position <= intakeMinPosition){
            power = Range.clip(-gp.left_stick_y, 0, 1.0);
        } else{
            power = Range.clip(-gp.left_stick_y, -1.0, 1.0);
        }
        intakeMotor.setPower(power);
        if(gp.x){
            intakeServo.setPower(.75);
        } else if(gp.y){
            intakeServo.setPower(-.75);
        } else{
            intakeServo.setPower(0);
        }

        if (gp.dpad_left){
            intakeServo2.setPosition(.2);
        } else if (gp.dpad_up){
//            center
            intakeServo2.setPosition(.3);
        } else if (gp.dpad_down) {
//            down
            intakeServo2.setPosition(.5);
        } else if (gp.dpad_right) {
            intakeServo2.setPosition(.4);

        }


        // Show the elapsed game time and wheel power.
        telemetry.addData("Motors", "power (%.2f)", power);
        telemetry.addData("Motors", "position (%.2f)", position);
        telemetry.addData("CRServo", "power (%.2f)", intakeServo.getPower());
    }
    public void outake(Gamepad gp) {
        double power = 0;
        power = Range.clip(gp.right_stick_y, -1.0, 1.0);

        double position = outakeMotor1.getCurrentPosition();
        int outakeMinPosition = 75;
        int outakeMaxPosition = 5700;

//honestly this doesnt make sense, (its in reverse?) but it works so dont touch
        if(position >= outakeMaxPosition){
            power = Range.clip(gp.right_stick_y, 0, 1);
        } else if(position <= outakeMinPosition){
            power = Range.clip(gp.right_stick_y, -1, 0);
        } else{
            power = Range.clip(gp.right_stick_y, -1.0, 1.0);
        }
//

        outakeMotor1.setPower(power);
        outakeMotor2.setPower(power);

        if(gp.a){
            outakeServo.setPosition(.7);
        } else if(gp.b){
            outakeServo.setPosition(.25);
        } else if (gp.right_bumper) {
            outakeServo.setPosition(.75);
        }

        telemetry.addData("position", position);
        
    }
}