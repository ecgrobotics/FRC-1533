/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class TrigonReincarnate extends IterativeRobot {
    Talon front=new Talon(1);
    Talon left=new Talon(2);
    Talon right=new Talon(3);
    Joystick joy1=new Joystick(1);
    Joystick joy2 = new Joystick(2);
    Talon shooter1 = new Talon(4);
    Talon shooter2 = new Talon(5);
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {

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
        double x = joy1.getX();
        double y = -joy1.getY();
        double r = joy1.getZ();
        if (Math.abs(x) < .05) x = 0;
        if (Math.abs(y) < .05) y = 0;
        if (Math.abs(r) < .05) r = 0;
        drive(x/2, y/2, r/2);
        shooter1.set(joy2.getY());
        shooter2.set(joy2.getY());
    }
    
    public void drive(double x, double y, double r) {
        front.set(x + r/2);
        left.set(-x/2 + y + r);
        right.set(-x/2 - y + r);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
