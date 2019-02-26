package org.team1533.frcvw;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

public class Console {
	private JFrame frame;
	private ColorPane console;
	private boolean scrollLock = false;
	
	public Console() {
		for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
			if (i.getName().equals("Windows"))
				try {
					UIManager.setLookAndFeel(i.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
			
		frame = new JFrame("Swerve Sim");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel root = new JPanel(new BorderLayout());
		
		console = new ColorPane();
		console.setEditable(false);
		console.setAutoscrolls(true);
		console.setFont(new Font("consolas", Font.PLAIN, 16));
		JScrollPane consoleScroll = new JScrollPane(console);
		consoleScroll.setAutoscrolls(true);
		consoleScroll.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				scrollLock = true;
			}
			public void mouseReleased(MouseEvent e) {
				scrollLock = false;
			}
		});
		consoleScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		System.setOut(console.getOutPrintStream());
		System.setErr(console.getErrPrintStream());
		root.add(consoleScroll, BorderLayout.CENTER);
		
		console.setBackground(Color.black);
		consoleScroll.setBorder(null);
		
		frame.add(root);
		frame.setPreferredSize(new Dimension(1000, 500));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	private class ColorPane extends JTextPane {
		static final int MAX_SIZE = 8192;
		SimpleAttributeSet out;
		SimpleAttributeSet err;
		StringBuilder outBuf = new StringBuilder();
		StringBuilder errBuf = new StringBuilder();
		private StyledDocument doc;
		
		public ColorPane() {
			super();
			doc = getStyledDocument();
			out = new SimpleAttributeSet();
			err = new SimpleAttributeSet();
			StyleConstants.setForeground(out, new Color(240, 240, 240));
			StyleConstants.setForeground(err, new Color(240, 0, 0));
		}
		public void append(AttributeSet att, String s) {
			try {
				doc.insertString(doc.getLength(), s, att);
				if (doc.getLength() > MAX_SIZE) doc.remove(0, 1);
				if (!scrollLock) setCaretPosition(doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		public PrintStream getOutPrintStream() {
			return new PrintStream(new OutputStream() {
				public void write(int c) {
					outBuf.append((char) c);
					if (c=='\n') flush();
				}
				
				public void flush() {
					append(out, outBuf.toString());
					outBuf.setLength(0);
				}
			});
		}
		
		public PrintStream getErrPrintStream() {
			return new PrintStream(new OutputStream() {
				public void write(int c) {
					errBuf.append((char) c);
					if (c=='\n') flush();
				}
				
				public void flush() {
					append(err, errBuf.toString());
					errBuf.setLength(0);
				}
			});
		}
	}
	
}
