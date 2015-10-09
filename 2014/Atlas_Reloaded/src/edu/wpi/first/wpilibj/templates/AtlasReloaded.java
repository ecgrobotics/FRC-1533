/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class AtlasReloaded extends IterativeRobot {
    final double GYRO_P_GAIN = -.025, //gain for gyro correction
            GYRO_D_GAIN = -.005,
            LIFT_GAIN = .05,
            LIFT_MAX = .3,
            LIFT_RATE = -1,
            SHOOTING_ANGLE = 25,
            SHOOTING_ANGLE_LONG = 35,
            SHOOTING_ANGLE_AUTO = 30,
            SPEED = 1;
    Jaguar fl, fr, bl, br, retractor, lift1, lift2;
    Relay roller, trigger;
    Joystick joy1, joy2,over;
    Gyro gyro;
    double x, y, rotation;
    boolean moving;
    Encoder liftEncoder;
    final double dthetaPerPulse=.2; //1:5 ratio (12:60)
    double liftTarget = 0;
    Watchdog dog;
    boolean shooting = false, retracting = false,noShoot=false,noRoll=false,
            noDrive=false,no=false;
    long autoStartTime, autoPrevTime;
    ADXL345_I2C accel=new ADXL345_I2C(1,ADXL345_I2C.DataFormat_Range.k8G);
    public static final double IMPACT_THRESHOLD=-.5;
    double ax,ay,vx,vy;
    boolean hot, autoShot, hitLowGoal;
    DigitalInput topSwitch, bottomSwitch, triggerSwitch;
    
    public void robotInit() {
        dog=Watchdog.getInstance();
        fl = new Jaguar(1);
        fr = new Jaguar(2);
        bl = new Jaguar(3);
        br = new Jaguar(4);
        retractor = new Jaguar(7);
        lift1 = new Jaguar(5);
        lift2 = new Jaguar(6);
        roller = new Relay(1);
        trigger = new Relay(2);
        joy1 = new Joystick(1);
        joy2 = new Joystick(2);
        over=new Joystick(4);
        gyro = new Gyro(1);
        gyro.reset();
        liftEncoder=new Encoder(2,3);
        liftEncoder.setDistancePerPulse(dthetaPerPulse);   
        liftEncoder.start();
        liftEncoder.reset();
        topSwitch = new DigitalInput(4);
        bottomSwitch = new DigitalInput(5);
        triggerSwitch = new DigitalInput(6);
//        comm.start();
        new Thread() {
            public void run() {
                while (true) {
                    SmartDashboard.putNumber("Angle Tank", 90-liftEncoder.getDistance());
                    SmartDashboard.putBoolean("Ready to Fire", topSwitch.get() && (bottomSwitch.get() || joy2.getRawButton(9)) && triggerSwitch.get());
                    SmartDashboard.putBoolean("Shooter Retracted", bottomSwitch.get());
                    SmartDashboard.putNumber("Num Solenoids", hot?1:0);
                }
            }
        }.start();
        
    }
    
    public void autonomousInit() {
        autoStartTime = System.currentTimeMillis();
        while (autoTime() < 750) {
            dog.feed();
            lift1.set(-.25);
            lift2.set(.25);
        }
        while (autoTime() < 1000) {
            dog.feed();
        }
        try {
            hot = SmartDashboard.getBoolean("Hot Goal", true);
        } catch (Exception e) {}
        liftTarget = SHOOTING_ANGLE_AUTO;
        gyro.reset();
        liftEncoder.reset();
        autoShot = false;
        hitLowGoal = false;
        shooting = false;
    }
//    public void autonomousPeriodic() {
//        dog.feed();
//        if (!shooting && !hitLowGoal) {
//            driveMotors(0,.45, GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate());
////            driveMotors(0,.45, GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate());
//        } else {
//            driveMotors(0, 0, 0);
//        }
//        boolean timebackup=false;
//        if(autoTime()>7000)timebackup=true;
//        if(autoTime()>2500 && !hitLowGoal && (accel.getAcceleration(ADXL345_I2C.Axes.kX)<IMPACT_THRESHOLD || timebackup)) {
//            hitLowGoal=true;
//            System.out.println("hit low goal "+autoTime());
//        }
//        if(!autoShot && hitLowGoal  && ((hot && autoTime() > 4000) || autoTime() > 7000)){
//            shooting=true;
//            autoShot = true;
//            System.out.println(hot);
//            System.out.println(autoTime());
//        }
//        if(System.currentTimeMillis()-autoStartTime>8500){
//            roller.set(Relay.Value.kReverse);
//        }
//        autoPrevTime = System.currentTimeMillis();
//        lift();
//        shooter();
//    }
    public void autonomousPeriodic() {
        dog.feed();
        if (hot && autoTime()<4000) {
            y = ((double)autoTime()-1000)/1000;
        } else if (!hot && autoTime()>3500 && autoTime()<6000) {
            y = ((double)autoTime()-3500)/1000;
        } else {
            y = 0;
        }
        driveMotors(0, Math.min(y, .45), GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate());
        if (!autoShot && ((hot && autoTime()>2500) || autoTime()>5000)) {
            shooting = true;
            autoShot = true;
        }
        lift();
        shooter();
    }
    
    private long autoTime() {
        return System.currentTimeMillis()-autoStartTime;
    }
    
    public void teleopPeriodic() {
        dog.feed();        
        no=over.getRawButton(6);
        noShoot= no||over.getRawButton(5);
        noRoll=no||over.getRawButton(7);
        noDrive=no||over.getRawButton(8);
        
        lift();
        drive();
        shooter();
       
        if (joy1.getRawButton(8) && !noRoll) {
            roller.set(Relay.Value.kForward);
        } else if (joy1.getRawButton(7) && !noRoll) {
            roller.set(Relay.Value.kReverse);
        } else {
            roller.set(Relay.Value.kOff);
        }
    }    
    
    public void drive(){
        moving = false;
        double X=noDrive ? over.getX() : joy1.getX();
        double Y=noDrive ? over.getY() : joy1.getY();
        double Z=noDrive ? over.getZ() : joy1.getZ();
        
        Y*=-1;
        
        if (Math.abs(X) > .05) {
            x = X*SPEED;
            moving = true;
        } else {
            x = 0;
        }
        if (Math.abs(Y) > .05) {
            y = Y*SPEED;
            moving = true;
        } else {
            y = 0;
        }
        if (joy1.getRawButton(2)) {
            x = 0;
            y = 0;
            rotation = 0;
            moving = true;
        }
        if (joy1.getRawButton(6)) {
            if (Math.abs(Z) > .05) {
                rotation = Z*SPEED;
            } else {
                rotation = 0;
            }
            double r = Math.sqrt(x*x + y*y);
            double angle = MathUtils.atan2(y, x);
            angle += gyro.getAngle() * Math.PI / 180;
            x = r * Math.cos(angle);
            y = r * Math.sin(angle);
        } else if (Math.abs(Z) > .05) {
            rotation = Z*SPEED;
            gyro.reset();
        } else if (moving) {
            rotation = GYRO_P_GAIN*gyro.getAngle() + GYRO_D_GAIN*gyro.getRate();
        } else {
            rotation = 0;
            gyro.reset();
        }
        driveMotors(x, y, rotation);
    }
    
    public void lift(){
        if (joy2.getRawButton(4)) {
            liftTarget = 95;
        } else if (joy2.getRawButton(3)) {
            liftTarget = SHOOTING_ANGLE;
        } else if (joy2.getRawButton(5)) {
            liftTarget = 0;
        } else if (joy2.getRawButton(2)) {
            liftTarget = SHOOTING_ANGLE_LONG;
        } else if (Math.abs(joy2.getY())>.05) {
            liftTarget += joy2.getY()*LIFT_RATE;
            liftTarget = Math.max(0, Math.min(95, liftTarget));
        }
        double angle = liftEncoder.getDistance();
        double power = LIFT_GAIN*(liftTarget-angle);
        power = Math.max(-LIFT_MAX, Math.min(LIFT_MAX, power));
        if (angle<10) {
            power = Math.max(-.05, power);
        } else if (angle>80) {
            power = Math.min(.15, power);
        }
        if (joy2.getRawButton(6) && joy2.getRawButton(7)) {
            power = joy2.getY()/4;
            liftEncoder.reset();
            liftTarget = 0;
        }
        lift1.set(power);
        lift2.set(-power);
    }
    
    long lastBottom = 0;
    long lastBottomDepressed = 0;
    boolean lastBottomState = true;
    public void shooter() {
//        System.out.println(bottomSwitch.get()+","+topSwitch.get()+","+triggerSwitch.get()+","+shooting);
        if (joy2.getRawButton(8)) {
            if (joy2.getRawButton(10) && !(bottomSwitch.get() || joy2.getRawButton(9))) {
                retractor.set(-1);
            } else if (joy2.getRawButton(11) && !topSwitch.get()) {
                retractor.set(.35);
            } else {
                retractor.set(0);
            }
        }
        if (!shooting && (bottomSwitch.get() || joy2.getRawButton(9)) && topSwitch.get() && triggerSwitch.get() && joy2.getRawButton(1) && !noShoot) {
            shooting = true;
        }
        if (!triggerSwitch.get()) {
            shooting = false;
        }
        if ((shooting && triggerSwitch.get()) || !triggerSwitch.get()) {
            trigger.set(Relay.Value.kForward);
        } else {
            trigger.set(Relay.Value.kOff);
        }
        if (bottomSwitch.get()) {
            lastBottom = System.currentTimeMillis();
        } 
        if (bottomSwitch.get() && !lastBottomState) {
            lastBottomDepressed = System.currentTimeMillis();
        }
        if (!joy2.getRawButton(8)) {
            if ((System.currentTimeMillis() - lastBottom > 500 && !(bottomSwitch.get() || joy2.getRawButton(9))) || System.currentTimeMillis()-lastBottomDepressed < 10) {
                retractor.set(-1);
            } else if (!topSwitch.get()) {
                retractor.set(.4);
            } else {
                retractor.set(0);
            }
        }
        lastBottomState = bottomSwitch.get();
    }
    
    public void driveMotors(double x, double y, double rot) {
        fl.set(y + x + rot);
        fr.set(-y + x + rot);
        bl.set(y - x + rot);
        br.set(-y - x + rot);
    }
    
}