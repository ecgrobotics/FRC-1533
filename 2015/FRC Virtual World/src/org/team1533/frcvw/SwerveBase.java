package org.team1533.frcvw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;


public class SwerveBase extends RobotBase {
	static final double STEER_SPEED = 15,
			DRIVE_SPEED = 150;
	double[] steer = new double[4]; {
		for (int i = 0; i < 4; i++)
			steer[i] = Math.PI/2;
	}
	
	void paint(Graphics2D g) {
		Vector l = forward.multiply((double)LENGTH/2),
				w = forward.perpendicular().multiply((double)WIDTH/2);
//		for (double x : motors) System.out.print(""+x+" ");
//		System.out.println();
		g.setColor(Color.black);
		g.setStroke(new BasicStroke(3f));
		Vector v1, v2;
		int count = 0;
		for (int i = 1; i >= -1; i -= 2) {
			for (int j = -1; j <= 1; j += 2) {
				g.setColor(Color.black);
				v1 = center.add(l.multiply(i)).add(w.multiply(j)).multiply(2);
				g.fillOval(v1.intX()-5, v1.intY()-5, 11, 11);
				v2 = Vector.polar(1, steer[count]+forward.perpendicular().angle()).multiply(20).add(v1);
				g.drawLine(v1.intX(), v1.intY(), v2.intX(), v2.intY());
				v2 = Vector.polar(RobotWorld.world.disabled?0:motors[count], steer[count]+forward.perpendicular().angle()).multiply(20).add(v1);
				g.setColor(Color.red);
				g.drawLine(v1.intX(), v1.intY(), v2.intX(), v2.intY());
				count++;
			}
		}
		g.setColor(Color.black);
		v1 = center.add(l).multiply(2).add(forward.multiply(8));
		v2 = center.multiply(2);
		g.drawLine(v1.intX(), v1.intY(), v2.intX(), v2.intY());
		v2 = v1.subtract(forward.multiply(8)).subtract(forward.perpendicular().multiply(6));
		g.drawLine(v1.intX(), v1.intY(), v2.intX(), v2.intY());
		v2 = v2.add(forward.perpendicular().multiply(12));
		g.drawLine(v1.intX(), v1.intY(), v2.intX(), v2.intY());
		v1 = center.add(forward.multiply(28)).multiply(2);
//		g.fillOval(v1.intX()-5, v1.intY()-5, 11, 11);
	}

	void physics(double dt) {
		super.physics(dt);
		Vector l = forward.multiply((double)LENGTH/2),
				w = forward.perpendicular().multiply((double)WIDTH/2);
		Vector[] pos = {
			center.add(l).subtract(w),
			center.add(l).add(w),
			center.subtract(l).subtract(w),
			center.subtract(l).add(w)
		};
		Vector[] old = new Vector[4];
		Vector ctemp = Vector.zero();
		for (int i = 0; i < 4; i++) {
			old[i] = pos[i].clone();
			if (!RobotWorld.world.disabled) {
				steer[i] += STEER_SPEED * motors[i+4] * dt;
//				if (i != 1)
				pos[i] = pos[i].add(Vector.polar(motors[i], steer[i]+forward.perpendicular().angle()).multiply(DRIVE_SPEED*dt));
			}
			ctemp = ctemp.add(pos[i]);
			analog[i] = .2 + 4.6 * ((steer[i]-Math.PI/2) % (2*Math.PI)) / (2*Math.PI);
		}
		center = ctemp.divide(4);
		forward = pos[0].add(pos[1]).subtract(pos[2]).subtract(pos[3]).normalize();
		l = forward.multiply((double)LENGTH/2);
		w = forward.perpendicular().multiply((double)WIDTH/2);
		pos = new Vector[] {
			center.add(l).subtract(w),
			center.add(l).add(w),
			center.subtract(l).subtract(w),
			center.subtract(l).add(w)
		};
		center.x = Math.max(-RobotWorld.WIDTH/4, Math.min(RobotWorld.WIDTH/4, center.x));
		center.y = Math.max(-RobotWorld.HEIGHT/4, Math.min(RobotWorld.HEIGHT/4, center.y));
		double last;
		for (int i = 0; i < 4; i++) {
			last = encoders[i];
			encoders[i] += Vector.polar(1, steer[i]+forward.perpendicular().angle()).dot(pos[i].subtract(old[i]));
			encoderRate[i] = (encoders[i] - last) / dt;
		}
	}
	
}
