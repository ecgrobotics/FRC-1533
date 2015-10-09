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
public class Cataphract2014 extends IterativeRobot {
    Watchdog dog;
    Jaguar left,right,bottom1,bottom2,tshoot,bshoot;
     ADXL345_I2C accel = new ADXL345_I2C(1, ADXL345_I2C.DataFormat_Range.k8G);
     
  //  DigitalOutput out,light;
    volatile Jaguar flip;
    Joystick lstick,rstick;
    volatile Encoder tencoder=new Encoder(1,2);//1=a, 2=b
    volatile Encoder bencoder=new Encoder(3,4);
    Joystick pad;
    double speed=0;
    double max=.25;
    boolean fire=false;
    boolean flag=false;
    long last=0;
    PIDController tpid;
    PIDController bpid;
    DriverStation ds=DriverStation.getInstance();
    DriverStationLCD lcd=DriverStationLCD.getInstance();
   
        
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        lstick=new Joystick(1);
        rstick=new Joystick(2);
        pad=new Joystick(4);
        dog=Watchdog.getInstance();
        left=new Jaguar(1,2);
        right=new Jaguar(1,1);
        bottom1=new Jaguar(1,6);
        flip=new Jaguar(1,5);
        tshoot=new Jaguar(1,3);
        bshoot=new Jaguar(1,4);
        //tpid=new PIDController(1,1,1,top,tshoot,20);
        //bpid=new PIDController(1,1,1,bottom,bshoot,20);
    }
    
    public void autonomousInit(){
        
    }
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    }
    
    public void teleopInit(){
       
    }
            
    /**
     * This function is called periodically during operator control
     */
    long time=0;
    public void teleopPeriodic() {
        dog.feed();
       // light.set(true);
        if(!pad.getRawButton(6)&&!pad.getRawButton(5)){
            left.set(lstick.getY());
            right.set(-rstick.getY());
        }
        else{
            left.set(pad.getRawAxis(2));
            right.set(-pad.getRawAxis(4));
        }
        double temp= -lstick.getZ();
        speed=(temp + 1) / 2;
        speed=Math.abs(speed/2);
        if(pad.getRawButton(7)){
            if(speed>max){
                speed=max;
                System.out.println("Maxed!");
            }
        }
        if(pad.getRawButton(4)) {
            max+=.001;
        } else if(pad.getRawButton(2)) {
            max-=.001;
        }
        if(lstick.getRawButton(1)){
            tshoot.set(speed);
            bshoot.set(speed);
        }
        else{
            tshoot.set(0);
            bshoot.set(0);
        }
        
        if(rstick.getRawButton(3)){
            bottom1.set(-1);
            
        }
        else if(rstick.getRawButton(2)){
            bottom1.set(.5);
            
        }
        else{
            bottom1.set(0);
            
        }
        if(lstick.getRawButton(3)){
            flip.set(.7);
        }
        else if(lstick.getRawButton(2)){
            flip.set(-.7);
        }
        else {
            flip.set(0);
        }
        if(rstick.getRawButton(1)&&!pad.getRawButton(6)&&!pad.getRawButton(8)){
            if(!fire) {
                fire=true;
            }
        }
        if(fire){

            if(System.currentTimeMillis()-last>500){
                flip.set(-.7);
            }
            else{
                flip.set(.7);
            }
            if(System.currentTimeMillis()-last>500*2){
                fire=false;
                flip.set(0);
            }
        }
        if(!fire){
            last=System.currentTimeMillis();
        }
        double dt=  System.currentTimeMillis()-time;
        double td=(double)tencoder.get()*(1000.0/dt);
        double bd=(double)bencoder.get()*(1000.0/dt);
        td/=360;
        bd/=360;
        System.out.println("top= "+tencoder.getRate()+", bottom= "+bencoder.getRate());
        System.out.println(lstick.getZ());
        time=System.currentTimeMillis();
        tencoder.reset();
        bencoder.reset();
    }

    public void disabledPeriodic() {
       // light.set(false);
    }
    
    
    
}