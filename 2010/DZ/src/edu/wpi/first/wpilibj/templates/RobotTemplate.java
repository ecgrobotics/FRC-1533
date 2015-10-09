/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalSource;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
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

    Jaguar leftJaguar;
    Jaguar rightJaguar;
    Joystick leftStick;
    Joystick rightStick;
    Joystick gamepad;
    Victor armVictor;
    DriverStationLCD lcd;
    Watchdog dog;
    Solenoid kickerOutLeft;//=2
    Solenoid kickerInLeft;//=1
    Solenoid kickerOutRight;//3
    Solenoid kickerInRight;//4
    Solenoid leftOut;//5
    Solenoid leftIn;//6
    Solenoid rightOut;//7
    Solenoid rightIn;//8
    Encoder encoder;
    //DigitalInput encoderTest1;
    //DigitalInput encoderTest2;
    boolean leftButtonEnabled = false;
    boolean rightButtonEnabled = false;
    boolean kickerButtonEnabled = false;
    boolean leftIsOut = false;
    boolean rightIsOut = false;
    boolean kickerIsOut = false;
    Relay compressorRelay;
    DigitalInput pressureSwitch;
    final int SOLENOID_MODULE = 8;
    final int PRESSURE_CHANNEL = 14;
    final int COMPRESSOR_CHANNEL = 1;
    final int DIGITAL_SLOT = 4;
    final boolean TANK_DRIVE = false;
    final boolean SIMPLE_DRIVE = true;

    public void robotMain() {
        leftStick = new Joystick(1);
        rightStick = new Joystick(2);
        gamepad = new Joystick(3);
        leftJaguar = new Jaguar(4, 1);
        rightJaguar = new Jaguar(4, 2);
        encoder = new Encoder(3, 4);
        encoder.start();
        //encoderTest1 = new DigitalInput(3);
        //encoderTest2 = new DigitalInput(4);
        //armVictor = new Victor(4, 3);
        lcd = DriverStationLCD.getInstance();
        dog = Watchdog.getInstance();
        leftOut = new Solenoid(SOLENOID_MODULE, 5);
        leftIn = new Solenoid(SOLENOID_MODULE, 6);
        rightOut = new Solenoid(SOLENOID_MODULE, 7);
        rightIn = new Solenoid(SOLENOID_MODULE, 8);
        kickerOutLeft = new Solenoid(SOLENOID_MODULE, 2);
        kickerOutRight = new Solenoid(SOLENOID_MODULE, 4);
        kickerInLeft = new Solenoid(SOLENOID_MODULE, 1);
        kickerInRight = new Solenoid(SOLENOID_MODULE, 3);
        pressureSwitch = new DigitalInput(14);
        compressorRelay = new Relay(DIGITAL_SLOT, COMPRESSOR_CHANNEL, Relay.Direction.kForward);
        while (true) {
            dog.feed();
            checkCompressor();
            if (isAutonomous()) {
                autonomous();
            }
            if (isOperatorControl()) {
                operatorControl();
            }
        }
    }

    public void checkCompressor() {
        if (!pressureSwitch.get()) {
            compressorRelay.set(Relay.Value.kOn);
        } else if (pressureSwitch.get()) {
            compressorRelay.set(Relay.Value.kOff);
        }//*/
        if (pressureSwitch.get()) {
            lcd.println(DriverStationLCD.Line.kUser3, 1, "ON ");
        } else {
            lcd.println(DriverStationLCD.Line.kUser3, 1, "OFF");
        }
        lcd.updateLCD();
    }

    /**
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous() {
    }

    public double absVal(double x) {
        if (x < 0) {
            return -x;
        }
        return x;
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        while (isOperatorControl()) {
            dog.feed();
            //System.out.println(encoder.getDefaultDigitalModule());
            lcd.println(DriverStationLCD.Line.kMain6, 1, "" + encoder.get());
            //lcd.println(DriverStationLCD.Line.kUser2,1,""+encoderTest1.get());
            //lcd.println(DriverStationLCD.Line.kUser4,1,""+encoderTest2.get());
            //encoderTest1.requestInterrupts();
            lcd.updateLCD();
            checkCompressor();
            if (isEnabled()) {
                if (TANK_DRIVE) {
                    double leftStickValue;
                    double rightStickValue;
                    leftStickValue = leftStick.getY();
                    rightStickValue = rightStick.getY();
                    leftJaguar.set(-leftStickValue);
                    rightJaguar.set(rightStickValue);
                } else if (SIMPLE_DRIVE) {
                    lcd.println(DriverStationLCD.Line.kUser2, 1, "Gamepad Control");
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
                }
                //control stuff
                if (gamepad.getRawButton(8) && rightButtonEnabled) {//toggle right self-righting
                    rightIsOut = !rightIsOut;
                    rightButtonEnabled = false;
                } else if (!gamepad.getRawButton(8)) {
                    rightButtonEnabled = true;
                }
                if (gamepad.getRawButton(7) && leftButtonEnabled) {//toggle right self-righting
                    leftIsOut = !leftIsOut;
                    leftButtonEnabled = false;
                } else if (!gamepad.getRawButton(7)) {
                    leftButtonEnabled = true;
                }
                if (gamepad.getRawButton(6) && kickerButtonEnabled) {//toggle kicker
                    kickerIsOut = !kickerIsOut;
                    System.out.println("toggling kicker");
                    kickerButtonEnabled = false;
                } else if (!gamepad.getRawButton(6)) {
                    kickerButtonEnabled = true;
                }
                rightOut.set(rightIsOut);
                rightIn.set(!rightIsOut);
                leftOut.set(leftIsOut);
                leftIn.set(!leftIsOut);
                kickerOutRight.set(kickerIsOut);
                kickerOutLeft.set(kickerIsOut);
                kickerInRight.set(!kickerIsOut);
                kickerInLeft.set(!kickerIsOut);
            }
        }
    }
}
