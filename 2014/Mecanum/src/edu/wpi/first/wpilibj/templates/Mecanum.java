/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Mecanum extends IterativeRobot {
    Watchdog dog;
    Jaguar fl, fr, bl, br;
    Joystick gamepad;
    Gyro gyro;
    boolean headingFixed;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        dog = Watchdog.getInstance();
        fl = new Jaguar(1);
        fr = new Jaguar(2);
        bl = new Jaguar(3);
        br = new Jaguar(4);
        gamepad = new Joystick(4);
        gyro = new Gyro(1);
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {

    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        dog.feed();
        double x, y, r;
        if (Math.abs(gamepad.getX()) < .05) {
            x = 0;
        } else {
            x = gamepad.getX();
        }
        if (Math.abs(gamepad.getY()) < .05) {
            y = 0;
        } else {
            y = -gamepad.getY();
        }
        if (Math.abs(gamepad.getZ()) < .05) {
            r = 0;
        } else {
            r = gamepad.getZ()/2;
        }
        if (!gamepad.getRawButton(5)) {
            x /= 2;
            y /= 2;
        }
        if (gamepad.getRawButton(6)) {
            headingFixed = true;
            double tempx = x;
            double tempy = y;
            double angle = gyro.getAngle() * Math.PI / 180;
            x = tempx * Math.cos(angle) - tempy * Math.sin(angle);
            y = tempx * Math.sin(angle) + tempy * Math.cos(angle);
        } else if (headingFixed) {
            headingFixed = false;
            gyro.reset();
        } else if (r==0) {
            if (x!=0 || y!=0) {
                r = gyro.getAngle() * -.005;
            } else {
                gyro.reset();
            }
        } else {
            gyro.reset();
        }
        
        fl.set(-(y + x - r));
        fr.set(y - x + r);
        bl.set(-(y - x - r));
        br.set(y + x + r);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
