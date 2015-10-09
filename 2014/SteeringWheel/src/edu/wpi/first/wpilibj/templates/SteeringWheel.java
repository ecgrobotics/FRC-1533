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
public class SteeringWheel extends IterativeRobot {
    Jaguar fl, fr, bl, br;
    Joystick wheel;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        fl = new Jaguar(3);
        fr = new Jaguar(1);
        bl = new Jaguar(4);
        br = new Jaguar(2);
        wheel = new Joystick(1);
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
        double steer = wheel.getX();
        double accel = (1-wheel.getY())/2;
        double brake = (1-wheel.getZ())/2;
        double left = accel - brake;
        double right = left;
        double slide; //x axis movement
        if (steer > 0) {
            right *= 1 - 1.65*steer;
        } else if (steer < 0) {
            left *= 1 + 1.65*steer;
        }
        if(wheel.getRawButton(1)){
            slide = -.5;
        }
        else if(wheel.getRawButton(2)){
            slide = .5;
        }
        else {slide = 0;}
        
//        fl.set(-y + x + rot);
//        fr.set(y + x + rot);
//        bl.set(-y - x + rot);
//        br.set(y - x + rot);
        
        fl.set(left-slide);
        bl.set(left+slide);
        fr.set(-right-slide);
        br.set(-right+slide);
        
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}