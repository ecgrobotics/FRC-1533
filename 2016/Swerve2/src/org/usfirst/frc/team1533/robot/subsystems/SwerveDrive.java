package org.usfirst.frc.team1533.robot.subsystems;

import org.usfirst.frc.team1533.robot.Constants;
import org.usfirst.frc.team1533.robot.RobotMap;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * @author Duncan
 *
 */
public class SwerveDrive extends Subsystem {
	double pivotX, pivotY;
	SwerveModule[] modules;
	
	/**
	 * Custom constructor for current robot.
	 */
	public SwerveDrive() {
		//initialize array of modules
		//array can be any size, as long as the position of each module is specified in its constructor
		modules = new SwerveModule[] {
			//front left
			new SwerveModule(new Talon(RobotMap.FL_DRIVE),
					new Talon(RobotMap.FL_STEER),
					new AbsoluteEncoder(RobotMap.FL_ENCODER, Constants.FL_ENC_OFFSET),
					-Constants.WHEEL_BASE_WIDTH/2,
					Constants.WHEEL_BASE_LENGTH/2
					),
			//front right
			new SwerveModule(new Talon(RobotMap.FR_DRIVE), 
					new Talon(RobotMap.FR_STEER),
					new AbsoluteEncoder(RobotMap.FR_ENCODER, Constants.FR_ENC_OFFSET),
					Constants.WHEEL_BASE_WIDTH/2,
					Constants.WHEEL_BASE_LENGTH/2
					),
			//back left
			new SwerveModule(new Talon(RobotMap.BL_DRIVE),
					new Talon(RobotMap.BL_STEER),
					new AbsoluteEncoder(RobotMap.BL_ENCODER, Constants.BL_ENC_OFFSET),
					-Constants.WHEEL_BASE_WIDTH/2,
					-Constants.WHEEL_BASE_LENGTH/2
					),
			//back right
			new SwerveModule(new Talon(RobotMap.BR_DRIVE), 
					new Talon(RobotMap.BR_STEER),
					new AbsoluteEncoder(RobotMap.BR_ENCODER, Constants.BR_ENC_OFFSET),
					Constants.WHEEL_BASE_WIDTH/2,
					-Constants.WHEEL_BASE_LENGTH/2
					)
		};
	}
	
	/**
	 * @param pivotX x coordinate in inches of pivot point relative to center of robot
	 * @param pivotY y coordinate in inches of pivot point relative to center of robot
	 */
	public void setPivot(double pivotX, double pivotY) {
		this.pivotX = pivotX;
		this.pivotY = pivotY;
	}
	
	/**
	 * Drive with field oriented capability
	 * @param translationX relative speed in left/right direction (-1 to 1)
	 * @param translationY relative speed in forward/reverse direction (-1 to 1)
	 * @param rotation relative rate of rotation around pivot point (-1 to 1) positive is clockwise
	 * @param heading offset in heading in radians (used for field oriented control)
	 */
	private void driveWithOrient(double translationX, double translationY, double rotation, double heading) {
		Vector[] vects = new Vector[modules.length];
		Vector transVect = new Vector(translationX, translationY),
				pivotVect = new Vector(pivotX, pivotY);
		
		//if there is only one module ignore rotation
		if (modules.length < 2)
			for (SwerveModule module : modules) 
				module.set(transVect.getAngle(), Math.min(1, transVect.getMagnitude())); //cap magnitude at 1

		double maxDist = 0;
		for (int i = 0; i < modules.length; i++) {
			vects[i] = new Vector(modules[i].positionX, modules[i].positionY);
			vects[i].subtract(pivotVect); //calculate module's position relative to pivot point
			maxDist = Math.max(maxDist, vects[i].getMagnitude()); //find farthest distance from pivot
		}
		
		double maxPower = 1;
		for (int i = 0; i < modules.length; i++) {
			//rotation motion created by driving each module perpendicular to
			//the vector from the pivot point
			vects[i].makePerpendicular();
			//scale by relative rate and normalize to the farthest module
			//i.e. the farthest module drives with power equal to 'rotation' variable
			vects[i].scale(rotation / maxDist);
			vects[i].add(transVect);
			//calculate largest power assigned to modules
			//if any exceed 100%, all must be scale down
			maxPower = Math.max(maxPower, vects[i].getMagnitude());
		}
		
		double power;
		for (int i = 0; i < modules.length; i++) {
			power = vects[i].getMagnitude() / maxPower; //scale down by the largest power that exceeds 100%
			if (power > .05) {
				modules[i].set(vects[i].getAngle()-Math.PI/2, power);
			} else {
				modules[i].rest();
			}
		}
	}
	
	/**
	 * Regular robot oriented control.
	 * @param translationX relative speed in left/right direction (-1 to 1)
	 * @param translationY relative speed in forward/reverse direction (-1 to 1)
	 * @param rotation relative rate of rotation around pivot point (-1 to 1) positive is clockwise
	 */
	public void driveNormal(double translationX, double translationY, double rotation) {
		driveWithOrient(translationX, translationY, rotation, 0);
	}
	
	public void enable() {
		for (SwerveModule module : modules) module.enable();
	}
	
	public void disable() {
		for (SwerveModule module : modules) module.disable();
	}

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    /**
     * 2D Mathematical Vector
     */
	private class Vector {
		double x = 0, y = 0;

		public Vector(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getAngle() {
			return Math.atan2(y, x);
		}

		public double getMagnitude() {
			return Math.hypot(x, y);
		}

		public void scale(double scalar) {
			x *= scalar;
			y *= scalar;
		}

		public void add(Vector v) {
			x += v.x;
			y += v.y;
		}
		
		public void subtract(Vector v) {
			x -= v.x;
			y -= v.y;
		}
		
		public void makePerpendicular() {
			double temp = x;
			x = y;
			y = -temp;
		}
	}
}

