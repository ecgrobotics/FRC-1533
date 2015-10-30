package org.team1533.frcvw;

public class Encoder {
	int a;
	
	public Encoder(int aChannel, int bChannel) {
		a = aChannel;
	}
	
	public double get() {
		return RobotWorld.world.robot.getEncoder(a);
	}
	
	public double getRate() {
		return RobotWorld.world.robot.getEncoderRate(a);
	}
	
}
