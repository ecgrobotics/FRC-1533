package org.team1533.frcvw;

import java.awt.Graphics2D;

abstract class RobotBase {
	public static int WIDTH = 28,
			LENGTH = 28;
	Vector center = Vector.zero(), 
			forward = Vector.vertical();
	double size = 1;
	double[] motors = new double[8];
	double[] analog = new double[4];
	double[] encoders = new double[4];
	double[] encoderRate = new double[4];
	double gyro, gyroRate;
	double lastAngle = forward.angleDegrees();
	
	public static void main(String[] args) {
//		new Console();
		try {
			String className  = System.getProperty("sun.java.command");
			System.out.println(className);
			Class<?> c = Class.forName(className);
			RobotBase robot = (RobotBase) c.newInstance();
			RobotWorld.start(robot);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void setMotorPower(int port, double power) {
		if (port < 1 || port > 8) return;
		motors[port-1] = Math.max(-1, Math.min(1, power));
	}
	
	double getMotorPower(int port) {
		if (port < 1 || port > 8) return 0;
		return motors[port-1];
	}
	
	double getAnalog(int channel) {
		if (channel < 1 || channel > 4) return 0;
		return analog[channel-1];
	}
	
	double getEncoder(int aChannel) {
		if (aChannel/2 < 0 || aChannel/2 > 3) return 0;
		return encoders[aChannel/2];
	}
	
	double getEncoderRate(int aChannel) {
		if (aChannel/2 < 0 || aChannel/2 > 3) return 0;
		return encoderRate[aChannel/2];
	}
	
	abstract void paint(Graphics2D g);
	void physics(double dt) {
		double angle = forward.angleDegrees();
		gyro += lastAngle - angle;
		gyroRate = (lastAngle - angle) / dt;
		lastAngle = angle;
		if (RobotWorld.world.disabled)
			for (int i = 0; i < 8; i++) motors[i] = 0;
	}
	
	public void robotInit() {}
	public void teleopInit() {}
	public void teleopPeriodic() {}
	public void autonomousInit() {}
	public void autonomousPeriodic() {}
	public void disabledInit() {}
	public void disabledPeriodic() {}
	
}
