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
public class Atlas2014 extends IterativeRobot {
    final double GYRO_P_GAIN = -.025, //gain for gyro correction
            GYRO_D_GAIN = -.005,
            PICKUP_GAIN = .05,
            PICKUP_MAX = .3,
            DISTANCE_GAIN = .01,
            DISTANCE_MAX = .5,
            CATCH_GAIN_X = 0.06,
            CATCH_GAIN_Y = -0.03,
            CATCH_GAIN_LIFT = 650,
            LIFT_GAIN = .05,
            LIFT_MAX = .3,
            LIFT_RATE = -1,
            SHOOTING_ANGLE = 25,
            SPINNING_SPEED = .6;
    final DoubleSolenoid.Value FORWARD = DoubleSolenoid.Value.kForward;
    final DoubleSolenoid.Value REVERSE = DoubleSolenoid.Value.kReverse;
    final DoubleSolenoid.Value OFF = DoubleSolenoid.Value.kOff;
    Joystick override=new Joystick(4);
    Jaguar fl, fr, bl, br, roller, lift1, lift2;
    Joystick joy1,joy2;
    DoubleSolenoid[] solenoids=new DoubleSolenoid[4];// s1, s2, s3, s4; //secret password: cdskgu476di27dxuw2
    Relay compressor;
    DigitalInput guage;
    AnalogChannel pistonSensor, pressureSensor;
    Gyro gyro;
    double x, y, rotation;
    boolean moving;
    Encoder liftEncoder; 
    final double dthetaPerPulse=.2;//1:5 ratio (12:60
    double liftTarget = 15;
    Watchdog dog;
    Relay vacuum;
    SocketConnection sock;
    BufferedReader in;
    volatile double blueDist,blueTheta,bluePhi,redDist,redTheta,redPhi;
    volatile int dt;
    volatile boolean sockConnected;
    boolean shooting = false, retracting = false;
    long shootTime = 0;
    int numSolenoids = 4;
    long autoStartTime, autoPrevTime;
    ADXL345_I2C accel=new ADXL345_I2C(1,ADXL345_I2C.DataFormat_Range.k8G);
    public static final double IMPACT_THRESHOLD=-.5;
    double ax,ay,vx,vy;
    volatile double speed;
    boolean spinLeft, spinRight, spinLeftP, spinRightP;
    AnalogChannel sonar = new AnalogChannel(4);
    double distTarget = 213.4;
    boolean hot, autoShot;
    boolean b5,b6,b7,b8;
    Jaguar cutter;
    AxisCamera camera;// = AxisCamera.getInstance("10.15.33.12");
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
        cutter = new Jaguar(8);
        joy1 = new Joystick(1);
        joy2 = new Joystick(2);
        if(solenoids.length>4)System.err.println("CHANGE FOR LOOP FOR SOLENOID 5");
        for(int i=0;i<solenoids.length;i++){
            solenoids[i]=new DoubleSolenoid(2*i+1,2*(i+1));
        }
        compressor = new Relay(1);
        guage = new DigitalInput(1);
        pistonSensor = new AnalogChannel(2);
        pressureSensor = new AnalogChannel(3);
        gyro = new Gyro(1);
        gyro.reset();
        vacuum = new Relay(7);
        liftEncoder=new Encoder(2,3);
        liftEncoder.setDistancePerPulse(dthetaPerPulse);   
        liftEncoder.start();
        liftEncoder.reset();
//        comm.start();
        new Thread() {
            public void run() {
                while (true) {
                    SmartDashboard.putBoolean("Shooter Retracted", pistonSensor.getValue()>500);
                    SmartDashboard.putNumber("Angle Tank", 90-liftEncoder.getDistance());
                    SmartDashboard.putNumber("Pressure Gauge", (pressureSensor.getAverageValue()-495)*12);
                    SmartDashboard.putBoolean("Ready to Fire", pressureSensor.getAverageValue()>=500 && pistonSensor.getValue()>500);
                    SmartDashboard.putNumber("Num Solenoids", numSolenoids);
                    SmartDashboard.putNumber("Speed", speed);
                }
            }
        }.start();
        
    }
    
    public void autonomousInit() {
        autoStartTime = System.currentTimeMillis();
        while (System.currentTimeMillis()-autoStartTime < 200) {
            dog.feed();
        }
//        try {
//            hot = SmartDashboard.getBoolean("Hot Goal");
//        } catch (Exception e) {}
        hot = false;
        liftTarget = SHOOTING_ANGLE;
        gyro.reset();
        autoShot = false;
    }
    boolean hitLowGoal=false;
    public void autonomousPeriodic() {
        dog.feed();
        if (!shooting && !hitLowGoal) {
            driveMotors(0,.45, GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate());
        } else {
             driveMotors(0, 0, 0);
        }
        boolean timebackup=false;
        if(System.currentTimeMillis()-autoStartTime>7000)timebackup=true;
        if(System.currentTimeMillis()-autoStartTime>1000 && !hitLowGoal &&( accel.getAcceleration(ADXL345_I2C.Axes.kX)<IMPACT_THRESHOLD || timebackup) ){
            hitLowGoal=true;
        }
        if(!autoShot && hitLowGoal  && ((hot && System.currentTimeMillis()-autoStartTime > 3500) || System.currentTimeMillis()-autoStartTime > 7000)){
            shooting=true;
        }
        if(System.currentTimeMillis()-autoStartTime>8500){
            roller.set(-1);
        }
        autoPrevTime = System.currentTimeMillis();
        pneumatics();
        lift();
    }
    
    ColorImage color;
    BinaryImage bin;
    ParticleAnalysisReport report;
    ParticleAnalysisReport[] reports;
    public boolean isHot() throws Exception{
        color=camera.getImage();
        color.write("image.jpg");
        bin= color.thresholdHSL(0, 255, 0, 100, 150, 255);
        bin.write("threshold.jpg");
        reports=bin.getOrderedParticleAnalysisReports(1);
        if (reports.length<1) {
            return false;
        }
        report=reports[0];
        int x=report.boundingRectLeft;
        int w=report.boundingRectWidth;
        int cx=camera.getResolution().width/2;
        if (x<cx && x+w>cx) {
            return true;
        }
        return false;
    }
    
    public void teleopInit(){
//        try {
//            System.out.println(isHot());
//        } catch (Exception e) {}
        /*
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
        //spedometer.start();                
         */
    }
    public void teleopPeriodic() {
        dog.feed();
        b5=override.getRawButton(5);
        b6=override.getRawButton(6);
        b7=override.getRawButton(7);
        b8=override.getRawButton(8);
        lift();
        pneumatics();
        if (joy1.getRawButton(5)) {
            cutter.set(4.8/12);
        } else {
            cutter.set(0);
        }
        if (false) {
            catchBall();
        } else {
            if (!b6 && !b8) {
                drive();
            } else {
                driveOverride();
            }
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
            x = joy1.getX();
            moving = true;
        } else {
            x = 0;
        }
        if (joy1.getRawButton(4)) {
            y = DISTANCE_GAIN * (sonar.getAverageVoltage()/.0049 - distTarget);
            y = Math.max(-DISTANCE_MAX, Math.min(DISTANCE_MAX, y));
        } else if (Math.abs(joy1.getY()) > .05) {
            y = -joy1.getY();
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
            try {
                if (DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.kBlue)) {
                    theta = SmartDashboard.getNumber("Blue Theta");
                } else {
                    theta = SmartDashboard.getNumber("Red Theta");
                }
            } catch (Exception e) {
                theta = 0;
            }
            rotation = PICKUP_GAIN*theta + GYRO_D_GAIN*gyro.getRate();
            rotation = Math.max(-PICKUP_MAX, Math.min(PICKUP_MAX, rotation));
        } else if (joy1.getRawButton(6)) {
            rotation = joy1.getZ();
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
    
    public void catchBall() {
        double area, theta, phi;
        if (DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.kBlue)) {
            area = SmartDashboard.getNumber("Blue Area", 0);
            theta = SmartDashboard.getNumber("Blue Theta", 0);
            phi = SmartDashboard.getNumber("Blue Phi", 0);
        } else {
            area = SmartDashboard.getNumber("Red Area", 0);
            theta = SmartDashboard.getNumber("Red Theta", 0);
            phi = SmartDashboard.getNumber("Red Phi", 0);
            phi-=10;
        }
        if (area > 100 && area < 1500) {
            liftTarget = liftEncoder.getDistance() - phi*Math.min(1, CATCH_GAIN_LIFT/area);
            liftTarget = Math.max(20, Math.min(45, liftTarget));
            double driveX = theta * CATCH_GAIN_X;
            double driveY = phi * CATCH_GAIN_Y;
//            System.out.println("Catching speeds: " + driveX + ", " + driveY);
            driveMotors(driveX, driveY, 0);
            roller.set(1);
        } else {
            liftTarget = SHOOTING_ANGLE;
            driveMotors(0, 0, 0);
            roller.set(0);
        }
    }
    
    public void lift(){
        if (!b7&&!b8&&joy2.getRawButton(4)) {
            liftTarget = 95;
        } else if (!b7&&!b8&&joy2.getRawButton(3)) {
            liftTarget = SHOOTING_ANGLE;
        } else if (!b7&&!b8&&joy2.getRawButton(5)) {
            liftTarget = 0;
        } else if (!b8&&!b7&&Math.abs(joy2.getY())>.05) {
            liftTarget += joy2.getY()*LIFT_RATE;
            liftTarget = Math.max(0, Math.min(95, liftTarget));
        }
        else if(b8||b7){
            if (override.getRawButton(2)) {
                liftTarget += -1*LIFT_RATE;
            } else if (override.getRawButton(4)) {
                liftTarget += 1*LIFT_RATE;
            }
            liftTarget = Math.max(0, Math.min(95, liftTarget));
        }
        double angle = liftEncoder.getDistance();
        double power = LIFT_GAIN*(liftTarget-angle);
        power = Math.max(-LIFT_MAX, Math.min(LIFT_MAX, power));
        if (angle<10) {
            power = Math.max(-.05, power);
        } else if (angle>80) {
            power = Math.min(.1, power);
        }
        lift1.set(power);
        lift2.set(-power);
    }
    
    public void pneumatics(){
        if (joy2.getRawButton(7)) {
            numSolenoids = 4;
            System.out.println(numSolenoids);
        } else if (joy2.getRawButton(6)) {
            numSolenoids = 3;
            System.out.println(numSolenoids);
        } else if (joy2.getRawButton(10)) {
            numSolenoids = 2;
            System.out.println(numSolenoids);
        } else if (joy2.getRawButton(11)) {
            numSolenoids = 1;
            System.out.println(numSolenoids);
        }
        if (!b5&&!b8&&joy2.getRawButton(1)) {
            shooting = true;
        } else if (joy2.getRawButton(2)) {
            shooting = false;
        }
//        if (joy2.getRawButton(2)) {
//            vacuum.set(Relay.Value.kForward);
//        } else if (!shooting) {
//            if (vacuum.get().equals(Relay.Value.kForward)) {
//                vacuum.set(Relay.Value.kReverse);
//            } else {
//                vacuum.set(Relay.Value.kOff);
//            }
//            
//        }
        if (!shooting) {
            for (int i=0; i<solenoids.length; i++) {
                if (solenoids[i].get().equals(FORWARD)) {
                    solenoids[i].set(REVERSE);
                } else if (solenoids[i].get().equals(REVERSE)) {
                    solenoids[i].set(OFF);
                }
            }
            vacuum.set(Relay.Value.kOff);
        }
        if (shooting) {
            shoot();
        }
        if (!guage.get()) {
            compressor.set(Relay.Value.kForward);
        } else {
            compressor.set(Relay.Value.kOff);
        }
    }
    
