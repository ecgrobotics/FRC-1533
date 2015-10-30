package test;

import org.team1533.frcvw.Gyro;

public class SwerveDrive {
	static final int FL = 0,
			FR = 1,
			BL = 2,
			BR = 3;
	static final int RATE = 0,
			POS = 1,
			FREE = 2,
			BRAKE = 3;
	static final double ROTATE_GAIN = .025,
			TRANS_P_GAIN = .1;
	static final int DELAY = 10,
			TIMEOUT = 1000;
	private double w, l;
	private SwerveModule[] modules = new SwerveModule[4];
	private SwerveModule encoderModule;
	private Gyro gyro;
	private double xRate, yRate, rRate;
	private double xPos, yPos, rPos;
	private double degreesPerDist, headAngle;
	private double pivotX, pivotY;
	private int transMode, angleMode;
	private long lastTransCommand, lastAngleCommand;
	private Thread t;
	
	public SwerveDrive(double chassisWidth, double chassisLength, Gyro g) {
		w = chassisWidth;
		l = chassisLength;
		double r = Math.hypot(w, l);
		gyro = g;
		modules[FL] = new SwerveModule(1, 5, 1, -w/2, l/2, 0, 1, 2, g);
		modules[FR] = new SwerveModule(2, 6, 2, w/2, l/2, 0);
		modules[BL] = new SwerveModule(3, 7, 3, -w/2, -l/2, 0);
		modules[BR] = new SwerveModule(4, 8, 4, w/2, -l/2, 0);
		encoderModule = modules[FL];
		t = new Thread() {
			public void run() {
				while (true) {
					loop();
					try {
						Thread.sleep(DELAY);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
	
	private void loop() {
		double r = 0;
		double angleTar = rPos;
		if (transMode == BRAKE) {
			setBrakeMode();
			return;
		}
		if (transMode == POS && angleMode == POS)
			angleTar += degreesPerDist * dist(xPos, yPos);
		switch (angleMode) {
		case RATE:
			if (System.currentTimeMillis()-lastAngleCommand > TIMEOUT) rRate = 0;
			r = rRate;
			break;
		case POS:
			r = angleError(angleTar) * ROTATE_GAIN;
		}
		if (angleMode == RATE || angleMode == FREE) {
			if (System.currentTimeMillis()-lastTransCommand > TIMEOUT) {
				xRate = 0;
				yRate = 0;
			}
		}
		switch (transMode) {
		case RATE:
			set(xRate, yRate, r);
			break;
		case POS:
			xRate = (xPos-getPosX()) * TRANS_P_GAIN;
			yRate = (yPos-getPosY()) * TRANS_P_GAIN;
			headAngle = 0;
		case FREE:
			setCarefree(xRate, yRate, r, headAngle);
		}
	}
	
	private void set(double x, double y, double r) {
		double m = Math.max(1, Math.max(Math.abs(x), Math.abs(y)));
		x /= m;
		y /= m;
		r = Math.max(-1, Math.min(1, r));
		double[] angle = new double[modules.length],
				power = new double[modules.length],
				sx = new double[modules.length],
				sy = new double[modules.length];
		double maxSteer = 0, maxPower = 1;
		for (int i = 0; i < modules.length; i++) {
			sx[i] = modules[i].my - pivotY;
			sy[i] = -modules[i].mx + pivotX;
			maxSteer = Math.max(maxSteer, Math.hypot(sx[i], sy[i]));
		}
		for (int i = 0; i < modules.length; i++) {
			sx[i] *= r / maxSteer;
			sy[i] *= r / maxSteer;
			power[i] = Math.hypot(x+sx[i], y+sy[i]);
			angle[i] = Math.atan2(y+sy[i], x+sx[i]) - Math.PI/2;
			maxPower = Math.max(power[i], maxPower);
		}
		for (int i = 0; i < modules.length; i++) {
			if (Math.abs(power[i]) < .01)
				modules[i].stopDrive();
			else
				modules[i].set(angle[i], power[i]/maxPower);
		}
	}
	
	private void setBrakeMode() {
		for (int i = 0; i < 4; i++)
			modules[i].set(Math.atan2(-modules[i].mx, modules[i].my), 0);
	}
	
	private void setCarefree(double x, double y, double r, double forwardAngle) {
		double tempx = x;
		double angle = (gyro.getAngle()-forwardAngle) * Math.PI / 180;
		x = x*Math.cos(angle) - y*Math.sin(angle);
		y = tempx*Math.sin(angle) + y*Math.cos(angle);
		set(x, y, r);
	}
	
	public void reset() {
		modules[FL].resetPos();
		gyro.reset();
	}
	
	public double getPosX() {
		double angle = gyro.getAngle() * Math.PI / 180;
		return encoderModule.x - encoderModule.mx*(Math.cos(angle)-1) - encoderModule.my*Math.sin(angle);
	}
	
	public double getPosY() {
		double angle = gyro.getAngle() * Math.PI / 180;
		return encoderModule.y - encoderModule.my*(Math.cos(angle)-1) + encoderModule.mx*Math.sin(angle);
	}
	
	public double dist(double x, double y) {
		return Math.hypot(x-getPosX(), y-getPosY());
	}
	
	public double wrapDegrees(double angle) {
		angle = (angle + 180) % 360;
		if (angle < 0) angle += 360;
		return angle - 180;
	}
	
	public double angleError(double angle) {
		return wrapDegrees(angle-gyro.getAngle());
	}
	
	public void rateDrive(double x, double y, double r) {
		rateDrive(x, y);
		rateDrive(r);
	}
	
	public void rateDrive(double x, double y) {
		transMode = RATE;
		xRate = x;
		yRate = y;
		lastTransCommand = System.currentTimeMillis();
	}
	
	public void rateDrive(double r) {
		angleMode = RATE;
		rRate = r;
		lastAngleCommand = System.currentTimeMillis();
	}
	
	public void carefreeDrive(double x, double y, double heading) {
		transMode = FREE;
		xRate = x;
		yRate = y;
		lastTransCommand = System.currentTimeMillis();
	}
	
	public void carefreeDrive(double x, double y) {
		if (transMode != FREE) headAngle = gyro.getAngle();
		carefreeDrive(x, y, headAngle);
	}
	
	public void angleHold(double angle) {
		rPos = angle;
		angleMode = POS;
	}
	
	public void angleHoldHere() {
		if (angleMode != POS) angleHold(gyro.getAngle());
		else angleHold(rPos);
	}
	
	public void posHold(double x, double y) {
		transMode = POS;
		xPos = x;
		yPos = y;
	}
	
	public void posHold(double x, double y, double angle) {
		if (transMode != POS || xPos != x || yPos != y || rPos != angle) {
			double dist = dist(x, y);
			if (dist < 10) degreesPerDist = 0;			
			else degreesPerDist = -angleError(angle) / dist(x, y);
		}
		angleMode = POS;
		rPos = angle;
		posHold(x, y);
	}
	
	public void brake() {
		transMode = BRAKE;
	}
	
	public void setPivot(double x, double y) {
		pivotX = x;
		pivotY = y;
	}
	
}
