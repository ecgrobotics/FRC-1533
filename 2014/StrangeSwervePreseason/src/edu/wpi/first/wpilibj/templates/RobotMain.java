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
public class RobotMain extends IterativeRobot {
    SwerveDrive swerve;
    Joystick joy;
    Gyro gyro;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        gyro = new Gyro(1);
        swerve = new SwerveDrive(60, 48, gyro);
        joy = new Joystick(1);
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
        double x, y, r;
        x = joy.getX();
        y = -joy.getY();
        r = joy.getZ();
        if (Math.abs(x) < .05) x = 0;
        if (Math.abs(y) < .05) y = 0;
        if (Math.abs(r) < .05) r = 0;
        if (joy.getRawButton(6)) swerve.carefreeDrive(x/2, y/2);
        else swerve.rateDrive(x/2, y/2);
        swerve.rateDrive(r/2);
        if (joy.getRawButton(2)) swerve.brake();
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
