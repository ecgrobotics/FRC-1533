///*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class TeamHoloMain extends IterativeRobot {
    final double cameraAdjustment = .2;
    final double cameraTiltCenter = 35, cameraRotateCenter = 90;
    double cameraTiltPos = cameraTiltCenter, cameraRotatePos = cameraRotateCenter;
    Watchdog dog;
    Jaguar back, front, left, right;
    double front_power, back_power, left_power, right_power, gyroTarget;
    Joystick joyLeft, joyRight, gamepad;
    Gyro gyro;
    Servo cameraRotate, cameraTilt;
    boolean targetFound, searching;
    boolean headingFixed;
    Relay lights1, lights2;
    DigitalInput prox;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        dog = Watchdog.getInstance();
        front = new Jaguar(3);
        back = new Jaguar(2);
        left = new Jaguar(4);
        right = new Jaguar(1);
        joyLeft = new Joystick(2);
        joyRight = new Joystick(3);
        gamepad = new Joystick(1);
        gyro = new Gyro(1);
        cameraRotate = new Servo(5);
        cameraTilt = new Servo(6);
        lights1 = new Relay(1);
        lights2 = new Relay(2);
        prox = new DigitalInput(1);
        
        Thread laser = new Thread(new Runnable() {
            public void run() {
                double searchX = .5;
                double searchY = .5;
                while (!isEnabled()) {}
                cameraRotate.setAngle(cameraRotatePos);
                cameraTilt.setAngle(cameraTiltPos);
                while (true) {
                    if (isEnabled()) {
                        try {
                            double imageWidth = SmartDashboard.getNumber("Image Width");
                            double imageHeight = SmartDashboard.getNumber("Image Height");
                            double targetX = SmartDashboard.getNumber("Target X");
                            double targetY = SmartDashboard.getNumber("Target Y");
                            double targetWidth = SmartDashboard.getNumber("Target Width");
                            double targetHeight = SmartDashboard.getNumber("Target Height");
                            double laserAdjustX = SmartDashboard.getNumber("Laser Adjust X");
                            double laserAdjustY = SmartDashboard.getNumber("Laser Adjust Y");
                            targetFound = targetX!=0 && targetY!=0 && (targetWidth * targetHeight) > 100;
                            if (targetFound && SmartDashboard.getBoolean("Enable Tracking", false)) {
                                targetX -= imageWidth / 2;
                                targetY -= imageHeight / 2;
                                targetX *= 54.0 / imageWidth;
                                targetY *= 54.0 / imageWidth;
                                targetX += laserAdjustX;
                                targetX = cameraRotatePos + (targetX * cameraAdjustment);
                                targetX = Math.max(0, Math.min(180, targetX));
                                targetY -= laserAdjustY;
                                targetY = cameraTiltPos - (targetY * cameraAdjustment);
                                targetY = Math.max(0, Math.min(180, targetY));
                                cameraRotatePos = targetX;
                                cameraTiltPos = targetY;
                                cameraRotate.setAngle(cameraRotatePos);
                                cameraTilt.setAngle(cameraTiltPos);
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ex) {}
                                searching = false;
                            } else if (SmartDashboard.getBoolean("Enable Searching", false)){
                                searching = true;
                                cameraRotatePos += searchX;
                                cameraTiltPos += searchY;
                                if (cameraRotatePos > 180) {
                                    cameraRotatePos = 180;
                                    searchX *= -1.0;
                                } else if (cameraRotatePos < 0) {
                                    cameraRotatePos = 0;
                                    searchX *= -1.0;
                                }
                                if (cameraTiltPos < 0) {
                                    cameraTiltPos = 0;
                                    searchY *= -1.0;
                                } else if (cameraTiltPos > 60) {
                                    cameraTiltPos = 60;
                                    searchY *= -1.0;
                                }
                                cameraRotate.setAngle(cameraRotatePos);
                                cameraTilt.setAngle(cameraTiltPos);
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ex) {}
                            } else {
                                cameraRotatePos = cameraRotateCenter;
                                cameraTiltPos = cameraTiltCenter;
                                cameraRotate.setAngle(cameraRotatePos);
                                cameraTilt.setAngle(cameraTiltPos);
                                searching = false;
                            }
                        } catch (TableKeyNotDefinedException e) {

                        }
                    }
                }
            } 
        });
        laser.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        boolean front, back, left, right;
        front = back = left = right = false;
        if (targetFound) {
            double angle = cameraRotatePos - 90;
            double speed = 0;
            double width = SmartDashboard.getNumber("Target Width");
            if (width < 45) {
                speed = .4;
            } else if (width > 60) {
                speed = -.4;
            }
            double rot = 0;
            if (angle < -45) {
                rot = -.1;
            } else if (angle > 45) {
                rot = .1;
            }
            angle *= Math.PI / 180;
            driveMotors(Math.sin(angle)*speed, Math.cos(angle)*speed, rot);
            long time = System.currentTimeMillis();
            if (time % 1000 < 500) {
                front = back = true;
            } else {
                left = right = true;
            }
        } else if (searching) {
            brake();
        } else {
            brake();
        }
        setLights(front, back, left, right);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        dog.feed();
        double x, y, rot;
        boolean front, back, left, right;
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
            rot = 0;
        } else {
            rot = gamepad.getZ()/4;
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
        } else if (rot==0) {
            if (x!=0 || y!=0) {
                rot = gyro.getAngle() * -.005;
            } else {
                gyro.reset();
            }
        } else {
            gyro.reset();
        }
        if (gamepad.getRawButton(2)) {
            brake();
        } else {
            driveMotors(x, y, rot);
        }
        
        if (headingFixed && !gamepad.getRawButton(2)) {
            double angle = gyro.getAngle() % 360;
            while (angle < 0) {
                angle += 360;
            }
            front = angle < 60 || angle > 300;
            left = angle > 30 && angle < 150;
            back = angle > 120 && angle < 240;
            right = angle > 210 && angle < 330;
        } else if (x != 0 || y != 0) {
            double r = Math.sqrt(x*x + y*y);
            front = y > Math.abs(x)/2;
            back = y < -Math.abs(x)/2;
            left = x < -Math.abs(y)/2;
            right = x > Math.abs(y)/2;
        } else if (x == 0 && y == 0 && rot != 0) {
            int speed = 500;
            long time = System.currentTimeMillis();
            time %= speed;
            front = back = left = right = false;
            if (time < speed/4) {
                front = true;
            } else if (time >= speed/4 && time < speed/2) {
                if (rot > 0) {
                    right = true;
                } else {
                    left = true;
                }
            } else if (time >= speed/2 && time < 3*speed/4) {
                back = true;
            } else if (time >= 3*speed/4) {
                if (rot > 0) {
                    left = true;
                } else {
                    right = true;
                }
            }
        } else if (gamepad.getRawButton(7) || gamepad.getRawButton(8)) {
            front = back = left = right = true;
        } else {
            front = back = left = right = false;
        }
        setLights(front, back, left, right);
    }
    
    public void driveMotors(double x, double y, double rot) {
        front_power = Math.max(-1, Math.min(1, x+rot));
        back_power = Math.max(-1, Math.min(1, x-rot));
        left_power = Math.max(-1, Math.min(1, y+rot));
        right_power = Math.max(-1, Math.min(1, y-rot));
        
        front.set(-front_power);
        back.set(back_power);
        left.set(-left_power);
        right.set(right_power);
    }
    
    public void brake() {
        driveMotors(0, 0, 0);
    }
    
    public void setLights(boolean front, boolean back, boolean left, boolean right) {
        if (front && right) { 
            lights1.set(Relay.Value.kOn);
        } else if (!(front || right)) { 
            lights1.set(Relay.Value.kOff);
        } else if (front && !right) { 
            lights1.set(Relay.Value.kReverse);
        } else if (right && !front) {
            lights1.set(Relay.Value.kForward);
        }
        
        if (back && left) { 
            lights2.set(Relay.Value.kOn);
        } else if (!(back || left)) { 
            lights2.set(Relay.Value.kOff);
        } else if (back && !left) { 
            lights2.set(Relay.Value.kForward);
        } else if (left && !back) {
            lights2.set(Relay.Value.kReverse);
        }
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}