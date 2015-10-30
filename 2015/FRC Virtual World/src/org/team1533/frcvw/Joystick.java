package org.team1533.frcvw;

import java.util.ArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;


public class Joystick {
	private int port;
	private Controller c;
	private static final String[] axes = {
		"X Axis", "Y Axis", "Z Axis", "Z Rotation"
	};
	
	public Joystick(int port) {
		this.port = port;
	}
	
	private boolean refresh() {
		ArrayList<Controller> joysticks = RobotWorld.world.joysticks;
		if (joysticks.size() >= port) {
			c = joysticks.get(port-1);
			return true;
		}
		c = null;
		return false;
	}
	
	public boolean getRawButton(int button) {
		if (refresh()) {
			for (Component co : c.getComponents()) {
				if (co.getName().equals("Button " + (button-1))) {
					return co.getPollData() > 0;
				}
			}
		}
		return false;
	}
	
	public double getRawAxis(int axis) {
		if (axis < 1 || axis > 4) return 0; 
		if (refresh()) {
			for (Component co : c.getComponents()) {
				if (co.getName().equals(axes[axis-1])){
					return co.getPollData();
				}
			}
		}
		return 0;
	}
	
	public double getX() {
		return getRawAxis(1);
	}
	
	public double getY() {
		return getRawAxis(2);
	}
	
	public double getZ() {
		return getRawAxis(3);
	}
	
}
