/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import com.sun.squawk.io.BufferedReader;
import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.io.InputStreamReader;
import javax.microedition.io.*;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class AtlasClone extends IterativeRobot {
    final double GYRO_P_GAIN = -.015,
            GYRO_D_GAIN = -.005,
            PICKUP_GAIN = .05,
            PICKUP_MAX = .2,
            DISTANCE_GAIN = .01,
            DISTANCE_MAX = .5,
            CATCH_GAIN_X = 0.06,
            CATCH_GAIN_Y = -0.03,
            CATCH_GAIN_LIFT = 650,
            LIFT_GAIN = -0.01,
            LIFT_ANGLE_GAIN = .35,
            LIFT_MAX = .15,
            LIFT_RATE = -1,
            SHOOTING_ANGLE = 30,
            SPINNING_SPEED = .6;
    Jaguar fl, fr, bl, br, roller, lift1, lift2;
    Joystick joy1,joy2;
    Gyro gyro;
    double x, y, rotation;
    boolean moving;
    Encoder liftEncoder;
    double liftTarget = 15;
    double distTarget = 100;//213.4;
    Watchdog dog;
    ADXL345_I2C accel=new ADXL345_I2C(1,ADXL345_I2C.DataFormat_Range.k8G);
    double ax,ay,vx,vy;
    volatile double speed;
    volatile double theta1=0;
    volatile double theta2=0;
    AnalogChannel sonar=new AnalogChannel(3);
     public double sin(double r){
         return Math.sin(r);
     }
     public double cos(double r){
         return Math.cos(r);
     }
     double targetP, targetY, currentY, dt;
     long currentTime, lastTime;
     boolean spinLeft, spinLeftP, spinRight, spinRightP;
     
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        dog=Watchdog.getInstance();
        fl = new Jaguar(1);
        fr = new Jaguar(2);
        bl = new Jaguar(3);
        br = new Jaguar(4);
        roller = new Jaguar(7);
        lift1 = new Jaguar(5);
        lift2 = new Jaguar(6);
        joy1 = new Joystick(1);
        joy2 = new Joystick(2);
        gyro = new Gyro(1);
        gyro.reset();
        liftEncoder=new Encoder(4,5);
        liftEncoder.setDistancePerPulse(1.5);
        liftEncoder.start();
        liftEncoder.reset();
//        comm.start();
        new Thread() {
            public void run() {
                while (true) {
                    SmartDashboard.putNumber("Angle Tank", 90-liftEncoder.getDistance());
                    SmartDashboard.putNumber("Speed", speed);
                }
            }
        }.start();
        Thread spedometer=new Thread(){
            public void run(){         
                
                long last=System.currentTimeMillis();
                double dt=0;
                 while(true){
                    ax=accel.getAcceleration(ADXL345_I2C.Axes.kX);
                    ay=accel.getAcceleration(ADXL345_I2C.Axes.kY);
                    dt=System.currentTimeMillis()-last;
                    vx+=.001*dt*ax*9.81;//v in m/s
                    vy+=.001*dt*ay*9.81;
                    last=System.currentTimeMillis();
                    speed=Math.sqrt((vx*vx)+(vy*vy));
                }
            }
        };
        spedometer.start();    
    }
    
    ColorImage color;
    BinaryImage bin;
    ParticleAnalysisReport report;
    ParticleAnalysisReport[] reports;
    public boolean isHot() throws Exception{
        AxisCamera camera = AxisCamera.getInstance("10.15.33.12");
        color=camera.getImage();
        bin= color.thresholdRGB(0, 10, 150, 255, 150, 255);
         reports=bin.getOrderedParticleAnalysisReports(1);
        if(reports.length<1)return false;
         report=reports[0];
        int x=report.boundingRectLeft;
        int w=report.boundingRectWidth;
        int cx=camera.getResolution().width/2;
        if(x<cx&&x+w>cx)return true;
        return false;
    }
    
    public void teleopInit(){
        lastTime = System.currentTimeMillis();
    }
    public void teleopPeriodic() {
        currentTime = System.currentTimeMillis();
        dt = (currentTime-lastTime) / 1000f;
        lastTime = currentTime;
        dog.feed();
        lift();
        if (joy1.getRawButton(5)) {
            catchBall();
        } else {
            drive();
            if (joy1.getRawButton(8)) {
                roller.set(1);
            } else if (joy1.getRawButton(7)) {
                roller.set(-1);
            } else {
                roller.set(0);
            }
        }
    }    
    
    public void drive(){
        if (joy1.getRawButton(1) && !spinLeftP && !(spinLeft || spinRight)) {
            spinLeft = true;
            gyro.reset();
        }
        if (joy1.getRawButton(3) && !spinRightP && !(spinLeft || spinRight)) {
            spinRight = true;
            gyro.reset();
        }
        if ((spinLeft || spinRight) && Math.abs(gyro.getAngle()) >= 320) {
            gyro.reset();
            spinLeft = spinRight = false;
        }
        spinLeftP = joy1.getRawButton(1);
        spinRightP = joy1.getRawButton(3);
        moving = false;
        if (Math.abs(joy1.getX()) > .05) {
            x = joy1.getX()/2;
            moving = true;
        } else {
            x = 0;
        }
        if (joy1.getRawButton(4)) {
            y = DISTANCE_GAIN * (sonar.getAverageVoltage()/.0049 - distTarget);
            y = Math.max(-DISTANCE_MAX, Math.min(DISTANCE_MAX, y));
        } else if (Math.abs(joy1.getY()) > .05) {
            y = -joy1.getY()/2;
            moving = true;
        } else {
            y = 0;
        }
        if (spinLeft || spinRight) {
            double r = Math.sqrt(x*x + y*y);
            double angle = MathUtils.atan2(y, x);
            angle += gyro.getAngle() * Math.PI / 180;
            x = r * Math.cos(angle);
            y = r * Math.sin(angle);
            if (spinLeft) {
                rotation = -SPINNING_SPEED;
            } else {
                rotation = SPINNING_SPEED;
            }
        } else if (joy1.getRawButton(2) ) {
            double theta;
            if (DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.kBlue)) {
                theta = SmartDashboard.getNumber("Blue Theta");
            } else {
                theta = SmartDashboard.getNumber("Red Theta");
            }
            rotation = PICKUP_GAIN*theta + GYRO_D_GAIN*gyro.getRate();
            rotation = Math.max(-PICKUP_MAX, Math.min(PICKUP_MAX, rotation));
        } else if (joy1.getRawButton(6)) {
            rotation = joy1.getZ()/2;
            if (Math.abs(rotation) < .05) {
                rotation = 0;
            }
            double r = Math.sqrt(x*x + y*y);
            double angle = MathUtils.atan2(y, x);
            angle += gyro.getAngle() * Math.PI / 180;
            x = r * Math.cos(angle);
            y = r * Math.sin(angle);
        } else if (Math.abs(joy1.getZ()) > .05) {
            rotation = joy1.getZ();
            gyro.reset();
        } else if (moving) {
            rotation = GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate();
        } else {
            rotation = 0;
            gyro.reset();
        }
        driveMotors(x, y, rotation);
    }
    boolean newData=false;
    double d1,d2;
    public double getDeltaY(){
        return -d1*sin(theta1)+((-d1*cos(theta1))/(d2*cos(theta2)-d1*cos(theta1)))*(-d2*sin(theta2)+d1*sin(theta1));   
    }
    public double getP(){
        double y1=-d1*sin(theta1);
        double y2=-d2*sin(theta2);
        double x1=d1*cos(theta1);
        double x2=d2*cos(theta2);
        return 180*MathUtils.atan((y2-y1)/(x2-x1))-90;
    }
    public void catchBall() {
        double distance, theta, phi;
        newData=SmartDashboard.getBoolean("New Data",false);
        if(newData){
            theta1=theta2;
            d1=d2;
        }
        if (DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.kBlue)) {
            distance = SmartDashboard.getNumber("Blue Distance", 0);
            theta = SmartDashboard.getNumber("Blue Theta", 0);
            phi = SmartDashboard.getNumber("Blue Phi", 0);
        } else {
            distance = SmartDashboard.getNumber("Red Distance", 0);
            theta = SmartDashboard.getNumber("Red Theta", 0);
            phi = SmartDashboard.getNumber("Red Phi", 0);
            phi-=10;
        }    
        d2=distance;
        if(newData){
            theta2=Math.toRadians(theta);
            SmartDashboard.putBoolean("New Data", false);
            vy = 0;
            targetP = getP();
            targetY = getDeltaY();
        }
        driveMotors(0, 0, 0);
    }
    
    public void lift(){
        if (joy2.getRawButton(4)) {
            liftTarget = 95;
        } else if (joy2.getRawButton(3)) {
            liftTarget = SHOOTING_ANGLE;
        } else if (joy2.getRawButton(5)) {
            liftTarget = 0;
        } else if (Math.abs(joy2.getY())>.05) {
            liftTarget += joy2.getY()*LIFT_RATE;
            liftTarget = Math.max(0, Math.min(95, liftTarget));
        }
        double angle = liftEncoder.getDistance();
        double power = LIFT_GAIN*(liftTarget-angle);
        power = Math.max(-LIFT_MAX, Math.min(LIFT_MAX, power)) + LIFT_ANGLE_GAIN*Math.sin((angle-18)*Math.PI/180);
        power = 0;
        lift1.set(power);
        lift2.set(-power);
    }
    
    public void driveMotors(double x, double y, double rot) {
        fl.set(y + x + rot);
        fr.set(-y + x + rot);
        bl.set(y - x + rot);
        br.set(-y - x + rot);
    }
    
    
}