
package org.usfirst.frc.team1533.robot;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */



//*******************TODO HIGH PRIORITY! add a button to make the eggbeaters try to go to their current location
//Also make buttons 7&8 together  disable the talons
public class Robot extends IterativeRobot {
	//static LaunchPad pad=new LaunchPad();
	static ADC adc=new ADC(I2C.Port.kMXP);
	DOF10 dof=new DOF10(I2C.Port.kOnboard,0,-1);
	SwerveDrive swerve = new SwerveDrive(21, 21, dof);
	Lift front=new Lift(new Victor(8),8,3,2);
	Lift back=new Lift(new CANTalon(2),6,0,1);
	Joystick joy1 = new Joystick(0),
			joy2 = new Joystick(1);
	Thread dash,beat;
	Eggbeater feleft = new Eggbeater(13, 7, -85, true),
			feright = new Eggbeater(12, 6, 130, false),
			//feleft = new Eggbeater(8, 7, -70, true),
			//feright = new Eggbeater(9,6, 135, false),
			beleft = new Eggbeater(11, 4, -45, true),
			beright = new Eggbeater(10, 5, 90, false);
//			beleft = new Eggbeater(8, 4, -45, true),
//			beright = new Eggbeater(9, 5, 90, false);
	
	Thread auto;
	Encoder encoder = new Encoder(22, 23, 21); ///////TODO   MOVE ENCODER WIRES TO 11, 12, 13
	Autonomous auton = new Autonomous(this, swerve, dof, encoder);
	boolean reversed;
//	CameraServer server;
//	Encoder denc = new Encoder(25, 24, 23);
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
	static PowerDistributionPanel pdp=new PowerDistributionPanel();
    public void robotInit() {
    	pdp.clearStickyFaults();
//    	server = CameraServer.getInstance();
//    	server.setQuality(50);
//    	server.startAutomaticCapture("cam0");
    	dash = new Thread() {
    		public void run() {
    			while (true) {
    		    	SmartDashboard.putNumber("fldist", front.getLeftDistance());
    		    	SmartDashboard.putNumber("frdist", front.getRightDistance());
    		    	SmartDashboard.putNumber("bldist", back.getLeftDistance());
    		    	SmartDashboard.putNumber("brdist", back.getRightDistance());
    		    	SmartDashboard.putNumber("flift" , front.getHeight());
    		    	SmartDashboard.putNumber("blift" , back.getHeight());
    		    	SmartDashboard.putNumber("fli", pdp.getCurrent(5));
    		    	SmartDashboard.putNumber("bri",pdp.getCurrent(6));//4
    		    	SmartDashboard.putNumber("fri",pdp.getCurrent(9));
    		    	SmartDashboard.putNumber("bli", pdp.getCurrent(7));//8
    		    	SmartDashboard.putBoolean("reversed", reversed);
    		    	SmartDashboard.putNumber("speed", encoder.getRate()/12.0);//TODO DUNCAN!!!!!!!! we wants the speeeeeeed
    		    	SmartDashboard.putNumber("dir",dof.getYaw());
    		    	//SmartDashboard.putBoolean("to", pad.to);
    		    //	System.out.println(front.getLeftDistance()+", "+front.getRightDistance()+", "+back.getRightDistance()+", "+back.getLeftDistance());
    		    	try {
    		    		Thread.sleep(50);
    		    	} catch (Exception e) {}
    			}
    		}
    	};
    	dash.start();
//    	beat=new Thread(){
//    		public void run(){
//    			while(true){
//
//    				feleft.update();
//    				feright.update();
//    				beleft.update();
//    				beright.update();
//    				try{
//    					Thread.sleep(5);
//    				}
//    				catch(Exception e){
//    					
//    				}
//    			}
//    		}
//    	};
//    	beat.start();
    	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    	dof.startGyroIntegrator();
    }
    public void disabledPeriodic() {
    	System.out.println("front: "+front.getHeight()+" back: "+back.getEncoderValue());
    }
    public boolean delay(double s){
    	if(!this.isAutonomous()||this.isDisabled()){
    		return true;
    	}
    	try{
    		Thread.sleep((long)(s*1000));
    	}catch(Exception e){
    		return true;
    	}
    	return false;
    }
    public static final int T2B1=0;
    public static final int T1B1=1;
    public static final int B2=2;
    public static final int DS=3;
    public static final int NOTHING=4;
    public static final int GROUP=5;
    public static final int B2_NO_TOTE=6;
    public static final int B1=7;
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousInit() {
    	int r = 0;
    	double d = 0;
    	try{
    		r=(int)SmartDashboard.getNumber("route");
    		d=SmartDashboard.getNumber("distance");
    	}catch(Exception e){}
    	final int route = r;
    	final double distance = d;
//    	auto = new Thread() {
//    		public void run() {
//    			dof.resetYaw();
//    			
////    			feleft.set(0);
////    			feright.set(0);
////    			beleft.set(-175);
////    			beright.set(175);
//    			
//    			feleft.driveUntilStall(0, 1);
//    			feright.driveUntilStall(0, 1);
//    			beleft.driveUntilStall(-175,1);
//    			beright.driveUntilStall(175,1);
//    			
//    			if(delay(.5))return;
//    			front.goToHeight(18);
//    			back.goToHeight(35);
//    			if(delay(.75))return;
//    			auton.rotate(-180);
//    			if(delay(.5))return;
//    			auton.move(0, .4, 19, 180, true);
//    			front.goToHeight(0);
//    			if(delay(.5))return;
//    			feleft.set(-90);
//    			feright.set(90);
//    			if(delay(1.75))return;
//    			feleft.driveUntilStall(0, 1);
//    			feright.driveUntilStall(0, 1);
////    			feleft.set(0);
////    			feright.set(0);
//    			if(delay(.75))return;
//    			front.goToHeight(18);
//    			if(delay(1))return;
//    			auton.move(-.5, 0, 130, 180, true);
////    			auton.rotate(180);
////    			if(delay(.5))return;;
////    			auton.move(.25, .25, 50, 0, false);
////    			auton.move(0, .35, 73, 0, true);
////    			if(delay(.5))return;
////    			auton.move(-.35, 0, 28, 0, true);
//    		}
//    	};
    	auto = new Thread() {
    		public void run() {
    			System.out.println(route);
    			if (route==NOTHING) return;
    			feleft.set(90);
    			feright.set(90);
    			beright.set(90);
    			beleft.set(90);
    			dof.resetYaw();
    			
    			if (route==DS) {
    				auton.move(.35, 0, distance, 0, false);
    				return;
    			}
    			
//    			feleft.set(0);
//    			feright.set(0);
//    			beleft.set(-175);
//    			beright.set(175);
    			
    			if (route==B2) {
    				return;
    			}
    			
    			if (route==T1B1 || route==T2B1) {
	    			feleft.driveUntilStall(0, 1);
	    			feright.driveUntilStall(0, 1);
    			}
    			beleft.driveUntilStall(175,1);
    			beright.driveUntilStall(175,1);
    			if (route==B1) {
    				back.goToHeight(12);
    				//if (delay(.1)) return;
    				if(delay(.5))return;
    				beleft.binHold();
    				beright.binHold();
    				auton.move(0, -.7, 33, 0, false);
    				auton.move(0, -.35, 33, 0, true);
    				
    				/*
    				auton.move(0,.4,66,0,false);
    				auton.move(.4, 0, 9*12, 0, false);*/
    				auton.rotate(90);
    				return;
    			}
    			
    			if(delay(.5))return;
    			beleft.binHold();
    			beright.binHold();
    			if(route==B2_NO_TOTE){
    				back.goToHeight(8);
    				while(back.getHeight()<2)if(delay(.001))return;
    				auton.move(0, .35, 31, 0, true);
    				feright.driveUntilStall(175, 1);
    				feleft.driveUntilStall(175, 1);
    				if(delay(.5))return;
    				front.goToHeight(8);
    				while(front.getHeight()<7)if(delay(.001))return;
    			}
    			else{
    			
	    			front.goToHeight(8);
	    			back.goToHeight(18);
	    			while (back.getHeight()<17) if(delay(.001)) return;
	    			
	    			if (route==T2B1 || route==GROUP) {
		    			auton.move(0, -.35, 19, 0, true);
		    			back.goToHeight(0);
		    			while (back.getHeight()>12) if(delay(.001)) return;
		    			beleft.set(90);
		    			beright.set(90);
		    			while(back.getHeight()>1) if (delay(.001)) return;
		    			beleft.driveUntilStall(-20, .8);
		    			beright.driveUntilStall(-20, .8);
		//    			beleft.set(0);
		//    			beright.set(0);
		    			if(delay(.75))return;
		    			back.goToHeight(8);
	    			}
    			}
    			if(delay(.5))return;
    			auton.move(.4, 0, 94, 0, false);
    		}
    	};
//    	auto = new Thread() {
//    		public void run() {
//    			dof.resetYaw();
//    			
//    			feleft.set(-90);
//    			feright.set(90);
//    			beleft.set(-120);
//    			beright.set(90);
//    			
//    			auton.move(.55, 0, 28, 0, true);
////    			swerve.rateDrive(0, -.05);
////    			beleft.set(-90);
//    			
//    			beleft.driveUntilStall(0, 1);
//    			beright.driveUntilStall(0, 1);
////    			beleft.driveUntilStall(-175,1);
////    			beright.driveUntilStall(175,1);
//    			if(delay(.25))return;
//    			back.goToHeight(18);
//    			if(delay(.5))return;
//    			auton.move(0, .425, 19, 0, true);
//    			feleft.driveUntilStall(0, 1);
//    			feright.driveUntilStall(0, 1);
////    			beleft.set(0);
////    			beright.set(0);
//    			if(delay(.25))return;
//    			front.goToHeight(18);
//    			if(delay(.5))return;
//    			auton.move(0, .425, 82, 0, true);
//    			front.goToHeight(0);
//    			while (front.getHeight()>12) if(delay(.001)) return;
//    			feleft.set(-90);
//    			feright.set(90);
//    			while(front.getHeight()>1) if (delay(.001)) return;
//    			feleft.driveUntilStall(0, 1);
//    			feright.driveUntilStall(0, 1);
////    			beleft.set(0);
////    			beright.set(0);
//    			if(delay(.75))return;
//    			front.goToHeight(18);
//    		}
//    	};
    	auto.start();
    }
    
