package ie.wombat.rt.pothole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static List<TimeAccelerationPosition> getTimeAccelerationPositionRecords (File file)
	throws IOException {


		ArrayList<TimePosition> timepositions = new ArrayList<TimePosition>();

		// Pass 1: get position vs time
		String line;
		ArrayList<TimeAccelerationPosition> tapRecords = new ArrayList<TimeAccelerationPosition>();
		ArrayList<TimeAccelerationPosition> allTapRecords = new ArrayList<TimeAccelerationPosition>();

		long t=0;
		long t0 = 0;

		BufferedReader lnr = new BufferedReader(new FileReader(file));

		while (  (line = lnr.readLine()) != null ) {
			
			String[] p = line.split(" ");
			
			if (p.length < 2) {
				System.err.println ("short line: " + line);
				continue;
			}
			
			t = Long.parseLong(p[1]);
			
			if (t0 == 0) {
				t0 = t;
			}
			
			if ("L".equals(p[0])) {
				if (p.length < 4) {
					System.err.println("expecting 4 items or more in record: " + line);
				}
				TimePosition tp = new TimePosition();
				tp.t = t;
				tp.latitude = Double.parseDouble(p[2]);
				tp.longitude = Double.parseDouble(p[3]);
				timepositions.add(tp);
				
				// Linear interpolation of lat/lon for each record
				if (tapRecords.size () > 0 && timepositions.size() > 1) {
					// tpm1 is tp[i-1]
					TimePosition tpm1 = timepositions.get(timepositions.size()-2);
					double dt = (double)(tp.t - tpm1.t);
					double rlat = (tp.latitude - tpm1.latitude)/dt;
					double rlon = (tp.longitude - tpm1.longitude)/dt;
					for (TimeAccelerationPosition tap : tapRecords) {
						tap.latitude = tpm1.latitude + (tap.t - tpm1.t) * rlat;
						tap.longitude = tpm1.longitude + (tap.t - tpm1.t) * rlon;
					}
					allTapRecords.addAll(tapRecords);
				}
				
				
				tapRecords.clear();
				continue;
			} 
			
			if ("G".equals(p[0])) {
				if (p.length < 5) {
					System.err.println("expecting 5 items or more in record: " + line);
					continue;
				}
				TimeAccelerationPosition tap = new TimeAccelerationPosition();
				tap.t = t;
				tap.g = new float[3];
				tap.g[0] = Float.parseFloat (p[2]);
				tap.g[1] = Float.parseFloat (p[3]);
				tap.g[2] = Float.parseFloat (p[4]);
				tapRecords.add(tap);
			}
			
				
		}
		lnr.close();
		return allTapRecords;
	}
}
