package ie.wombat.rt.gps;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class NMEAStream {

	public static void handleStream (Reader r, UpdateCallback callback) throws IOException {
		
		LineNumberReader reader = new LineNumberReader (r);
		
		String line;
		
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("$GPGGA")) {
				continue;
			}

			// System.err.println(line);

			String[] p = line.split(",");
			String lat_dm = p[2];
			String lon_dm = p[4];

			double[] lla = new double[3];

			try {
				lla[0] = Double.parseDouble(lat_dm.substring(0, 2))
						+ Double.parseDouble(lat_dm.substring(2)) / 60;
				if (p[3].equals("S")) {
					lla[0] = -lla[0];
				}

				lla[1] = Double.parseDouble(lon_dm.substring(0, 3))
						+ Double.parseDouble(lon_dm.substring(3)) / 60;
				if (p[5].equals("W")) {
					lla[1] = -lla[1];
				}

				lla[2] = Double.parseDouble(p[9]);
			} catch (Exception e) {
				// ignore
			}
			
			callback.gpsUpdate(lla[0], lla[1], lla[2], 0);
		}
	}
}
