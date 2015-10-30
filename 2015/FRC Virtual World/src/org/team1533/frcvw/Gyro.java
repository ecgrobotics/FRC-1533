package org.team1533.frcvw;

public class Gyro {
	
	public Gyro(int channel) {}
	
	public double getAngle() {
		return RobotWorld.world.robot.gyro;
	}
	
	public double getRate() {
		return RobotWorld.world.robot.gyroRate;
	}
	
	public void reset() {
		RobotWorld.world.robot.gyro = 0;
	}
	
}
