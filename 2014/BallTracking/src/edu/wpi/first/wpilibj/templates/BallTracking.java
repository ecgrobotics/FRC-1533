/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.camera.*;
import edu.wpi.first.wpilibj.image.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class BallTracking extends IterativeRobot {
    AxisCamera cam = AxisCamera.getInstance("10.15.33.11");
    Joystick joy = new Joystick(1);
    ColorImage img;
    BinaryImage bin;
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
        if (joy.getRawButton(1)) {
            cam.writeResolution(AxisCamera.ResolutionT.k640x480);
            System.out.println("640x480");
        } else if (joy.getRawButton(4)) {
            cam.writeResolution(AxisCamera.ResolutionT.k320x240);
            System.out.println("320x240");
        } else if (joy.getRawButton(3)) {
            cam.writeResolution(AxisCamera.ResolutionT.k160x120);
            System.out.println("160x120");
        }
        if (joy.getRawButton(2)) {
            try {
                long time = System.currentTimeMillis();
                img = cam.getImage();
                bin = img.thresholdHSL(128, 184, 14, 255, 9, 139);
                bin = bin.removeSmallObjects(false, 1);
                bin = bin.convexHull(true);
                ParticleAnalysisReport report = bin.getOrderedParticleAnalysisReports()[0];
                System.out.println(report.boundingRectWidth+", "+report.boundingRectHeight+", "+report.particleArea);
                System.out.println(System.currentTimeMillis()-time);
                img.free();
                bin.free();
                System.gc();
            } catch (AxisCameraException ex) {
                ex.printStackTrace();
            } catch (NIVisionException ex) {
                ex.printStackTrace();
            } catch (IndexOutOfBoundsException e) {}
        }
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        
    }
    
}
