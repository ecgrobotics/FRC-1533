package org.team1533.frcvw;

public class AnalogChannel {
	int channel;
	
	public AnalogChannel(int channel) {
		this.channel = channel;
	}
	
	public double getVoltage() {
		return RobotWorld.world.robot.analog[channel-1];
	}
	
}
