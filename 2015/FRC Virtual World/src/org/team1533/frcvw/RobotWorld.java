package org.team1533.frcvw;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

import javax.swing.*;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Controller.Type;

class RobotWorld implements MouseListener, ComponentListener {
	static final int G_DELAY = 10,
			P_DELAY = 1,
			RC_DELAY = 1;
	static final int WIDTH = 1296,
			HEIGHT = 592;
	static final double PPI = 2;
	public static RobotWorld world;
	RobotBase robot;
	JFrame frame;
	JPanel panel;
	Thread gThread, pThread, rcThread;
	ArrayList<Controller> joysticks = new ArrayList<Controller>();
	boolean teleop = true, disabled = false;
	JFrame sFrame;
	JPanel sPanel;
	JButton enable, disable, auto, tele;
	JLabel label1, label2;
	VolatileImage vi;
	boolean painting = false;
	
	public RobotWorld(RobotBase robot) {
		this.robot = robot;

		frame = new JFrame("Swerve Sim");
		panel = new JPanel();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setPreferredSize(new Dimension((int) (1296*1.25), (int) (592*1.25)));
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		panel.addMouseListener(this);
		
		vi = panel.createVolatileImage((int)(WIDTH*PPI), (int)(HEIGHT*PPI));
		
		sFrame = new JFrame("Control Panel");
		sPanel = new JPanel();
		sPanel.setLayout(new GridLayout(3, 1));
		JPanel temp;
		
		temp = new JPanel();
		temp.setLayout(new GridLayout(2, 1));
		label1 = new JLabel("Teleop");
		label2 = new JLabel("Disabled");
		Font f = label1.getFont().deriveFont(40f);
		label1.setFont(f);
		label2.setFont(f);
		label1.setHorizontalAlignment(JLabel.CENTER);
		label2.setHorizontalAlignment(JLabel.CENTER);
		label1.setVerticalAlignment(JLabel.BOTTOM);
		label2.setVerticalAlignment(JLabel.TOP);
		temp.add(label1);
		temp.add(label2);
		sPanel.add(temp);
		
		temp = new JPanel();
		enable = new JButton("Enable");
		disable = new JButton("Disable");
		temp.setLayout(new GridLayout(1, 2));
		temp.add(enable);
		temp.add(disable);
		sPanel.add(temp);
		disable.setEnabled(false);
		enable.addActionListener(e -> buttonPressed(e));
		disable.addActionListener(e -> buttonPressed(e));
		f = f.deriveFont(18f);
		enable.setFont(f);
		disable.setFont(f);
		
		temp = new JPanel();
		temp.setLayout(new GridLayout(1, 2));
		auto = new JButton("Autonomous");
		tele = new JButton("Teleop");
		temp.add(auto);
		temp.add(tele);
		tele.setEnabled(false);
		auto.addActionListener(e -> buttonPressed(e));
		tele.addActionListener(e -> buttonPressed(e));
		sPanel.add(temp);
		tele.setFont(f);
		auto.setFont(f);
		
		sFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sFrame.add(sPanel);
		sPanel.setPreferredSize(new Dimension(300, 450));
		sFrame.pack();
		
		checkJoysticks();
		ControllerEnvironment.getDefaultEnvironment().addControllerListener(new ControllerListener() {
			public void controllerAdded(ControllerEvent e) {
				System.out.println("device added");
				checkJoysticks();
			}

			public void controllerRemoved(ControllerEvent e) {
				System.out.println("device removed");
				checkJoysticks();
			}
		});
		
		gThread = new Thread("Graphics") {
			public void run() {
				long start, delay;
				while (true) {
					start = System.currentTimeMillis();
					paint();
					delay = System.currentTimeMillis() - start;
					try {
						if (delay < G_DELAY) Thread.sleep(G_DELAY - delay);
					} catch (Exception e) {}
				}
			}
		};
		
		pThread = new Thread("Physics") {
			public void run() {
				long start, delay;
				long dt, last = System.currentTimeMillis();
				while (true) {
					start = System.currentTimeMillis();
					dt = start - last;
					last = start;
					physics(dt / 1000d);
					delay = System.currentTimeMillis() - last;
					try {
						if (delay < P_DELAY) Thread.sleep(P_DELAY - delay);
					} catch (Exception e) {}
				}
			}
		};
		
		rcThread = new Thread("Robot Code") {
			public void run() {
				robot.robotInit();
				boolean di = true,
						ti = true,
						ai = true;
				long start, delay;
				while (true) {
					start = System.currentTimeMillis();
					if (disabled) {
						ti = true;
						ai = true;
						if (di) {
							di = false;
							robot.disabledInit();
						} else {
							robot.disabledPeriodic();
						}
					} else if (teleop) {
						di = true;
						ai = true;
						if (ti) {
							ti = false;
							robot.teleopInit();
						} else {
							robot.teleopPeriodic();
						}
					} else {
						di = true;
						ti = true;
						if (ai) {
							ai = false;
							robot.autonomousInit();
						} else {
							robot.autonomousPeriodic();
						}
					}
					delay = System.currentTimeMillis() - start;
					try {
						if (delay < RC_DELAY) Thread.sleep(RC_DELAY - delay);
					} catch (Exception e) {}
				}
			}
		};
		
	}
	
