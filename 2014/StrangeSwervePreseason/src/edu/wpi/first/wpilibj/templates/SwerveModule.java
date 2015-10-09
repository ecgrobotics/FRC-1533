package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.*;

public class SwerveModule {
	public final static double STEER_GAIN = 2,
			RAMP = .5;
	public final static int DELAY = 10;
	Jaguar drive, steer;
	AnalogChannel steerEncoder;
	double calib;
	double targetAngle, targetPower, speed;
	Thread t;
	double nx, ny;
	Encoder driveEncoder;
	double x, y, encLast;
	Gyro gyro;
	
	public SwerveModule(int driveChannel, int steerChannel, int encoderChannel, double calibration) {
		drive = new Jaguar(driveChannel);
		steer = new Jaguar(steerChannel);
		steerEncoder = new AnalogChannel(encoderChannel);
		calib = calibration;
		t = new Thread() {
			public void run() {
				while (true) loop();
			}
		};
		t.start();
	}
	
	public SwerveModule(int driveChannel, int steerChannel, int encoderChannel, double calibration, int encoderA, int encoderB, Gyro g) {
		this(driveChannel, steerChannel, encoderChannel, calibration);
		driveEncoder = new Encoder(encoderA, encoderB);
		gyro = g;
	}
	
	private void loop() {
		speed = Math.min(speed + DELAY/1000d/RAMP, targetPower);
		if (Math.abs(angleError(targetAngle)) > Math.PI)
			System.out.println(angleError(targetAngle));
		if (Math.abs(angleError(targetAngle)) > Math.PI/2) {
			steer.set(STEER_GAIN * angleError(targetAngle - Math.PI));
			drive.set(-speed);
		} else {
			steer.set(STEER_GAIN * angleError(targetAngle));
			drive.set(speed);
		}
		if (driveEncoder != null) {
			double enc = driveEncoder.get();
			double angle = getAngle() - gyro.getAngle() * Math.PI / 180 + Math.PI/2;
			x += (enc - encLast) * Math.cos(angle);
			y += (enc - encLast) * Math.sin(angle);
			encLast = enc;
		}
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {}
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
