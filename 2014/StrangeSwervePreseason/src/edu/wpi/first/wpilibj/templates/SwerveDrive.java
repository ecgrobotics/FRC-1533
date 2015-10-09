/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.Gyro;

public class SwerveDrive {
	static final int FL = 0,
			FR = 1,
			BL = 2,
			BR = 3;
	static final int RATE = 0,
			POS = 1,
			FREE = 2,
			BRAKE = 3;
	static final double ROTATE_GAIN = .1,
			TRANS_P_GAIN = .1;
	static final int DELAY = 10,
			TIMEOUT = 1000;
	private double w, l, nw, nl;
	private SwerveModule[] modules = new SwerveModule[4];
	private Gyro gyro;
	private double xRate, yRate, rRate;
	private double xPos, yPos, rPos;
	private double degreesPerDist, headAngle;
	private int transMode, angleMode;
	private long lastTransCommand, lastAngleCommand;
	private Thread t;
	
	public SwerveDrive(double chassisWidth, double chassisLength, Gyro g) {
		w = chassisWidth;
		l = chassisLength;
//		double r = Math.hypot(w, l);
                double r = Math.sqrt(w*w + l*l);
		nw = w / r;
		nl = l / r;
		gyro = g;
		modules[FL] = new SwerveModule(1, 5, 2, 123, 1, 2, g);
		modules[BL] = new SwerveModule(2, 6, 3, -130);
		modules[BR] = new SwerveModule(3, 7, 4, 160);
		modules[FR] = new SwerveModule(4, 8, 5, -20);
		t = new Thread() {
			public void run() {
				while (true) {
					loop();
					try {
						Thread.sleep(DELAY);
					} catch (InterruptedException e) {}
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
		double[] angle = new double[4],
				power = new double[4];
		double cx, cy, maxPower = 1;
		for (int i = 0; i < 4; i++) {
			if (i == FL || i == FR) cx = x + r*nl;
			else cx = x - r*nl;
			if (i == FL || i == BL) cy = y + r*nw;
			else cy = y - r*nw;
//			power[i] = Math.hypot(cx, cy);
                        power[i] = Math.sqrt(cx*cx + cy*cy);
			angle[i] = MathUtils.atan2(cy, cx) - Math.PI/2;
			maxPower = Math.max(power[i], maxPower);
		}
		for (int i = 0; i < 4; i++) 
			if (Math.abs(power[i]) < .01) 
				modules[i].stopDrive();
			else
				modules[i].set(angle[i], power[i]/maxPower);
	}
	
	private void setBrakeMode() {
		double sign, angle = MathUtils.atan2(nw, nl);
		for (int i = 0; i < 4; i++) {
			if (i == FL || i == BR) sign = 1;
			else sign = -1;
			modules[i].set(angle * sign, 0);
		}
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
		return modules[FL].x + w/2*(Math.cos(angle)-1) - l/2*Math.sin(angle);
	}
	
	public double getPosY() {
		double angle = gyro.getAngle() * Math.PI / 180;
		return modules[FL].y - l/2*(Math.cos(angle)-1) - w/2*Math.sin(angle);
	}
	
	public double dist(double x, double y) {
//		return Math.hypot(x-getPosX(), y-getPosY());
            x -= getPosX();
            y -= getPosY();
            return Math.sqrt(x*x + y+y);
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
	
}
