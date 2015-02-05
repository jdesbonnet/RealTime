package ie.wombat.rt.datasource;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.Station;
import ie.wombat.rt.wx.NRAStationRecord;

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
import org.hibernate.Transaction;

/**
 * Year 2010: changes made.
 * http://www.nratraffic.ie/weather.asp?Index=IR049
 */
public class ReadOldNRAData {

	private static Logger log = Logger.getLogger(ReadOldNRAData.class);
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");

	public static void pollServer () throws IOException {
		HttpClient client = new HttpClient ();
		//client.setTimeout(900);
		
		String observationsURL = "http://www.nra.ie/RoadWeatherInfo/Map/data/htm/WeatherTable.htm";
		
		GetMethod get = new GetMethod (observationsURL);
	
		//get.setRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		
		int status = client.executeMethod(get);
		
		if (status != 200) {
			throw new IOException ("Server returned status !=200. Status=" + status);
		}
		
		String html = get.getResponseBodyAsString();
		
		if (html == null || html.length()==0) {
			throw new IOException ("Server did not return any data, html=");
		}
		
		//System.out.println ("html=" + html);
		
		List recs = parseHTML(html);
		
		
		log.info (recs.size() + " records retrieved from server");
		
		Date oldestRec = Calendar.getInstance().getTime();
		Iterator iter;
		
		iter = recs.iterator();
		while (iter.hasNext()) {
			NRAStationRecord r = (NRAStationRecord)iter.next();
			if (r.getTimestamp().before(oldestRec)) {
				oldestRec = r.getTimestamp();
			}
		}
		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();
		
		String query;
		
		
		// Get list of stations and make hash
		query = "from Station where stationType='NRA'";
		List stations = hsession.createQuery(query).list();
		HashMap stnHash = new HashMap(stations.size());
		iter = stations.iterator();
		while (iter.hasNext()) {
			Station stn = (Station)iter.next();
			stnHash.put (stn.getStationId(),stn);
		}
		
		
		query = "from NRAStationRecord where timestamp >= "
			+ hqlf.format(oldestRec);
		
		log.info("query=" + query);
		
		List dbrecs = hsession.createQuery(query).list();
		
		HashMap recHash = new HashMap(dbrecs.size());
		iter = dbrecs.iterator();
		while (iter.hasNext()) {
			NRAStationRecord r = (NRAStationRecord)iter.next();
			String key = r.getStationId() + hqlf.format(r.getTimestamp());
			recHash.put (key, r);
		}
		
		iter = recs.iterator();
		int nNewRecord = 0;
		while (iter.hasNext()) {
			NRAStationRecord r = (NRAStationRecord)iter.next();
			
			//	Create station record if none exists
			/*
			if (! stnHash.containsKey(r.getStationId())) {
				Station stn = new Station();
				stn.setStationType ("NRA");
				stn.setStationId(r.getStationId());
				hsession.save(stn);
				stnHash.put(stn.getStationId(), stn);
			}
			*/
			
			
			String key = r.getStationId() + hqlf.format(r.getTimestamp());
			if (recHash.containsKey(key)) {
			 // ignore
			} else {
				log.info ("creating new record for timestamp=" + r.getTimestamp());
				hsession.save(r);
				nNewRecord++;
			}
		}
		tx.commit();
		HibernateUtil.closeSession();
		
		log.info("" + nNewRecord + " records created");
		
	}
	
