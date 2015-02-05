package ie.wombat.rt.gps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class TestGPS implements UpdateCallback {

	private long lastUpdate = System.currentTimeMillis();
	
	// Field delimiter
	public static final String SEP = "\t";

	// Record delimiter
	public static final String EOR = "\n";
	
	public static File tripFile = new File ("/var/tmp/trip.dat");
	
	public static void main (String[] arg) throws IOException {
		java.io.File gpsDevice = new java.io.File ("/dev/rfcomm2");
		FileReader r = new FileReader (gpsDevice);
		
		NMEAStream.handleStream(r, new TestGPS());
	}
	
	public void gpsUpdate (double lat, double lon, double alt, double speed) {
		System.out.println ("lat=" + lat + " lon=" + lon + " alt=" + alt);
		
		if ( (System.currentTimeMillis() - lastUpdate) < 60000 ) {
			System.err.println ("Skipping update...");
			return;
		}
		HttpClient client = new HttpClient();
		
		client.setTimeout(20000);
		
		GetMethod get = new GetMethod("http://cms.galway.net:8080/RealTime/jsp/gpsmap/update-form-submit.jsp"
				+ "?lat=" + lat + "&lon=" + lon + "&alt=" + alt + "&comment=");
		
		try {
			int status = client.executeMethod(get);
			System.err.println ("status=" + status);
			if (status == 200) {
				lastUpdate = System.currentTimeMillis();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		try {
		FileWriter w = new FileWriter(tripFile, true);
		w.write ("" + System.currentTimeMillis()
				+ SEP + lat
				+ SEP + lon
				+ SEP 
				+ EOR);
		w.close();
		} catch (IOException e) {
			// ignore
		}
		*/
	}
}
