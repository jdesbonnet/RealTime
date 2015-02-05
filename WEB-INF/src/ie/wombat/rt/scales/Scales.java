package ie.wombat.rt.scales;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.ui.tabbedui.VerticalLayout;


public class Scales extends JFrame implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static boolean AC_MODE = false;
	
	private static final int scale = 1;
	private int offset = -500;
	
	private Canvas canvas;
	private JLabel currentValue;
	LineNumberReader lnr;
	
	public static void main(String args[]) throws IOException {
		Scales osc = new Scales();
		/*
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //createAndShowGUI();
            	new Oscilloscope();
            }
        });
        */
		File ioDevice = new File(args[0]);
		FileReader r = new FileReader(ioDevice);
		osc.lnr = new LineNumberReader(r);
		
	}
	public Scales () {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new VerticalLayout());
		this.setSize(640, 480);
		
		//JPanel p = new JPanel(new FlowLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Digital Scales"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		add(buttonPanel);
		currentValue = new JLabel("");
		//currentValue.setBackground(Color.YELLOW);
		
		buttonPanel.add(currentValue);
		buttonPanel.add(new JButton("TARE"));
		buttonPanel.add(new JButton("HOLD"));
		buttonPanel.add(new JButton("QCAL"));
		
		
		//ScaleBar sb = new ScaleBar();
		//add(sb);
		
		//canvas = new Canvas();
		//canvas.setSize(1000, 950);
		//add(canvas);
		
		JLabel wLabel = new JLabel("98.6 g");
		Font font = new Font("sans-serif", Font.PLAIN, 96);
		wLabel.setBackground(Color.YELLOW);
		wLabel.setFont(font);
		add (wLabel);
		
		
		
	
		//canvas.getGraphics().drawRect(0,0,100,100);
		
		setVisible(true);
		
		Thread runThread = new Thread(this);
		runThread.start();
	}
	public void run() {
		
		Graphics g = canvas.getGraphics();
		int x = 0, xm1;
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		int[] v = new int[width];
		int[] y = new int[width];
		int[] movingAv = new int[256];
		int sigmaMovingAv = 0;
		
		while (true) {
			
			xm1 = x - 1;
			if (xm1<0) {
				xm1 = 0;
			}
			
			// Erase point from last scan
			
			g.setColor(Color.white);
			g.drawRect(x, y[x],2,2);
			
			
			v[x] = getADC();
			
			currentValue.setText(""+v[x]);
			
			sigmaMovingAv -= movingAv[(x+1)%movingAv.length];
			movingAv[x%movingAv.length] = v[x];
			sigmaMovingAv += v[x];
			
			if (AC_MODE) {
				y[x] = height - (v[x] - ((sigmaMovingAv / movingAv.length))*scale + offset);
			} else {
				y[x] = height - (v[x]*scale + offset);
			}
			if (v[x] > 1000) {
				g.setColor(Color.red);
			} else if (v[x] > 800){
				g.setColor(Color.orange);
			} else {
				g.setColor(Color.black);
			}
		
			//g.drawLine(x-1,h - v[ (x-1) < 0  ?  0 : (x-1)], x, h - v[x]);
			g.drawRect( x, y[x],2,2);
			//System.out.println(v[x]);
			x++;
			if (x >= width) {
				x = 0;
			}
		}
		
	}
	public int getADC ()  {
		try {
			return Integer.parseInt(lnr.readLine());
		} catch (Exception e) {
			return -1;
		}
	}
}