//    public void shoot() {
//        if (shootTime==0) {
//            for (int i=0; i<numSolenoids; i++) {
//                solenoids[i].set(FORWARD);
//            }
//            shootTime = System.currentTimeMillis();
//            retracting = false;
//        } else if (!retracting && System.currentTimeMillis()-shootTime>500) {
//            for (int i=0; i<solenoids.length; i++) {
//                solenoids[i].set(REVERSE);
//            }
//            vacuum.set(Relay.Value.kForward);
//            shootTime = System.currentTimeMillis();
//            retracting = true;
//        } else if (retracting && (System.currentTimeMillis()-shootTime>10000 || pistonSensor.getValue() > 500)) {
//            vacuum.set(Relay.Value.kReverse);
//            shooting = false;
//            retracting = false;
//            shootTime = 0;
//        }
//    }
     public void shoot() {
        if (shootTime==0) {
            for (int i=0; i<numSolenoids; i++) {
                solenoids[i].set(FORWARD);
            }
            vacuum.set(Relay.Value.kForward);
            shootTime = System.currentTimeMillis();
            retracting = false;
        } else if (shooting && System.currentTimeMillis()-shootTime>500) {
            shooting = false;
            retracting = false;
            shootTime = 0;
        }
    }
    
    public void driveMotors(double x, double y, double rot) {
        fl.set(y + x + rot);
        fr.set(-y + x + rot);
        bl.set(y - x + rot);
        br.set(-y - x + rot);
    }
    public void driveOverride(){
        
        moving = false;
        if (Math.abs(override.getX()) > .05) {
            x = override.getX();
            moving = true;
        } else {
            x = 0;
        }
        if (Math.abs(override.getY()) > .05) {
            y = -override.getY();
            moving = true;
        } else {
            y = 0;
        }
          if (Math.abs(override.getZ()) > .05) {
            rotation = override.getZ();
            gyro.reset();
        } else if (moving) {
            rotation = GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate();
        } else {
            rotation = 0;
            gyro.reset();
        }
        driveMotors(x, y, rotation);
    }
}