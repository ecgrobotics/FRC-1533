package org.usfirst.frc.team1533.robot;
/**
 * Nanosecond Timer
 * @author Programming
 *
 */
public class NSTimer {
	long start=0;
	public void start(){
		start=System.nanoTime();
	}
	public long get(){
		return System.nanoTime()-start;
	}
	public double getDouble(){
		return get()*1e-9;
	}
	public void restart(){
		start();
	}
}
