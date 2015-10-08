package org.usfirst.frc.team1533.robot;

import edu.wpi.first.wpilibj.Encoder;

public class Autonomous {
	static final double OMEGA_P = .04,
			OMEGA_I = .0075,
			OMEGA_CAP = 80,
			ANGLE_P = 10,
			ANGLE_ERROR = 0;
	SwerveDrive swerve;
	DOF10 dof;
	Encoder enc;
	Robot robot;
	
	public Autonomous(Robot robot, SwerveDrive swerve, DOF10 dof, Encoder enc) {
		this.swerve = swerve;
		this.dof = dof;
		this.enc = enc;
		this.robot = robot;
		enc.setDistancePerPulse(4*Math.PI/(2048*1.6*2.25)*1.085);
	}
	
	public void rotate(double angle) {
//		long start = System.currentTimeMillis();
		swerve.rateDrive(0, 0);
//		dof.resetYaw();
		double setpoint = dof.getYaw();
		double speed;
		double omegaTar, omegaError;
		double oi = 0;
		while (Math.abs(dof.getYaw()-setpoint) < Math.abs(angle)-ANGLE_ERROR && robot.isAutonomous() && !robot.isDisabled() && !Thread.currentThread().isInterrupted()) {
//		while (true) {
			omegaTar = -(dof.getYaw()-setpoint-angle) * ANGLE_P;
			omegaTar = Math.max(-OMEGA_CAP, Math.min(OMEGA_CAP, omegaTar));
			omegaError = dof.getdYaw()-omegaTar;
			oi += omegaError * .01;
//			System.out.println(dof.getdYaw());
			swerve.rateDrive(omegaError*OMEGA_P + oi*OMEGA_I);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
		swerve.brake();
	}
	
	public void move(double x, double y, double dist, double dir, boolean brake) {
		double px, py;
		enc.reset();
//		dof.resetYaw();
		swerve.angleHold(dir);
		while (Math.abs(enc.getDistance()) < Math.abs(dist) && robot.isAutonomous() && !robot.isDisabled() && !Thread.currentThread().isInterrupted()) {
			swerve.rateDrive(x, y);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
		if (brake) swerve.brake();
		else swerve.rateDrive(0, 0, 0);
	}
	
	public void allign(Lift lift) {
		while (!robot.allign(lift) && robot.isAutonomous() && !robot.isDisabled() && !Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
		swerve.rateDrive(0, 0, 0);
	}
	
}
