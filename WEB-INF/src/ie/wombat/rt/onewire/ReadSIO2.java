/*
 * Created on May 3, 2005
 *
 * (c) 2005 Joe Desbonnet
 */
package ie.wombat.rt.onewire;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;


import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Read lines from SIO and prefix with timestamp
 *  
 */
public class ReadSIO2 extends Thread implements SerialPortEventListener {


	private static final String VERSION = "0.1";
	private static final int DEFAULT_BPS = 19200;
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
			System.err.println ("expecting just one arg, got " + argList.size());
			usage();
			return;
		}

		String printerDeviceName = argList.get(0);

		ReadSIO2 driver = new ReadSIO2(printerDeviceName, baudRate);
		driver.start();
	}

	/**
	 * Display some usage help.
	 *  
	 */
	private static void usage() {
		String className = ReadSIO2.class.getName();
		System.err.println("java " + className + "\n"
				+ "    [-debug]\n"
				+ "    [-baudrate n]\n" 
				+ "    [-version] "
				+ " siodevpath");
		System.err.println("example: java " + className
				+ " -debug -baudrate 19200"
				+ " /dev/ttyS0");
	}

	public ReadSIO2(String printerDeviceName, int speed)
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

		java.util.Map<Integer,SensorReading> lastReadingMap = new java.util.HashMap<Integer,SensorReading>();
		
		try {
			InputStreamReader sioInReader = new InputStreamReader (sioPort.getInputStream());
			LineNumberReader lnr = new LineNumberReader(sioInReader);
			
			String line;
		
			while ( (line = lnr.readLine()) != null ) {
				System.out.println  ( (System.currentTimeMillis()/1000) + "\t" + line );
			}
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
