/*
 * Created on May 3, 2005
 *
 * (c) 2005 Joe Desbonnet
 */
package ie.wombat.rt.buspirate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import org.jfree.io.IOUtils;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 
 *  
 */
public class BusPirate extends Thread implements SerialPortEventListener {


	private static final String VERSION = "0.1";
	private static final int DEFAULT_BPS = 115200;
	private SerialPort sioPort;

	private boolean debugFlag = false;

	
	private String sioDevName;
	private int baudrate = DEFAULT_BPS;


	public static void main(String[] arg) throws Exception {

		List<String> argList = new ArrayList<String>(arg.length);
	
		boolean debugFlag = false;
		int baudRate = DEFAULT_BPS;
		
		for (int i = 0; i < arg.length; i++) {
			if ("-debug".equals(arg[i])) {
				debugFlag = true;
				continue;
			}
			if ("-baudrate".equals(arg[i])) {
				try {
					i++;
					baudRate = Integer.parseInt(arg[i]);
				} catch (NumberFormatException e) {
					System.err.println("baudrate must be an integer");
					return;
				}
				continue;
			}

			if ("-scanports".equals(arg[i])) {
				scanPorts();
				return;
			}
			if ("-version".equals(arg[i])) {
				System.err.println("Version: " + VERSION);
				return;
			}
			if ("-help".equals(arg[i])) {
				usage();
				return;
			}
			
			argList.add(arg[i]);
		}

		if (argList.size() != 1) {
			System.err.println ("expecting just one arg, got " + argList.size());
			usage();
			return;
		}

		String sioDeviceName = argList.get(0);

		BusPirate driver = new BusPirate(sioDeviceName, baudRate);
		driver.debugFlag = debugFlag;
		driver.start();
	}

	/**
	 * Display some usage help.
	 *  
	 */
	private static void usage() {
		String className = BusPirate.class.getName();
		System.err.println("java " + className + "\n"
				+ "    [-debug]\n"
				+ "    [-scanports]\n"
				+ "    [-baudrate n]\n" 
				+ "    [-version] "
				+ " siodevice");
		System.err.println("example: java " + className
				+ " -debug -baudrate 19200"
				+ " /dev/ttyS0");
		System.err.println("example: java " + className
				+ " -debug -baudrate 115200"
				+ " COM2");
	}

	public BusPirate(String sioDeviceName, int speed)
			throws IOException {

		
		this.sioDevName = sioDeviceName;
		this.baudrate = speed;

		setName("SIOReader-" + this.sioDevName);
	}

	public void run() {

		sioPort = null;

		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) ports.nextElement();
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& cpi.getName().equals(this.sioDevName)) {
				try {
					sioPort = (SerialPort) cpi.open("SIOReader", 2000);
				} catch (PortInUseException e) {
					System.err.println("error: port in use");
					return;
				}
			}
		}

		if (sioPort == null) {
			System.err.println("error: serial port " + sioDevName
					+ " not found.");
			System.err.println("scanning for ports...");
			scanPorts();
			return;
		}

		/*
		 * Set serial port
		 */
		if (this.debugFlag) {
			System.err.println ("bps=" + this.baudrate);
		}
		
		try {
			sioPort.setSerialPortParams(this.baudrate, 
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, 
					SerialPort.PARITY_NONE);
			//sioPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
			sioPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			sioPort.addEventListener(this);
			sioPort.notifyOnDataAvailable(true);

		} catch (UnsupportedCommOperationException e) {
			System.err.println("error: " + e);
			return;
		} catch (TooManyListenersException e) {
			System.err.println("error: " + e);
			return;
		}

		
		
		try {
			
			// Start the write thread
			OutputStream sioOut = sioPort.getOutputStream();
			Thread wt = new Thread(new WriteThread(sioOut));
			wt.start();
			
			//PrintWriter w = new PrintWriter(sioOut);
			
			
			
			InputStreamReader sioInReader = new InputStreamReader (sioPort.getInputStream());
			
			int c;
			while ( (c = sioInReader.read()) != -1 ) {
				System.out.write(c);
				System.out.flush();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sioPort.removeEventListener();
		sioPort.close();

		System.err.println(" *** MAIN THREAD TERMINATED ***");

	}


	
	public void serialEvent(SerialPortEvent ev) {

		if (this.debugFlag) {
			System.err.println("Received serialport event: " + ev);
		}
	}

	
	/**
	 * Scan for available serial ports and display device names to stderr
	 */
	private static void scanPorts() {

		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			CommPortIdentifier cpi =  ports.nextElement();
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.err.println("  found port " + cpi.getName());
			}
		}
	}



	public void setDebug(boolean b) {
		this.debugFlag = b;
	}

	public boolean isDebug() {
		return this.debugFlag;
	}

}
