/*
 * Created on May 3, 2005
 *
 * (c) 2005 Joe Desbonnet
 */
package ie.wombat.rt.witilt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;

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
public class ReadSIORaw extends Thread implements SerialPortEventListener {


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
			usage();
			return;
		}

		String printerDeviceName = argList.get(0);

		ReadSIORaw driver = new ReadSIORaw(printerDeviceName, baudRate);
		driver.start();
	}

	/**
	 * Display some usage help.
	 *  
	 */
	private static void usage() {
		String className = ReadSIORaw.class.getName();
		System.err.println("java " + className + "\n"
				+ "    [-debug]\n"
				+ "    [-baudrate n]\n" 
				+ "    [-version] "
				+ " siodevpath");
		System.err.println("example: java " + className
				+ " -debug -baudrate 19200"
				+ " /dev/ttyS0");
	}

	public ReadSIORaw(String printerDeviceName, int speed)
			throws IOException {

		
		this.sioDevName = printerDeviceName;
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
		System.err.println ("bps=" + this.baudrate);
		
		try {
			sioPort.setSerialPortParams(this.baudrate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			sioPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
			sioPort.addEventListener(this);
			sioPort.notifyOnDataAvailable(true);

		} catch (UnsupportedCommOperationException e) {
			System.err.println("error: " + e);
			return;
		} catch (TooManyListenersException e) {
			System.err.println("error: " + e);
			return;
		}
		
		// Write dump command
		try {
			OutputStream sioOut = sioPort.getOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(sioOut);
			w.write("1");
			w.flush();
		} catch (IOException e) {
			
		}
		
		try {
			InputStream in = sioPort.getInputStream();
			OutputStream out = System.out;
			IOUtils.getInstance().copyStreams(in, out);
		} catch (IOException e) {
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

	public void finalize() {
		System.err.println("LPS finilize() called");
	}
}
