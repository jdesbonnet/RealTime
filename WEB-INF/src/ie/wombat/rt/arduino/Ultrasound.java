package ie.wombat.rt.arduino;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import javax.swing.JFrame;

public class Ultrasound extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Canvas canvas;
	LineNumberReader lnr;

	public static void main(String args[]) throws IOException {
		
		/*
		 * javax.swing.SwingUtilities.invokeLater(new Runnable() { public void
		 * run() { //createAndShowGUI(); new Oscilloscope(); } });
		 */
		File ioDevice = new File(args[0]);
		FileReader r = new FileReader(ioDevice);
		
		Ultrasound osc = new Ultrasound(new LineNumberReader(r));

	}

	public Ultrasound(LineNumberReader lnr) {
		
		this.lnr = lnr;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		this.setSize(1024, 980);
		canvas = new Canvas();
		canvas.setSize(1000, 950);
		add(canvas);

		setVisible(true);

		Thread runThread = new Thread(this);
		runThread.start();
	}

	public void run() {
		int i, j = 0, v,py;
		Graphics g = canvas.getGraphics();
		
		g.setColor(Color.RED);
		
		String line;
		try {
			while ((line = lnr.readLine()) != null) {
				//System.err.println(line);
				String[] p = line.split(" ");
				py = j;
				for (i = 0; i < p.length; i++) {
					v = Integer.parseInt(p[i], 16);
					//g.drawRect(i*10, v + j, 2, 2);
					g.drawLine((i-1)*5,py, i*5, v + j);
					py = v + j;
				}
				j += 50;
				if (j > 800) {
					j = 0;
				}
			}
		} catch (IOException e) {
			// ignore
		}
	}

	public int getADC() {
		try {
			return Integer.parseInt(lnr.readLine());
		} catch (Exception e) {
			return -1;
		}
	}
}
