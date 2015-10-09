package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.*;

public class Swerve {
    final double STEER_GAIN = 3;
    final int FL = 1,
            FR = 4,
            BL = 2,
            BR = 3;
    final double WIDTH = 23.5,
            LENGTH = 19;
    final double PI = Math.PI;
    double w, l;
    double xp, xn, yp, yn;
    double[] power = new double[5],
            angle = new double[5];
    Talon[] drive = new Talon[5],
            steer = new Talon[5];
    AnalogChannel[] encoder = new AnalogChannel[5];
    double[] angleOffsets = new double[5];
    
    public Swerve() {
        double norm = Math.sqrt(WIDTH*WIDTH + LENGTH*LENGTH);
        w = WIDTH / norm;
        l = LENGTH / norm;
        for (int i = 1; i <= 4; i++) {
            drive[i] = new Talon(i);
            steer[i] = new Talon(i+4);
            encoder[i] = new AnalogChannel(i+1);
            power[i] = 0;
            angle[i] = PI/2;
        }
        angleOffsets[FL] = 35 * 3.14 / 180;
        angleOffsets[FR] = -108 * 3.14 / 180;
        angleOffsets[BL] = 140 * 3.15 / 180;
        angleOffsets[BR] = 74 * 3.14 / 180;
    }
    
    public void drive(double x, double y, double r){
        xp = x + r * l;
        xn = x - r * l;
        yp = y + r * w;
        yn = y - r * w;
        double max = 1;
        double vx, vy;
        for (int i = 1; i <= 4; i++) {
            vx = xp;
            vy = yp;
            if (i == FR)
                vy = yn;
            else if (i == BL)
                vx = xn;
            else if (i == BR) {
                vx = xn;
                vy = yn;
            }
            power[i] = Math.sqrt(vx*vx + vy*vy);
            max = Math.max(max, power[i]);
            if (power[i] > 0)
                angle[i] = MathUtils.atan2(vy, vx);
        }
        for (int i = 1; i <= 4; i++) {
            if (Math.abs(steerError(angle[i], i)) > PI/2 && power[i] > 0) {
                angle[i] += PI;
                power[i] *= -1;
            }
            drive[i].set(power[i]);
            //drive[i].set(0);
            steer(angle[i], i);
        }
    }
    
    public void brake() {
        double a = MathUtils.atan2(l, w);
	drive[FL].set(0);
	drive[FR].set(0);
	drive[BL].set(0);
	drive[BR].set(0);
        steer(angle[FL] = -a, FL);
        steer(angle[FR] = PI+a, FR);
        steer(angle[BL] = a, BL);
        steer(angle[BR] = PI-a, BR);
    }
    
    public void steer(double angle, int module) {
        steer[module].set(Math.max(-1, Math.min(1, STEER_GAIN * steerError(angle, module))));
    }
    
    private double steerError(double targetAngle, int module) {
        double res = (getAngle(module) - targetAngle + PI) % (2 * PI);
        while (res < 0) res += 2*PI;
        return res - PI;
    }
    
    public double getAngle(int module) {
        return (encoder[module].getVoltage()-.2) / 2.3 * PI - angleOffsets[module];
    }
    
    public void returnToHome() {
        for (int i = 1; i <= 4; i++)
            angle[i] = PI/2; 
    }
    
}