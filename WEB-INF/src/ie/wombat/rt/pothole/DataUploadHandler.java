package ie.wombat.rt.pothole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.batik.parser.NumberParser;

public class DataUploadHandler {
	private static long MAX_DATA_GAP = 600000;

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmm");
	
	public static void createMetadataFile (File inFile) throws IOException {

		File dataDir = inFile.getParentFile();
		
		// Upload id is filename - file suffix.
		//String id = inFile.getName().substring(inFile.getName().length()-4);
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(inFile));
		
		String line;
		long t = -1, prevt = -1;
		long tript0 = -1;
		//long tripfp0 = -1; // file offset at start of trip
		int tripCount = 0;
		double lat,lon;
		double latMin=90,latMax=-90,lonMin=180,lonMax=-180;
				
		// Skip until we get a Location record
		while (  (line = lnr.readLine()) != null ) {
			String[] p = line.split(" ");
			if ("L".equals(p[0]) && p.length == 7) {
				t = Long.parseLong(p[1]);
				tript0 = t;
				//tripfp0 = raf.getFilePointer();
				lat = Double.parseDouble(p[2]);
				lon = Double.parseDouble(p[3]);
				prevt = t;
				break;
			}
		}
		
		if (t == -1) {
			// no L record
			return;
		}
		
		FileWriter tripWriter = new FileWriter (new File(dataDir, getTripFileName(new Date(t))));
		
		while (  (line = lnr.readLine()) != null ) {
			String[] p = line.split(" ");
			
			if (p.length < 2) {
				continue;
			}
			
			// Ignore hardware/software version record
			if ("V".equals(p[0])) {
				continue;
			}
			
			try {
				t = Long.parseLong(p[1]);
			} catch (NumberFormatException e) {
				System.err.println ("Bad record at line " + lnr.getLineNumber() + ": " + line);
				continue;
			}
			
			/*
			if ( prevt > t) {
				System.err.println ("Time anomaly at line " + lnr.getLineNumber() 
						+ ": time jumped back by "
						+ (prevt - t) + "ms");
				continue;
				
			}
			*/
			if ("L".equals(p[0]) && p.length == 7) {
				lat = Double.parseDouble(p[2]);
				lon = Double.parseDouble(p[3]);
				latMin = lat < latMin ? lat : latMin;
				latMax = lat > latMax ? lat : latMax;
				lonMin = lon < lonMin ? lat : lonMin;
				lonMax = lon > lonMax ? lat : lonMax;
			} 
			
			if ( t - prevt  > MAX_DATA_GAP) {
				System.err.println ("Trip "
						+ "<a href=\"?id=" + inFile.getName() + "&trip=" + tripCount + "\">" 
								+ tripCount + "</a> from " 
						+ new java.util.Date(tript0)
						+ " to " 
						+ new java.util.Date(prevt) 
						+ " due to gap of " 
						+ ((t - prevt) / 3600000) + "h"
						+ " duration=" + ((prevt-tript0)/60000) + "m <br>\n");
				
				tripWriter.close();
				
				tripCount++;
				
				
				tripWriter = new FileWriter (new File(dataDir, getTripFileName(new Date(t))));

				
				// Skip until we get a Location record
				while (  (line = lnr.readLine()) != null ) {
					p = line.split(" ");
					if ("L".equals(p[0])) {
						t = Long.parseLong(p[1]);
						tript0 = t;
						//tripfp0 = raf.getFilePointer();
						lat = Double.parseDouble(p[2]);
						lon = Double.parseDouble(p[3]);
						prevt = t;
						break;
					}
				}
				
				
				
			}
			
			tripWriter.write(line + "\n");
			
			prevt = t;
		}
		
		tripWriter.close();
		
		//raf.close();
		
	}
	
	private static String getTripFileName (Date d) {
		return "trip-" + df.format(d) + ".dat";
	}
}
