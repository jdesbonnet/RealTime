package ie.wombat.rt.witilt;

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
public class WiTiltInputStream {
	
	private static Pattern pattern = Pattern.compile("^X=([+\\-0-9\\.]+)Y=([+\\-0-9\\.]+)Z=([+\\-0-9\\.]+)R=([+\\-0-9]+)");
	
	private LineNumberReader lnr;
	
	public WiTiltInputStream (java.io.Reader sioIn) {
		lnr = new LineNumberReader(sioIn);
	}
	
	public WiTiltRecord getReading () throws IOException {
		String line;
		while ( (line = lnr.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (! matcher.find()) {
				//System.err.println ("NOMATCH:" + line);
				continue;
			}
			WiTiltRecord reading = new WiTiltRecord();
			reading.timestamp = System.currentTimeMillis();
			reading.x = Float.parseFloat(matcher.group(1));
			reading.y = Float.parseFloat(matcher.group(2));
			reading.z = Float.parseFloat(matcher.group(3));
			reading.rz = Float.parseFloat(matcher.group(4));
			
			return reading;
			//System.out.println  ( (System.currentTimeMillis()/1000) + "\t" + sensorNumber + "\t" + temperatureC );
		}
		
		// TODO: wrong
		return null;
	}
}
