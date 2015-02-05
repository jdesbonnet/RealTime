package ie.wombat.rt.datasource;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.Station;
import ie.wombat.rt.wx.NRAStationRecord;

import java.io.IOException;
import java.text.ParseException;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * NRA data feed changed in March 2010. This has been updated to scrape the 
 * new format.
 * 
 * Example of station data URL:
 * http://www.nratraffic.ie/weather.asp?Index=IR049
 */
public class ReadNRAData {

	private static Logger log = Logger.getLogger(ReadNRAData.class);
	private static SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");
	
	public static void pollServer () throws IOException {
		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();
		
		List<Station> stations = hsession.createQuery("from Station where stationId like 'IR%' and stationType='NRA'")
		//.setMaxResults(1)
		.list();
		
		HttpClient client = new HttpClient ();
		//client.setTimeout(900);
		
		for (Station station : stations) {
			if ( ! station.getStationId().startsWith("IR")) {
				System.err.println ("** Ignoring station " + station.getStationId());
				continue;
			}
			String url = "http://www.nratraffic.ie/weather.asp?Index=" + station.getStationId();
			System.err.println ("Retrieving " + url);
			GetMethod get = new GetMethod (url);
			
			
			int status = client.executeMethod(get);
			
			if (status != 200) {
				throw new IOException ("Server returned status !=200. Status=" + status);
			}
			
			String html = get.getResponseBodyAsString();
			
			if (html == null || html.length()==0) {
				throw new IOException ("Server did not return any data, html=");
			}
			
			NRAStationRecord rec;
			
			try {
				rec = parseHTML(station,html);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			// Make sure this record isn't already there
			List<NRAStationRecord> list = hsession.createQuery("from NRAStationRecord where stationId=:stationId AND timestamp=:ts")
					.setString("stationId", station.getStationId())
					.setTimestamp("ts", rec.getTimestamp())
					.list();
			
			if (list.size() == 0) {
				System.err.println ("New record... saving it");
				hsession.save(rec);
			} else {
				System.err.println ("Record already exists, NOT SAVING");
			}
			
		}
		
		
		tx.commit();
		HibernateUtil.closeSession();
		
	}
	
	public static NRAStationRecord parseHTML (Station station, String html) throws Exception {
		
		//SimpleDateFormat tsf = new SimpleDateFormat ("yyyy MMM dd, HH:mm");
		SimpleDateFormat tsf = new SimpleDateFormat ("dd-MM-yyyy HH:mm");
		tsf.setTimeZone(TimeZone.getTimeZone("GB-Eire"));
		
		HashMap<String,Integer> precipHash = new HashMap<String,Integer>();
		precipHash.put("No Recent Rainfall", new Integer(0));
		precipHash.put("Light Rainfall", new Integer(1));
		precipHash.put("Medium Rainfall", new Integer(2));
		precipHash.put("Heavy Rainfall", new Integer(3));
		
		String[] rows = html.split("<tr>");
		
		NRAStationRecord rec = new NRAStationRecord();
		rec.setStationId(station.getStationId());
		
		int j;
		// Ignore first array element
		for (int i = 1; i < rows.length; i++) {
			
			String xml = "<tr>" + rows[i];
			j = xml.indexOf("</tr>");
			if (j>=0) {
				xml = xml.substring(0,j+5);
			}
			
			xml = xml.replaceAll("&deg;", "°");
			Document document;
			try {
				document = DocumentHelper.parseText(xml);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.err.println ("ERROR PARSING " + xml);
				continue;
			}
			//System.err.println ("XML=" + document.asXML());
			String fieldName = document.selectSingleNode("//th").getStringValue().trim();
			String fieldValue = document.selectSingleNode("//td").getStringValue().trim();
			
			System.err.println (fieldName + fieldValue);
			
			if ("Unavailable".equals(fieldValue)) {
				System.err.println ("ignoring...");
				continue;
			}
			
			if ("Update Time:".equals(fieldName)) {
				Date ts = tsf.parse(fieldValue);
				rec.setTimestamp(ts);
			} else if ("Air Temperature:".equals(fieldName)) {
				try {
					Float t = new Float(fieldValue.substring(0,fieldValue.length()-2)); // remove "°C"
					rec.setAirTemperature(t);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} else if ("Road Temperature:".equals(fieldName)) {
				try {
					Float t = new Float(fieldValue.substring(0,fieldValue.length()-2)); // remove "°C"
					rec.setRoadTemperature(t);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} else if ("Humidity:".equals(fieldName)) {
				Float rh = new Float(fieldValue.substring(0,fieldValue.length()-1)); // remove "%"
				rec.setRelativeHumidity(rh);
			} else if ("Wind Speed:".equals(fieldName)) {
				Float s = new Float(fieldValue.substring(0,fieldValue.length()-4)); // remove "Km/h"
				rec.setWindSpeed(s);
			} else if ("Direction (angle):".equals(fieldName)) {
				Float d = new Float(fieldValue.substring(0,fieldValue.length()-1)); // remove "°"
				rec.setWindDirection(d);
			} else if ("Max Gust Speed:".equals(fieldName)) {
				Float s = new Float(fieldValue.substring(0,fieldValue.length()-4)); // remove "Km/h"	
			} else if ("Precipitation:".equals(fieldName)) {
				if (precipHash.containsKey(fieldValue)) {
					rec.setPrecipitationStatus(precipHash.get(fieldValue));
				}
			} else if ("Road Surface State:".equals(fieldName)) {
				rec.setRoadCondition(fieldValue);
			}
		}
		
		return rec;
		
	}
}