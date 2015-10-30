package org.team1533.frcvw;

public class Jaguar {
	int port;
	
	public Jaguar(int channel) {
		this.port = channel;
	}
	
	public void set(double power) {
		RobotWorld.world.robot.setMotorPower(port, power);
	}
	
	public double get() {
		return RobotWorld.world.robot.getMotorPower(port);
	}
	
}
