package org.usfirst.frc.team1533.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Talon;

public class SwerveModule {
	public final static double STEER_GAIN = -1,
			RAMP = .5,
			STEER_CAP = .6,
			DEAD_ZONE = 1*Math.PI/180,
			I_GAIN = -1;
	public final static int DELAY = 10;
	Talon drive, steer;
	AnalogInput steerEncoder;
	double calib;
	double targetAngle, targetPower, speed;
	Thread t;
	Encoder driveEncoder;
	double x, y, encLast;
	DOF10 dof;
	double mx, my; //module's position relative to center of robot
	double iterm;
	
	public SwerveModule(int driveChannel, int steerChannel, int encoderChannel, double locationX, double locationY, double calibration) {
		drive = new Talon(driveChannel);
		steer = new Talon(steerChannel);
		steerEncoder = new AnalogInput(encoderChannel);
		calib = calibration;
		mx = locationX;
		my = locationY;
		t = new Thread() {
			public void run() {
				while (true) loop();
			}
		};
		t.start();
	}
	
	public SwerveModule(int driveChannel, int steerChannel, int encoderChannel, double locationX, double locationY, double calibration, int encoderA, int encoderB, int encoderX, DOF10 dof) {
		this(driveChannel, steerChannel, encoderChannel, locationX, locationY, calibration);
		driveEncoder = new Encoder(encoderA, encoderB, encoderX);
		driveEncoder.setDistancePerPulse(4*Math.PI/(2048*1.6*2.25));
		this.dof = dof;
	}
	
	private void loop() {
		double dpower, spower, error;
		speed = Math.min(speed + DELAY/1000d/RAMP, targetPower);
//		if (Math.abs(angleError(targetAngle)) > Math.PI)
//			System.out.println(angleError(targetAngle));
		if (Math.abs(angleError(targetAngle)) > Math.PI/2) {
			error = angleError(targetAngle - Math.PI);
			dpower = -speed;
		} else {
			error = angleError(targetAngle);
			dpower = speed;
		}
//		System.out.println(iterm);
//		iterm += error * DELAY / 1000d;
//		iterm = Math.max(-STEER_CAP/I_GAIN, Math.min(STEER_CAP/I_GAIN, iterm));
		spower = error * STEER_GAIN;
//		spower += iterm * I_GAIN;
		spower = Math.max(-STEER_CAP, Math.min(STEER_CAP, spower));
		if (Math.abs(error) < DEAD_ZONE) spower = 0;
		steer.set(spower);
		drive.set(dpower);
		if (driveEncoder != null) {
			double enc = driveEncoder.getDistance();
			double angle = getAngle() + dof.getYaw() * Math.PI / 180 + Math.PI/2;
			x += (enc - encLast) * Math.cos(angle);
			y += (enc - encLast) * Math.sin(angle);
			encLast = enc;
		}
		try {
			Thread.sleep(DELAY);
		} catch (Exception e) {}
	}
	
	public double getAngle() {
		double angle = (steerEncoder.getVoltage()-.2) / 4.6 * (2*Math.PI);
		angle -= calib * Math.PI / 180;
		return wrap(angle);
	}
	
	public double angleError(double target) {
		return wrap(getAngle()-target);
	}
	
	private double wrap(double x) {
		x = ((x + Math.PI) % (2*Math.PI));
		if (x < 0) x += 2*Math.PI;
		return x - Math.PI;
	}
	
	public void set(double angle, double power) {
		if (targetAngle != angle) iterm = 0;
		targetAngle = angle;
		targetPower = power;
	}
	
	public void stopDrive() {
		targetPower = 0;
	}
	
	public void resetPos() {
		x = 0;
		y = 0;
	}
	
}
