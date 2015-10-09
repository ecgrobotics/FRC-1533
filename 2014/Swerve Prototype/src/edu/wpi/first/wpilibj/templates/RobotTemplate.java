/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot {
    final double GYRO_GAIN = -.001;
    Joystick gamepad = new Joystick(1);
    Swerve swerve = new Swerve();
    double x, y, r;
    Gyro gyro = new Gyro(1);
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        gyro.reset();
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
        x = gamepad.getX();
        y = -gamepad.getY();
        r = gamepad.getZ();
        if (Math.abs(x) < .05) x = 0;
        if (Math.abs(y) < .05) y = 0;
        
        if (Math.abs(r) < .05) r = 0;
        //System.out.println(gyro.getAngle());
        if (gamepad.getRawButton(6)) {
            double r = Math.sqrt(x*x + y*y);
            double angle = MathUtils.atan2(y, x);
            angle += gyro.getAngle() * Math.PI / 180;
            x = r * Math.cos(angle);
            y = r * Math.sin(angle);
        } else if ((x!=0 || y!=0) && r==0) {
            r = gyro.getAngle() * GYRO_GAIN;
        } else
            gyro.reset();
        if (gamepad.getRawButton(1))
            swerve.returnToHome();
        if (gamepad.getRawButton(2))
            swerve.brake();
        else
            swerve.drive(x/2, y/2, r/2);
//        for (int i = 1; i <= 4; i++)
//            System.out.print("encoder " + i + ": " + swerve.getAngle(i));
//        System.out.println();
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
