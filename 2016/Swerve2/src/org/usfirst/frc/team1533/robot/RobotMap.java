package org.usfirst.frc.team1533.robot;
/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
    // For example to map the left and right motors, you could define the
    // following variables to use with your drivetrain subsystem.
    // public static int leftMotor = 1;
    // public static int rightMotor = 2;
    
    // If you are using multiple modules, make sure to define both the port
    // number and the module. For example you with a rangefinder:
    // public static int rangefinderPort = 1;
    // public static int rangefinderModule = 1;

	public static int FL_STEER = 4; //front left
	public static int FR_STEER = 5; //front right
	public static int BL_STEER = 6; //back left
	public static int BR_STEER = 7;	//back right
	
	public static int FL_DRIVE = 0;
	public static int FR_DRIVE = 1;
	public static int BL_DRIVE = 2;
	public static int BR_DRIVE = 3;

	public static int FL_ENCODER = 0;
	public static int FR_ENCODER = 1;
	public static int BL_ENCODER = 2;
	public static int BR_ENCODER = 3;
	
	
}
