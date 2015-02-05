package ie.wombat.rt.onewire;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Take raw SIO data from Manuel's AVR dev board and convert to tab delimited 
 * data file. One line per record. Tab delimited fields. Fields are:
 * timestamp, sensor number, temperature C.
 * 
 * @author joe
 *
 */
public class SensorInputStream {
	
	private static Pattern pattern = Pattern.compile("^Sensor# (\\d+) = ([+\\-0-9\\.]+)");
	
	private LineNumberReader lnr;
	
	public SensorInputStream (java.io.Reader sioIn) {
		lnr = new LineNumberReader(sioIn);
	}
	
	public SensorReading getReading () throws IOException {
		String line;
		while ( (line = lnr.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (! matcher.find()) {
				//System.err.println ("NOMATCH:" + line);
				continue;
			}
			SensorReading reading = new SensorReading();
			reading.timestamp = System.currentTimeMillis();
			reading.sensorNumber = Integer.parseInt(matcher.group(1));
			reading.temperatureC = Float.parseFloat(matcher.group(2));
			
			return reading;
			//System.out.println  ( (System.currentTimeMillis()/1000) + "\t" + sensorNumber + "\t" + temperatureC );
		}
		
		// TODO: wrong
		return null;
	}
}
