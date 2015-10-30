package test;

import org.team1533.frcvw.*;

public class RobotMain extends SwerveBase {
	SwerveDrive swerve;
	Joystick joy;
	Gyro gyro;
	
	public void robotInit() {
		gyro = new Gyro(1);
		swerve = new SwerveDrive(WIDTH, LENGTH, gyro);
		joy = new Joystick(1);
	}
	
	public void teleopInit() {
//		swerve.setPivot(0, 28)
	}
	
	public void teleopPeriodic() {
		double x, y, r;
		x = joy.getX();
		y = -joy.getY();
		r = joy.getZ();
		if (Math.abs(x) < .1) x = 0;
		if (Math.abs(y) < .1) y = 0;
		if (Math.abs(r) < .1) r = 0;
		if (r==0) swerve.angleHoldHere();
		else swerve.rateDrive(r);
		if (joy.getRawButton(6)) swerve.carefreeDrive(x, y, 0);
		else swerve.rateDrive(x, y);
		if (joy.getRawButton(2)) swerve.brake();
//		swerve.posHold(x*324, y*148);
//		swerve.angleHold(r*180);
//		swerve.rateDrive(r);
	}
	
	public void autonomousInit() {
		swerve.posHold(120, -70, 90);
	}
	
	public void autonomousPeriodic() {
		System.out.println(swerve.getPosX()+", "+swerve.getPosY());
		if (swerve.dist(120, -70) < .1 && Math.abs(swerve.angleError(90)) < .1)
			swerve.brake();
	}
	
}
