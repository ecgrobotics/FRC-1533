package org.usfirst.frc.team1533.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Talon;

public class Eggbeater {
	final static double P_GAIN = -.02,
			D_GAIN = -.0005,
			DEAD_ZONE = 5,
			CAP = 1;
	Talon motor;
	AnalogInput encoder;
	double offset, target = 0;
	double lastAngle;
	long lastTime;
	boolean untilStall=false;
	volatile double stallPower=0;
	public static final long DISABLE_TIME=500;
	public static final long TIMEOUT=4000;
	long stallStart=0;
	boolean left;
	boolean timed=true;
	public Eggbeater(int motor, int encoder, double offset, boolean left) {
		this.motor = new Talon(motor);
		this.encoder = new AnalogInput(encoder);
		this.offset = offset;
		this.left = left;
	}
	
	public void driveUntilStall(double target,double pwr){
		untilStall=true;
		this.target=target;
		stallPower=-pwr*Math.signum(getError(target));
		stallStart=System.currentTimeMillis();
		timed=false;
		binhold=false;
	}
	
	int stallcount=0;
	boolean binhold=false;
	public void update() {
		if(binhold){
			motor.set(.2);
			return;
		}
		//System.out.println(untilStall);
		long currentTime = System.currentTimeMillis();
		double dt = (currentTime-lastTime) / 1000d;
		double error = getError(target);
		double power = error * P_GAIN;
		if (Math.abs(error) > DEAD_ZONE){
			power += D_GAIN * (getError(lastAngle)) / dt;
			
		}
		else{
			if(untilStall){
				untilStall=false;
				stallcount=0;
//				target=getAngle();
			}
		}if(untilStall&&Math.signum(error)==Math.signum(power)){//Stop stalling if gone past dead zone
			untilStall=false;
			stallcount=0;
//			target=getAngle();
		}
		lastTime = currentTime;
		lastAngle = getAngle();
		if(untilStall){
			
			if(Math.abs(getError(lastAngle) / dt) <10){
				stallcount++;
				System.out.println("STALLING!");
			}
			else{
				stallcount=0;
			}
			if(stallcount>=20){
				System.out.println("STOP");
				target=getAngle();
				untilStall=false;
				stallcount=0;
			}
			if(System.currentTimeMillis()-disable>DISABLE_TIME)motor.set(stallPower);
			else motor.disable();
			
			
		}
		else if(System.currentTimeMillis()-disable>DISABLE_TIME && Math.abs(error)>DEAD_ZONE)motor.set(Math.max(-CAP, Math.min(CAP, power)));
		else motor.disable();
		if(!timed&&System.currentTimeMillis()-stallStart>TIMEOUT){
			stallcount=0;
			untilStall=false;
			target=getAngle();
			motor.disable();
			timed = true;
		}
	}
	
	public void set(double angle) {
		target = angle;
		untilStall=false;
		stallcount=0;
		stallStart=System.currentTimeMillis();
		timed=false;
		binhold=false;
	}
	
	public double getAngle() {
		double angle = (encoder.getVoltage() - .2) / 4.6 * 360;
		angle -= offset;
		if (left) angle *= -1;
		return wrapShifted(angle);
	}
	
	private double wrapShifted(double angle) {
		angle = (angle + 90) % 360;
		while (angle < 0) angle += 360;
		return angle - 90;
	}
	public void binHold(){
		binhold=true;
	}
	private double wrapExpanded(double angle) {
		while (angle > 270) angle -= 360;
		while (angle < -270) angle += 360;
		return angle;
	}
	
	private double getError(double target) {
		return wrapExpanded(getAngle()-wrapShifted(target));
	}
	
	public void hold(){
		target=getAngle();
		binhold=false;
	}
	
	long disable;
	public void disable() {
		disable = System.currentTimeMillis();
		hold();
	}
	
}
