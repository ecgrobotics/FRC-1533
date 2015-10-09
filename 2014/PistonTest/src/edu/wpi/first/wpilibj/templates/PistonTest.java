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
public class PistonTest extends IterativeRobot {
    DoubleSolenoid s1 = new DoubleSolenoid(1, 2);
    DoubleSolenoid s2 = new DoubleSolenoid(3, 4);
    Joystick pad = new Joystick(1);
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
        if (pad.getRawButton(4)) {
            s1.set(DoubleSolenoid.Value.kForward);
            s2.set(DoubleSolenoid.Value.kForward);
        } else {
            s1.set(DoubleSolenoid.Value.kReverse);
            s2.set(DoubleSolenoid.Value.kReverse);
        }
        if (pad.getRawButton(1)) {
            s1.set(DoubleSolenoid.Value.kForward);
        }
        if (pad.getRawButton(3)) {
            s2.set(DoubleSolenoid.Value.kForward);
        }
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
