package org.usfirst.frc.team1533.robot;

public class Constants {
	
	public static double SWERVE_STEER_P = 0;
	public static double SWERVE_STEER_I = 0;
	public static double SWERVE_STEER_D = 0;
	public static double SWERVE_STEER_CAP = 1; //speed limit on the steering motor
	
	//swerve module encoder offsets (degrees)
	public static double FL_ENC_OFFSET = 0; //front left
	public static double FR_ENC_OFFSET = 0; //front right
	public static double BL_ENC_OFFSET = 0; //back left
	public static double BR_ENC_OFFSET = 0; //back right
	
	//wheel base dimensions measures where the wheels touch the ground
	public static double WHEEL_BASE_WIDTH = 24;
	public static double WHEEL_BASE_LENGTH = 24;
	
}