	private void buttonPressed(ActionEvent e) {
		Object s = e.getSource();
		if (s == enable) {
			disabled = false;
		} else if (s == disable) {
			disabled = true;
		} else if (s == tele) {
			teleop = true;
			disabled = true;
		} else if (s == auto) {
			teleop = false;
			disabled = true;
		}
		enable.setEnabled(disabled);
		disable.setEnabled(!disabled);
		auto.setEnabled(teleop);
		tele.setEnabled(!teleop);
		label1.setText(teleop?"Teleop":"Autonomous");
		label2.setText(disabled?"Disabled":"Enabled");
	}

	public static void start(RobotBase robot) {
		world = new RobotWorld(robot);
		world.start();
	}
	
	public void start() {
		gThread.start();
		pThread.start();
		rcThread.start();

		frame.setVisible(true);
//		sFrame.setVisible(true);
	}
	
	public void paint() {
		Graphics2D g = (Graphics2D) panel.getGraphics();
		Graphics2D g2 = (Graphics2D) vi.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.white);
		g2.fillRect(0, 0, vi.getWidth(), vi.getHeight());
		g2.scale(PPI, -PPI);
		g2.translate(WIDTH/2, -HEIGHT/2);
		robot.paint(g2);
		int x, y, w, h;
		if ((double) vi.getWidth() / vi.getHeight() > (double) panel.getWidth() / panel.getHeight()) {
			w = panel.getWidth();
			h = (int) ((double) w / vi.getWidth() * vi.getHeight());
			x = 0;
			y = (panel.getHeight() - h) / 2;
		} else {
			h = panel.getHeight();
			w = (int) ((double) h / vi.getHeight() * vi.getWidth());
			x = (panel.getWidth() - w) / 2;
			y = 0;
		}
		g.setColor(Color.black);
//		g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
		g.drawImage(vi, x, y, w, h, null);
		g2.dispose();
		g.dispose();
	}
	
	public void physics(double dt) {
		for (int i = 0; i < joysticks.size(); i++) {
			joysticks.get(i).poll();
		}
		robot.physics(dt);
	}
	
	public void checkJoysticks() {
		ArrayList<Controller> temp = new ArrayList<Controller>();
		for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			if (c.getType().equals(Type.STICK) || c.getType().equals(Type.GAMEPAD)) {
				temp.add(c);
			}
		}
		joysticks = temp;
		System.out.println(joysticks);
	}

	public void componentResized(ComponentEvent e) {
		paint();
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	
}
