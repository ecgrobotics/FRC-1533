package org.usfirst.frc.team1533.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.hal.CanTalonSRX;

public class Lift {
	SpeedController lift;
	Encoder encoder;
	int rightDist,leftDist=0;
	Thread t;
	long DELAY=10;
	public static final int AUTO=1;
	public static final int MANUAL=0;
	volatile int mode=MANUAL;
	volatile double target=0;
	volatile double ERROR=.1;
	volatile double E=0;
	volatile double pwr=0;
	volatile double KP=-1;
	public static final double MAX=45.75;
	public static final double MIN=.1;
	public Lift(SpeedController lift, int encoder,int rightDist, int leftDist){
		this.lift=lift;
		this.rightDist=rightDist;
		this.leftDist=leftDist;
		this.encoder=new Encoder(encoder,encoder+1);
		this.encoder.setDistancePerPulse(.01113);
		this.encoder.reset();
		t=new Thread(){
			public void run(){
				while(true){
					loop();					
					try{
						Thread.sleep(DELAY);
					}catch(Exception e){
						
					}
				}
			}
		};
		t.start();
		
	}	
	public void loop(){
		E=target-getHeight();
		if(target<0)target=0;
		if(target>MAX)target=MAX;
		if((getHeight()<=MIN&&pwr>0)||(getHeight()>=MAX&&pwr<0)){
			lift.set(0);
			return;
		}
		
		if(mode==MANUAL&&Math.abs(pwr)>=.05){
			
			lift.set(pwr);
			target=encoder.getDistance();
		}
		else{
			if(Math.abs(E)>ERROR){
				lift.set(KP*E);
			}
			else lift.set(0);
		}
	}
	public double getLeftDistance(){
		return Robot.adc.getAnalog(leftDist);
	}
	public double getRightDistance(){
		return Robot.adc.getAnalog(rightDist);
	}
	public void setPower(double d){
		lift.set(d);
	}
	public double getHeight(){
		return encoder.getDistance();//TODO some mathy math here to get height
	}
	public int getEncoderValue(){
		return encoder.get();
	}
	public double getSpeed(){
		return encoder.getRate();
	}
	public void goToHeight(double target){
		this.target=target;
		this.mode=AUTO;
	}
	public void stopAuto(){
		this.mode=MANUAL;
	}
	/**
	 * If abs(pwr)>.1, automatically enables 
	 * @param pwr
	 */
	public void set(double pwr){
		if(Math.abs(pwr)>.1)mode=MANUAL;
		if(mode==MANUAL)this.pwr=pwr;		
	}
	
}