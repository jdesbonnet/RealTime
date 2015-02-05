package ie.wombat.rt.datasource;

import ie.wombat.rt.Station;
import ie.wombat.rt.wx.WUStationRecord;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class ReadWUData {

	private static Logger log = Logger.getLogger(ReadWUData.class);

	private static String WU_TS_FORMAT="yyyy-MM-dd HH:mm:ss";
	//private static SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");

	public static void pollServer (Session hsession, String stationId) throws IOException {
		
	
		String query = "from Station where stationId='" + stationId + "'";
		List list = hsession.createQuery(query).list();
		if (list.size() == 0) {
			throw new IOException ("station " + stationId + " not found");
		}
		
		Station station = (Station)list.get(0);
		
		
		HttpClient client = new HttpClient ();
		//client.setTimeout(900);
	
		String url = "http://www.wunderground.com/weatherstation/WXDailyHistory.asp?format=1&ID=" + stationId;
		
		GetMethod get = new GetMethod (url);
	
		//get.setRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		
		int status = client.executeMethod(get);
		
		if (status != 200) {
			throw new IOException ("Server returned status !=200. Status=" + status);
		}
		
		String data = get.getResponseBodyAsString();
		
		if (data == null || data.length()==0) {
			throw new IOException ("Server did not return any data, html=");
		}
		
		List<WUStationRecord> recs = parseCsv(station, data);
		
		log.info (recs.size() + " records retrieved from server");
		
		Date oldestRec = Calendar.getInstance().getTime();
	
		for (WUStationRecord r : recs) {
			if (r.getTimestamp().before(oldestRec)) {
				oldestRec = r.getTimestamp();
			}
		}
		
		log.info("oldestRec=" + oldestRec);
		
		query = "from WUStationRecord where timestamp >= :oldestRec ";
		
		List<WUStationRecord> dbrecs = hsession.createQuery(query)
			.setTimestamp("oldestRec", oldestRec)
			.list();
		
		HashMap<String,WUStationRecord> recHash = new HashMap<String,WUStationRecord>(dbrecs.size());
		for (WUStationRecord r : dbrecs) {
			String key = r.getStationId() + hqlf.format(r.getTimestamp());
			recHash.put (key, r);
		}
		
		int nNewRecord = 0;
		for (WUStationRecord r : recs) {
			String key = r.getStationId() + hqlf.format(r.getTimestamp());
			if (recHash.containsKey(key)) {
				// ignore
			} else {
				log.info ("creating new record for timestamp=" + r.getTimestamp());
				hsession.save(r);
				nNewRecord++;
			}
		}
		
		log.info("" + nNewRecord + " records created");
		
		
	}
	
	private static List parseCsv (Station station, String data) {
		String[] lines = data.split("\n");
		log.info("found " + lines.length + " lines");
		
		ArrayList recs = new ArrayList(lines.length);
		
		long tzo = (long)station.getTimezoneOffset().intValue() * 1000;
		
		
		SimpleDateFormat df = new SimpleDateFormat(WU_TS_FORMAT);
		TimeZone tz = TimeZone.getTimeZone(station.getTimezone());
		log.info("Found TZ=" + tz);
		df.setTimeZone(tz);
		
		
		// start with 2nd line (first CSV header)
		for (int i = 1; i < lines.length; i++) {
			String[] c = lines[i].split(",");
			
			Date ts;
			try {
				ts = df.parse(c[0]);
				// assert station.getTimezone() == "UTC"
				ts = new Date (ts.getTime() - tzo);
			} catch (Exception e) {
				// if ts can't be parsed ignore record
				continue;
			}
			
			WUStationRecord r = new WUStationRecord();
			r.setStationId(station.getStationId());
			r.setTimestamp(ts);
			
			r.setCsvRecord(lines[i]);
			
			try {
				r.setTemperature(new Float(c[1]));
			} catch (Exception e) {
				// ignore
			}
			
			try {
				r.setDewPointTemperature(new Float(c[2]));
			} catch (Exception e) {
				// ignore
			}
			
			try {
				r.setAtmosphericPressure(new Float(c[3]));
			} catch (Exception e) {
				// ignore
			}
			
			
			try {
				r.setWindDirection(new Float(c[5]));
			} catch (Exception e) {
				// ignore
			}
			
			try {
				r.setWindSpeed(new Float(c[6]));
			} catch (Exception e) {
				// ignore
			}
			
			try {
				r.setWindMaxGustSpeed(new Float(c[7]));
			} catch (Exception e) {
				// ignore
			}
			
			try {
				r.setRelativeHumidity(new Float(c[8]));
			} catch (Exception e) {
				// ignore
			}
			
			try {
				r.setPrecipitation(new Float(c[9]));
			} catch (Exception e) {
				// ignore
			}
			
			recs.add(r);
			
		}
		
		return recs;
	}
}