	public static List parseHTML (String html) {
		
		SimpleDateFormat tsf = new SimpleDateFormat ("yyyy MMM dd, HH:mm");
		tsf.setTimeZone(TimeZone.getTimeZone("GB-Eire"));
		
		HashMap wdHash = new HashMap();
		wdHash.put ("Northerly", new Float(0));
		wdHash.put ("North-easterly", new Float(45));
		wdHash.put ("Easterly", new Float(90));
		wdHash.put ("South-easterly", new Float (135));
		wdHash.put ("Southerly", new Float (180));
		wdHash.put ("South-westerly", new Float(225));
		wdHash.put ("Westerly", new Float (270));
		wdHash.put ("North-westerly", new Float(315));
		
		HashMap precipHash = new HashMap();
		precipHash.put("No recent rainfall", new Integer(0));
		precipHash.put("Light rainfall", new Integer(1));
		precipHash.put("Medium rainfall", new Integer(2));
		precipHash.put("Heavy rainfall", new Integer(3));
		
		
		
		
		ArrayList recList = new ArrayList ();
		
		String[] rows = html.split("<TR>");
		
		for (int j = 0; j < 7; j++) {
			String[] cols = rows[j].split ("<TD");
			for (int i = 0; i < cols.length; i++) {
			log.info("row " + j + " col " + i + ": " 
					+ cols[i]);
			}
		}
		
		String lcrap1 = "  colspan=\"1\" height=9 ALIGN=\"center\"><FONT SIZE=2>";
		String lcrap2 = "  colspan=\"2\" height=9 ALIGN=\"center\"><FONT SIZE=2>";
		String lcrap3 = "  colspan=\"3\" height=9 ALIGN=\"center\"><FONT SIZE=2>";
		
		String rcrap = "</TD></FONT>";
		
		for (int j = 4; j < rows.length; j++) {
			String[] cols = rows[j].split ("<TD");
			String stn = cols[1];
			stn=stn.replaceAll("  colspan=\"3\" height=9 ALIGN=\"left\"><FONT SIZE=2>&nbsp&nbsp", "");
			stn = stn.substring(0, stn.indexOf('<'));
			//stn=stn.replaceAll(rcrap,"");
			
			log.info("Station: " + stn);
			
			String tss = cols[3];
			tss=tss.replaceAll(lcrap1,"");
			//tss=tss.replaceAll(rcrap,"");
			tss = tss.substring(0, tss.indexOf('<'));
			
			Date ts;
			try {
				int thisYear = Calendar.getInstance().get(Calendar.YEAR);
				ts = tsf.parse(thisYear + " " + tss);
				log.info("  timestamp: " + ts);
			} catch (Exception e) {
				log.error(e);
				continue;
			}
			
			String ats = cols[4];
			ats=ats.replaceAll(lcrap1,"");
			//ats=ats.replaceAll(rcrap,"");
			ats = ats.substring(0, ats.indexOf('<'));
			ats=ats.replaceAll("&deg C.", "");
			Float at=null;
			try {
				at = new Float(ats);
			} catch (Exception e) {
				log.error(e);
				//continue;
			}
			
			String rts = cols[5];
			rts=rts.replaceAll(lcrap1,"");
			//rts=rts.replaceAll(rcrap,"");
			rts = rts.substring(0, rts.indexOf('<'));
			rts=rts.replaceAll("&deg C.", "");
			Float rt=null;
			try {
				rt = new Float(rts);
			} catch (Exception e) {
				log.error(e);
				//continue;
			}
			
			String rcs = cols[6];
			rcs=rcs.replaceAll(lcrap2,"");
			//rcs=rcs.replaceAll(rcrap,"");
			rcs = rcs.substring(0, rcs.indexOf('<'));
			
			String rhs = cols[7];
			rhs=rhs.replaceAll(lcrap1,"");
			//rhs=rhs.replaceAll(rcrap,"");
			rhs = rhs.substring(0, rhs.indexOf('<'));
			rhs = rhs.replaceAll("%","");
			Float rh=null;
			try {
				rh = new Float(rhs);
			} catch (Exception e) {
				log.error(e);
				//continue;
			}
			
			String wss = cols[8];
			wss = wss.replaceAll(" km/h","");
			wss=wss.replaceAll(lcrap1,"");
			//wss=wss.replaceAll(rcrap,"");
			wss = wss.substring(0, wss.indexOf('<'));
			Float ws=null;
			try {
				ws = new Float(wss);
			} catch (Exception e) {
				log.error(e);
				//continue;
			}
			
			String wds = cols[9];
			wds=wds.replaceAll(lcrap3,"");
			//wds=wds.replaceAll(rcrap,"");
			wds = wds.substring(0, wds.indexOf('<'));
			Float wd = (Float)wdHash.get(wds);
			
			String pss = cols[10];
			pss=pss.replaceAll(lcrap3,"");
			//pss=pss.replaceAll(rcrap,"");
			pss = pss.substring(0, pss.indexOf('<'));
			Integer ps = (Integer)precipHash.get(pss);
			
			NRAStationRecord rec = new NRAStationRecord();
			rec.setTimestamp(ts);
			rec.setStationId(stn);
			rec.setAirTemperature(at);
			rec.setRoadTemperature(rt);
			rec.setRelativeHumidity(rh);
			rec.setWindSpeed(ws);
			rec.setWindDirection(wd);
			rec.setPrecipitationStatus(ps);
			rec.setRoadCondition(rcs);
			
			recList.add(rec);
		}
		return recList;
	}
}