package org.usfirst.frc.team1533.robot.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;

/**
 * @author Duncan
 * Implementation of 6127V1A360L.5FS absolute encoder for use with swerve drive.
 */
public class AbsoluteEncoder extends AnalogInput {
	double angleOffset;
	
	/**
	 * @param channel
	 * @param angleOffset zero point of encoder in degrees
	 */
	public AbsoluteEncoder(int channel, double angleOffset) {
		super(channel);
		this.angleOffset = Math.toRadians(angleOffset);
	}
	
	/**
	 * @return angular position of encoder in radians (0 to 2pi)
	 */
	public double getAngle() {
		//convert voltage (0.2-4.8) to radians
		double angle = (getVoltage() - 0.2) * (2*Math.PI) / 4.6;
		return (angle + angleOffset) % (2*Math.PI);
	}
	
	/**
	 * @return result of getAngle()
	 */
	public double pidGet() {
		return getAngle();
	}
	
}