    public void autonomousPeriodic() {
    	feleft.update();
		feright.update();
		beleft.update();
		beright.update();
    }
    public void teleopInit(){
    	swerve.rateDrive(0, 0, 0);
    }
    public void disabledInit() {
    	swerve.rateDrive(0, 0, 0);
    	if (auto != null && auto.isAlive()) auto.interrupt();
    	
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
//    	System.out.println(dof.getYaw());
//    	System.out.println(front.getRightDistance()+", "+back.getLeftDistance()+", "+back.getRightDistance());
    	if (joy1.getRawButton(4)) front.set(-1);
    	else if (joy1.getRawButton(1)) front.set(1);	
    	else front.set(joy2.getRawAxis(3));
    	back.set(joy2.getRawAxis(1));
    	//System.out.println(encoder.getDistance());
    	
    	if (joy1.getRawButton(2)) {
    		feleft.driveUntilStall(-10,1);
			feright.driveUntilStall(-10,1);
    	} else if (joy1.getRawButton(3)) {
    		feleft.set(90);
			feright.set(90);
    	} else if (joy1.getRawButton(10)) {
    		feleft.driveUntilStall(180,1);
			feright.driveUntilStall(180,1);
    	} else if (joy1.getRawButton(9)) {
    		feright.binHold();
			feleft.binHold();
    	}
    	
    	if (joy1.getRawButton(7)) {
    		reversed = true;
    		swerve.setPivot(0, -30);
    	} else if (joy1.getRawButton(8)) {
    		reversed = false;
    		swerve.setPivot(0, 30);
    	} else {
    		swerve.setPivot(0, 0);
    	}
    	double x, y, r;
		x = joy1.getX();
		y = -joy1.getY();
		r = joy1.getZ();
		if (Math.abs(x) < .1) x = 0;
		if (Math.abs(y) < .1) y = 0;
		if (Math.abs(r) < .1) r = 0;
		if (reversed) {
			x *= -1;
			y *= -1;
		}
		if (!joy1.getRawButton(5)) {
			x *= .65;
			y *= .65;
		}
//		if (joy1.getRawButton(3)) allign(reversed?back:front);
//		else if (joy1.getRawButton(1)) allign(reversed?front:back);
		/*else*/ if (joy1.getPOV()>-1) swerve.brake();
		else {
			if (joy1.getRawButton(6)) swerve.carefreeDrive(x, y);
			else swerve.rateDrive(x, y);
			if ((x!=0 || y!=0) && r==0) swerve.angleHoldHere();
			else swerve.rateDrive(r/2);
		}
//		if (joy1.getRawButton(4)) swerve.reset();
		if(joy2.getRawButton(10)){
			feright.disable();
			feleft.disable();
		}
		if(joy2.getRawButton(9)){
			beright.disable();
			beleft.disable();
		}
		if(joy2.getRawButton(8)){
			feright.binHold();
			feleft.binHold();
		}
		if(joy2.getRawButton(7)){
		/*	beright.hold();
			beleft.hold();*/
			beleft.binHold();
			beright.binHold();
		}
		if (joy2.getPOV()==180) {
			if (joy2.getRawButton(5)) {
				beleft.driveUntilStall(-10, 1);
				beright.driveUntilStall(-10, 1);
			} else {
				beleft.set(0);
				beright.set(0);
			}
		} else if (joy2.getPOV()==90) {
			beleft.set(-90);
			beright.set(-90);
		} else if (joy2.getPOV()==270) {
			beleft.set(90);
			beright.set(90);
		} else if (joy2.getPOV()==0) {
			if(joy2.getRawButton(5)){
				beleft.driveUntilStall(180,1);
				beright.driveUntilStall(180,1);
			}
			else{
				beleft.set(175);
				beright.set(175);
			}
		}
		if (joy2.getRawButton(2)) {
			if(joy2.getRawButton(6)){
				feleft.driveUntilStall(-10,1);
				feright.driveUntilStall(-10,1);
			}else{
				feleft.set(0);
				feright.set(0);
			}
		} else if (joy2.getRawButton(3)) {
			feleft.set(90);
			feright.set(90);
		} else if (joy2.getRawButton(1)) {
			feleft.set(-90);
			feright.set(-90);
		} else if (joy2.getRawButton(4)) {
			if(joy2.getRawButton(6)){
				feleft.driveUntilStall(180,1);
				feright.driveUntilStall(180,1);
			}
			else{
				feleft.set(175);
				feright.set(175);
			}
		}
		
		feleft.update();
		feright.update();
		beleft.update();
		beright.update();
    }
    
    final static int DROPOFF = 250,
    		TARGET_DIST = 3000,
    		DEAD_ZONE = 100;
    final static double POWER = .25;
    public boolean allign(Lift lift) {
    	double x = 0, y = 0, rot = 0;
    	double l = lift.getLeftDistance(),
    			r = lift.getRightDistance();
    	boolean lvis = lift.getLeftDistance() > DROPOFF,
    			rvis = lift.getRightDistance() > DROPOFF;
		double le = TARGET_DIST - l,
				re = TARGET_DIST - r;
		if (Math.abs(le) < DEAD_ZONE) le = 0;
		if (Math.abs(re) < DEAD_ZONE) re = 0;
    	if (lvis && !rvis) x = POWER;
    	else if (rvis && !lvis) x = -POWER;
    	else if (rvis && lvis) {
    		y += POWER/2 * Math.signum(le);
    		y += POWER/2 * Math.signum(re);
    		rot += POWER/2 * Math.signum(le);
    		rot -= POWER/2 * Math.signum(re);
    	}
    	if (lift == back) {
    		x *= -1;
    		y *= -1;
    	}
    	swerve.rateDrive(x, y, rot);
    	
    	return x==0 && y==0 && rot==0;
    }
    
}
