/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Watchdog;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends SimpleRobot {
//NATHAN, I changed AutonomousStateToString() to a case switch statment

    Jaguar leftJaguar;
    Jaguar rightJaguar;
    Joystick leftStick;
    Joystick rightStick;
    Joystick gamepad;
    Victor armVictor;
    DriverStation driverStation;
    DriverStationLCD lcd;
    Watchdog dog;
    DigitalInput rightLineTracker;
    DigitalInput leftLineTracker;
    Solenoid clampSolenoidIn;
    Solenoid clampSolenoidOut;
    Relay compressorRelay;
    Servo servo;
    //AnalogChannel gyro;
    Gyro gyro;
    DigitalInput pressureSwitch;
    final int SOLENOID_MODULE = 8;
    final int ARM_UP_BUTTON = 6;
    final int ARM_DOWN_BUTTON = 5;
    final int TANK_ARM_UP_BT = 3;
    final int TANK_ARM_DOWN_BT = 3;
    final int TANK_CLAMP_IN_BT = 2;
    final int TANK_CLAMP_OUT_BT = 2;
    final int CLAMP_IN_BUTTON = 7;
    final int CLAMP_OUT_BUTTON = 8;
    final int PRESSURE_CHANNEL = 14;
    final int COMPRESSOR_CHANNEL = 1;
    final int DIGITAL_SLOT = 4;
    boolean wasOn = false;
    boolean SINGLE_JOYSTICK = false;
    boolean TANK_DRIVE = false;
    boolean SIMPLE_DRIVE = false;
    boolean NICK_SYSTEM = false;
    boolean TYLER_SYSTEM = false;
    boolean SIMPLE_DRIVE_JOYSTICK = false;
    boolean clampIsOut = false;
    boolean clampButtonEnabled = true;
    //AUTONOMOUS MODES
    //final int STRAIGHT = 0;
    //final int TURN_RIGHT = 1;
    //final int TURN_LEFT = 2;
    //final int OUTSIDE = 3;
    //int autonomousMode = OUTSIDE;
    //AUTONOMOUS STATES
    //final int DRIVE1 = 0;//straight before turn
    final int STOPPED = 1;
    //final int TURNING_LEFT = 2;
    //final int TURNING_RIGHT = 3;
    //final int DRIVE2 = 4;//straight before stop
    //final int SPIN_LEFT = 5;
    //final int SPIN_STRAIGHT = 6;
    //final int SPIN_RIGHT = 7;
    //final int LAST_STRAIGHT = 8;//last straight run of outside drive
    final int LINE_FOLLOW_GYRO = 9;
    final int LINE_FOLLOW_RIGHT = 10;
    final int LINE_FOLLOW_LEFT = 11;
    final int LINE_FOLLOW = 12;
    final int TURN_TO_ANGLE = 13;
    int autonomousState = STOPPED;
    int autonomousStartLocation = 0;//0-2
    int autonomousEndLocation = 1;//0-5
    int autonomousProgress = 0;//used to store autonomous progress
    double autonomousAngle = 0;//angle to turn towards while following line
    int driveSelect = 1;
    //autonomous variables
    long endWaitTime;
    //end autonomous variables
    boolean autonomousInitialized = false;
    boolean teleopInitialized = false;
    double motorCalibration = 0.03;//positive value makes it turn right

    public void robotMain() {
        gyro = new Gyro(1);
        gyro.setSensitivity(0.00736);
        //<0.0074
        //>0.007
        //gyro=new AnalogChannel(6);
        driverStation = DriverStation.getInstance();
        leftStick = new Joystick(1);
        rightStick = new Joystick(2);
        gamepad = new Joystick(3);
        leftJaguar = new Jaguar(4, 2);
        rightJaguar = new Jaguar(4, 1);
        armVictor = new Victor(4, 3);
        servo = new Servo(4);
        leftLineTracker = new DigitalInput(12);
        rightLineTracker = new DigitalInput(13);
        lcd = DriverStationLCD.getInstance();
        dog = Watchdog.getInstance();
        clampSolenoidIn = new Solenoid(SOLENOID_MODULE, 1);
        clampSolenoidOut = new Solenoid(SOLENOID_MODULE, 2);
        pressureSwitch = new DigitalInput(14);
        compressorRelay = new Relay(DIGITAL_SLOT, COMPRESSOR_CHANNEL, Relay.Direction.kForward);
        while (true) {
            dog.feed();
            checkCompressor();
            if (isAutonomous()) {
                teleopInitialized = false;
                autonomous();
            }
            if (isOperatorControl()) {
                autonomousInitialized = false;
                operatorControl();
            }
        }
    }

    public void initializeAutonomous() {
        autonomousStartLocation = (int) (driverStation.getAnalogIn(1));
        autonomousEndLocation = (int) (driverStation.getAnalogIn(2));
        if (autonomousStartLocation == 0 && autonomousEndLocation == 1) {
            autonomousState = LINE_FOLLOW_GYRO;
        } else if (autonomousStartLocation == 1) {
            autonomousState = LINE_FOLLOW;
        } else if (autonomousStartLocation == 2 && autonomousEndLocation == 4) {
            autonomousState = LINE_FOLLOW_GYRO;
        } else {
            autonomousState = STOPPED;
        }
        gyro.reset();
        autonomousInitialized = true;
        autonomousProgress = 0;
        autonomousAngle = 0;//temporary
        //autonomousState=TURN_TO_ANGLE;//temporary
    }

    public void initializeTeleop() {
        teleopInitialized = true;
    }

    public String autonomousStateToString(int state) {
        switch (state) {
            case STOPPED:
                return "STOPPED";
            //case TURNING_LEFT: return "TURNING_LEFT";
            //case TURNING_RIGHT: return "TURNING_RIGHT";
            case LINE_FOLLOW_GYRO:
                return "LINE_FOLLOW_GYRO";
            case LINE_FOLLOW_RIGHT:
                return "LINE_FOLLOW_RIGHT";
            case LINE_FOLLOW_LEFT:
                return "LINE_FOLLOW_LEFT";
            case LINE_FOLLOW:
                return "LINE_FOLLOW";
            case TURN_TO_ANGLE:
                return "TURN_TO_ANGLE";
            default:
                return "UNKNOWN";
        }
        /*if (state == STOPPED) {
        return "STOPPED";
        } else if (state == TURNING_LEFT) {
        return "TURNING_LEFT";
        } else if (state == TURNING_RIGHT) {
        return "TURNING_RIGHT";
        } else if (state == LINE_FOLLOW_GYRO) {
        return "LINE_FOLLOW_GYRO";
        } else if (state == LINE_FOLLOW_RIGHT) {
        return "LINE_FOLLOW_RIGHT";
        } else if (state == LINE_FOLLOW_LEFT) {
        return "LINE_FOLLOW_LEFT";
        } else if (state == LINE_FOLLOW) {
        return "LINE_FOLLOW";
        } else if (state == TURN_TO_ANGLE) {
        return "TURN_TO_ANGLE";
        } else {
        return "UNKNOWN";
        }*/
    }

    public void checkCompressor() {
        //compressorRelay.set(Relay.Value.kOn);
        if (!pressureSwitch.get()) {
            compressorRelay.set(Relay.Value.kOn);
        } else if (pressureSwitch.get()) {
            compressorRelay.set(Relay.Value.kOff);
        }//*/
        if (pressureSwitch.get()) {
            wasOn = true;
            System.out.println("ON");
             //lcd.println(DriverStationLCD.Line.kUser3, 1, "ON ");
        } else {
            System.out.println("OFF");
           // lcd.println(DriverStationLCD.Line.kUser3, 1, "OFF");
        }
        // lcd.updateLCD();
        // System.out.println(wasOn);
    }

    public void displayTrackingSensors() {
        if (leftLineTracker.get()) {
            lcd.println(DriverStationLCD.Line.kUser2, 1, "ON ");
        } else {
            lcd.println(DriverStationLCD.Line.kUser2, 1, "OFF");
        }
        if (rightLineTracker.get()) {
            lcd.println(DriverStationLCD.Line.kUser3, 1, "ON ");
        } else {
            lcd.println(DriverStationLCD.Line.kUser3, 1, "OFF");
        }
        lcd.updateLCD();
    }

    public void autonomous() {
        dog.feed();
        //System.out.println("auto called");
        lcd.println(DriverStationLCD.Line.kUser6, 1, "                    ");
        lcd.println(DriverStationLCD.Line.kUser6, 1, autonomousStateToString(autonomousState));
        lcd.updateLCD();
        if (isEnabled()) {
            if (!autonomousInitialized) {
                initializeAutonomous();
            }
            //System.out.println("auto running");
            displayTrackingSensors();
            System.out.println(autonomousState);
            if (autonomousState == LINE_FOLLOW_GYRO) {
                double angle = gyro.getAngle() - autonomousAngle;
                double max_speed;
                double low_speed;
                if (autonomousStartLocation == 1) {
                    max_speed = 0.4;
                    low_speed = 0.0;
                } else {
                    max_speed = 0.7;
                    low_speed = 0.0;
                }
                if (leftLineTracker.get()) {//no line detected
                    leftJaguar.set(max_speed - (angle / 80) + motorCalibration);
                } else {//line detected
                    leftJaguar.set(low_speed);
                }
                if (rightLineTracker.get()) {//no line detected
                    rightJaguar.set(-(max_speed + (angle / 80) - motorCalibration));
                } else {//line detected
                    rightJaguar.set(-(low_speed));
                }
                if (!leftLineTracker.get() && !rightLineTracker.get()) {//both detect line
                    if (autonomousStartLocation == 1) {
                        if (autonomousProgress == 0) {
                            if (autonomousEndLocation == 3) {
                                autonomousState = LINE_FOLLOW_LEFT;
                                endWaitTime = System.currentTimeMillis() + 2000;
                            } else {
                                autonomousState = LINE_FOLLOW_RIGHT;
                                endWaitTime = System.currentTimeMillis() + 2000;
                            }
                        } else {
                            autonomousState = STOPPED;
                        }
                    } else {
                        autonomousState = STOPPED;
                    }
                }
            } else if (autonomousState == LINE_FOLLOW) {
                double max_speed;
                double low_speed;
                max_speed = 0.4;
                low_speed = 0.0;
                if (leftLineTracker.get()) {//no line detected
                    leftJaguar.set(max_speed + motorCalibration);
                } else {//line detected
                    leftJaguar.set(low_speed);
                }
                if (rightLineTracker.get()) {//no line detected
                    rightJaguar.set(-(max_speed - motorCalibration));
                } else {//line detected
                    rightJaguar.set(-(low_speed));
                }
                if (!leftLineTracker.get() && !rightLineTracker.get()) {//both detect line
                    if (autonomousStartLocation == 1) {//started in middle lane
                        if (autonomousProgress == 0) {//first straight drive
                            if (autonomousEndLocation == 3) {//left turn from middle
                                autonomousState = LINE_FOLLOW_LEFT;
                                endWaitTime = System.currentTimeMillis() + 2000;
                            } else {//right turn from middle
                                autonomousState = LINE_FOLLOW_RIGHT;
                                endWaitTime = System.currentTimeMillis() + 2000;
                            }
                        } else {//2nd straight drive (after branch in line)
                            autonomousState = TURN_TO_ANGLE;
                            autonomousAngle = 0;//turn to 0
                        }
                    } else {//started in one of the edge lanes
                        autonomousState = STOPPED;
                    }
                }
            } else if (autonomousState == STOPPED) {
                leftJaguar.set(0);
                rightJaguar.set(0);
            } else if (autonomousState == LINE_FOLLOW_LEFT) {
                //only left sensor is active
                if (leftLineTracker.get()) {//no line detected
                    leftJaguar.set(0.5 + motorCalibration);
                } else {//line detected
                    leftJaguar.set(0.0);
                }
                rightJaguar.set(-(0.5 - motorCalibration));
                if (System.currentTimeMillis() > endWaitTime) {
                    autonomousState = LINE_FOLLOW;
                    autonomousProgress = 1;
                }
            } else if (autonomousState == LINE_FOLLOW_RIGHT) {
                //only left sensor is active
                if (rightLineTracker.get()) {//no line detected
                    rightJaguar.set(-(0.5 - motorCalibration));
                } else {//line detected
                    rightJaguar.set(0.0);
                }
                leftJaguar.set(0.5 + motorCalibration);
                if (System.currentTimeMillis() > endWaitTime) {
                    autonomousState = LINE_FOLLOW;
                    autonomousProgress = 1;
                }
            } else if (autonomousState == TURN_TO_ANGLE) {
                double angle = gyro.getAngle() - autonomousAngle;
                double absAngle = Math.abs(angle);
                double speed;
                if (absAngle > 40) {//TODO: numbers need to be calibrated on new robot (should be close)
                    speed = 0.5;
                } else {
                    speed = 0.3;
                }
                if (angle > 0) {
                    speed = -speed;
                }
                leftJaguar.set(speed);
                rightJaguar.set(speed);
                if (absAngle < 3.5) {//TODO: number needs to be calibrated on new robot (should be close)
                    autonomousState = STOPPED;
                }
            }


        }
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public double absVal(double x) {
        if (x < 0) {
            return -x;
        }
        return x;
    }

    public void operatorControl() {
        TANK_DRIVE = false;
        SIMPLE_DRIVE = false;
        SINGLE_JOYSTICK = false;
        SIMPLE_DRIVE_JOYSTICK = false;
        driveSelect = (int) (driverStation.getAnalogIn(3));
        if (driveSelect == 0) {
            TANK_DRIVE = true;
        } else if (driveSelect == 1) {
            SINGLE_JOYSTICK = true;
        } else if (driveSelect == 2) {
            SIMPLE_DRIVE = true;
        } else if (driveSelect == 3) {
            SIMPLE_DRIVE_JOYSTICK = true;
        }
        dog.feed();
        checkCompressor();
        if (isEnabled()) {
            if (!teleopInitialized) {
                initializeTeleop();
            }
            if (TANK_DRIVE) {
                System.out.println("tank drive");
                double leftStickValue;
                double rightStickValue;
                leftStickValue = leftStick.getY();
                rightStickValue = rightStick.getY();
                leftJaguar.set(-leftStickValue);
                rightJaguar.set(rightStickValue);
                if (leftStick.getRawButton(TANK_ARM_DOWN_BT)) {
                    armVictor.set(-0.5);
                } else if (rightStick.getRawButton(TANK_ARM_UP_BT)) {
                    armVictor.set(0.5);
                } else {
                    armVictor.set(0);
                }
                if (leftStick.getRawButton(TANK_CLAMP_OUT_BT)) {
                    clampSolenoidOut.set(true);
                    clampSolenoidIn.set(false);
                }
                if (rightStick.getRawButton(TANK_CLAMP_IN_BT)) {
                    clampSolenoidOut.set(false);
                    clampSolenoidIn.set(true);
                }
                //if (gamepad.getRawButton(ARM_UP_BUTTON)) {//arm up
                //  armVictor.set(0.5);
                //} else if (gamepad.getRawButton(ARM_DOWN_BUTTON)) {//arm down
                //  armVictor.set(-0.5);
                //} else {//stop arm
                //  armVictor.set(0);
                //}
                //if (gamepad.getRawButton(CLAMP_IN_BUTTON)) {
                //  clampSolenoidIn.set(true);
                //  clampSolenoidOut.set(false);
                //} else if (gamepad.getRawButton(CLAMP_OUT_BUTTON)) {
                //  clampSolenoidIn.set(false);
                //  clampSolenoidOut.set(true);
                //}
            } else if (SINGLE_JOYSTICK) {
                System.out.println("single joystick");
                double speed;
                double turning;
                double spd1;
                boolean straight;
                if (rightStick.getRawButton(2)) {
                    straight = true;
                } else straight = false;
                if (straight == true) {
                    spd1 = rightStick.getY();
                    leftJaguar.set(-spd1+.02);
                    rightJaguar.set(spd1-.03);
                } else {
                speed = rightStick.getY();//leftStick.getY();
                turning = rightStick.getX();//rightStick.getY();
                double left = speed - turning;
                double right = speed + turning;
                double max = Math.max(absVal(left), absVal(right));
                if (max > 1) {
                    left /= max;
                    right /= max;
                }
                leftJaguar.set(-left);
                rightJaguar.set(right);
                }
                if (rightStick.getRawButton(1)) {
                    gyro.reset();
                }
                if (rightStick.getRawButton(4)) {
                    armVictor.set(-0.5);
                } else if (rightStick.getRawButton(5)) {
                    armVictor.set(0.5);
                } else {
                    armVictor.set(0);
                } if (rightStick.getRawButton(3) && clampButtonEnabled) {//toggle kicker
                    clampIsOut = !clampIsOut;
                    System.out.println("clamp");
                    clampButtonEnabled = false;
                } else if (!rightStick.getRawButton(3)) {
                    clampButtonEnabled = true;
                }
                if (clampIsOut) {
                    clampSolenoidIn.set(true);
                    clampSolenoidOut.set(false);
                } else{
                    clampSolenoidIn.set(false);
                    clampSolenoidOut.set(true);
                }

            } else if (SIMPLE_DRIVE) {
               // System.out.println("simple drive");
                double speed;
                double turning;
                speed = gamepad.getY();//leftStick.getY();
                turning = gamepad.getZ();//rightStick.getY();
                double left = speed - turning;
                double right = speed + turning;
                double max = Math.max(absVal(left), absVal(right));
                if (max > 1) {
                    left /= max;
                    right /= max;
                }
                leftJaguar.set(-left);
                rightJaguar.set(right);
                if (gamepad.getRawButton(1)) {
                    gyro.reset();
                }
                if (gamepad.getRawButton(ARM_UP_BUTTON)) {//arm up
                    armVictor.set(0.5);
                } else if (gamepad.getRawButton(ARM_DOWN_BUTTON)) {//arm down
                    armVictor.set(-0.5);
                } else {//stop arm
                    armVictor.set(0);
                }
                if (gamepad.getRawButton(CLAMP_IN_BUTTON)) {
                    clampSolenoidIn.set(true);
                    clampSolenoidOut.set(false);
                } else if (gamepad.getRawButton(CLAMP_OUT_BUTTON)) {
                    clampSolenoidIn.set(false);
                    clampSolenoidOut.set(true);
                }
            } else if (SIMPLE_DRIVE_JOYSTICK) {
                //System.out.println("simple drive joystick");
                double speed;
                double turning;
                speed = leftStick.getY();//leftStick.getY();
                turning = rightStick.getX();//rightStick.getY();
                double left = speed - turning;
                double right = speed + turning;
                double max = Math.max(absVal(left), absVal(right));
                if (max > 1) {
                    left /= max;
                    right /= max;
                }
                leftJaguar.set(-left);
                rightJaguar.set(right);
                if (gamepad.getRawButton(1)) {
                    gyro.reset();
                }
                if (rightStick.getRawButton(3)) {//arm up
                    armVictor.set(0.5);
                } else if (rightStick.getRawButton(2)) {//arm down
                    armVictor.set(-0.5);
                } else {//stop arm
                    armVictor.set(0);
                }
                if (rightStick.getRawButton(1) && clampButtonEnabled) {//toggle kicker
                    clampIsOut = !clampIsOut;
                    System.out.println("toggling clamp");
                    clampButtonEnabled = false;
                } else if (!rightStick.getRawButton(1)) {
                    clampButtonEnabled = true;
                }
                if (clampIsOut) {
                    clampSolenoidIn.set(true);
                    clampSolenoidOut.set(false);
                } else{
                    clampSolenoidIn.set(false);
                    clampSolenoidOut.set(true);
                }
            } else if (NICK_SYSTEM) {
                double speed;
                double turning;
                speed = gamepad.getY();//leftStick.getY();
                turning = gamepad.getX();//rightStick.getY();
                double left = speed - turning;
                double right = speed + turning;
                double max = Math.max(absVal(left), absVal(right));
                if (max > 1) {
                    left /= max;
                    right /= max;
                }
                leftJaguar.set(-left);
                rightJaguar.set(right);
                armVictor.set(gamepad.getZ());
                if (gamepad.getRawButton(CLAMP_IN_BUTTON)) {
                    clampSolenoidIn.set(true);
                    clampSolenoidOut.set(false);
                } else if (gamepad.getRawButton(CLAMP_OUT_BUTTON)) {
                    clampSolenoidIn.set(false);
                    clampSolenoidOut.set(true);
                }
            }//END NICK SYSTEM
            else if (TYLER_SYSTEM) {
                double speed;
                double turning;
                speed = gamepad.getY();//leftStick.getY();
                turning = gamepad.getZ();//rightStick.getY();
                double left = speed - turning;
                double right = speed + turning;
                double max = Math.max(absVal(left), absVal(right));
                if (max > 1) {
                    left /= max;
                    right /= max;
                }
                leftJaguar.set(-left);
                rightJaguar.set(right);
                if (gamepad.getRawButton(ARM_UP_BUTTON)) {//arm up
                    armVictor.set(0.5);
                } else if (gamepad.getRawButton(ARM_DOWN_BUTTON)) {//arm down
                    armVictor.set(-0.5);
                } else {//stop arm
                    armVictor.set(0);
                }
                if (gamepad.getRawButton(1)) {
                    clampSolenoidIn.set(true);
                    clampSolenoidOut.set(false);
                } else if (gamepad.getRawButton(2)) {
                    clampSolenoidIn.set(false);
                    clampSolenoidOut.set(true);
                }
            }

            lcd.println(DriverStationLCD.Line.kMain6, 1, "Gyro:" + gyro.getAngle());
            lcd.updateLCD();
        }

    }
}
