package ie.wombat.rt.datasource;

import ie.wombat.rt.wx.APRSWXStationRecord;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * See http://www.aprs.net/vm/DOS/WX.HTM
 * @author joe
 *
 */
public class ReadAPRSWXData {

	private static Logger log = Logger.getLogger(ReadAPRSWXData.class);
	
	private final static String serviceURL = "http://www.findu.com/cgi-bin/rawwx.cgi";
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");
	
	public static void pollServer (Session hsession, String stnId) throws IOException {
		
		String queryURL = serviceURL + "?call=" + stnId
			+ "&last=48";
		
		
		HttpClient client = new HttpClient ();
			
		GetMethod get = new GetMethod (queryURL);
		
		int status = client.executeMethod(get);
		
		if (status != 200) {
			throw new IOException ("Server returned status !=200. Status=" + status);
		}
		
		String html = get.getResponseBodyAsString();
		
		if (html == null || html.length()==0) {
			throw new IOException ("Server did not return any data, html=");
		}
		
		SimpleDateFormat tsf = new SimpleDateFormat ("yyyyMMddHHmm");
		tsf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		Date now = Calendar.getInstance().getTime();
		String datePrefix = tsf.format(now).substring (0,6);
		
		log.info("datePrefix=" + datePrefix);
		
		
		
		LineNumberReader lnr = new LineNumberReader(new StringReader(html));
		String line;
		Date oldestTimestamp = now;
		ArrayList recs = new ArrayList();
		
		while ( (line = lnr.readLine()) != null) {
			if (! line.startsWith (stnId)) {
				continue;
			}
			
			String[] p = line.split(" ");
			String packet = p[0];
			System.err.println ("packet=" + packet);
			
			String regex;
			regex = "([0-9]+)z"
				+ "([0-9\\.]+[NS])/([0-9\\.]+[EW])"
				+ "_([0-9]+)/([0-9]+)"
				+ "g([0-9]+)t([0-9]+)"
				+ "r([0-9]+)"
				+ "p([0-9]+)P([0-9]+)"
				+ "b([0-9]+)h([0-9]+)";
			
			
		
			String[] words = packet.split (",");
			
			String data = words[3];
			
			System.err.println ("  data=" + data);
			
			// ts, wd, ws, gs, at, p60m, p24h, pyd, ap, rh
			String csv = data.replaceAll(regex,",$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,");
			
			String[] c = csv.split (",");
			
			APRSWXStationRecord r = new APRSWXStationRecord ();
			r.setStationId(stnId);
			
			try {
				Date ts = tsf.parse(datePrefix + c[1]);
				r.setTimestamp(ts);
			} catch (Exception e) {
				log.error(e);
				continue;
			}
			
			log.info ("new record ts=" + r.getTimestamp());
			
			try {
				r.setWindDirection(new Float(c[4]));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				r.setWindSpeed(new Float(c[5]));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				r.setWindMaxGustSpeed(new Float(c[6]));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				r.setAirTemperature(new Float(c[7]));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				float pf = Float.parseFloat(c[8]);
				pf /= 100;
				r.setPrecipitationLast60m(new Float(pf));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				float pf = Float.parseFloat(c[9]);
				pf /= 100;
				r.setPrecipitationLast24h(new Float(pf));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				float pf = Float.parseFloat(c[10]);
				pf /= 100;
				r.setPrecipitationLastDay(new Float(pf));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				float ap = Float.parseFloat(c[11]);
				r.setAtmosphericPressure(new Float(ap/10));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			try {
				int rh = Integer.parseInt(c[12]);
				if (rh == 0) {
					rh = 100;
				}
				r.setRelativeHumidity(new Float(rh));
			} catch (Exception e) {
				log.error(e);
				// ignore
			}
			
			r.setRawData(packet);
			
			if (r.getTimestamp().before(oldestTimestamp)) {
				oldestTimestamp = r.getTimestamp();
			}
			recs.add (r);
			
		}
		
		String query = "from APRSWXStationRecord where timestamp >= "
			+ hqlf.format(oldestTimestamp);
		
		HashMap recHash = new HashMap();
		Iterator iter = hsession.createQuery(query).list().iterator();
		while (iter.hasNext()) {
			APRSWXStationRecord dbr = (APRSWXStationRecord)iter.next();
			recHash.put (dbr.getTimestamp(), dbr);
		}
		
		iter = recs.iterator();
		while (iter.hasNext()) {
			APRSWXStationRecord r = (APRSWXStationRecord)iter.next();
			if (recHash.containsKey(r.getTimestamp())) {
				log.info("record already exists for ts=" + r.getTimestamp());
				continue;
			} else {
				log.info("saving new record for ts=" + r.getTimestamp());
				hsession.save(r);
			}
		}
		
	}
}
