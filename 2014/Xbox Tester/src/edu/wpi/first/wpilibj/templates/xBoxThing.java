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
public class xBoxThing extends IterativeRobot {
    Joystick joy1;
    Jaguar fr1;
    Jaguar fl2;
    Jaguar br3;
    Jaguar bl4;
    Watchdog dog;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
joy1 = new Joystick(1);
fr1 = new Jaguar(1);
fl2 = new Jaguar(2);
br3 = new Jaguar(3);
bl4 = new Jaguar(4);
dog = Watchdog.getInstance();
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
        
        fl2.set(joy1.getRawAxis(2));
        fr1.set(-joy1.getRawAxis(4));
        
      //  System.out.println(joy1.getRawAxis(1) +",  "+ joy1.getRawAxis(2) +",  "+ joy1.getRawAxis(3) +",  "+ joy1.getRawAxis(4) +",  "+ joy1.getRawAxis(5));
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
