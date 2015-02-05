package ie.wombat.rt.datasource;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.wx.BuoyRecord;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Near real time data also from
 * http://dods.ndbc.noaa.gov/cgi-bin/nph-dods/dods/stdmet/62090/62090h9999.nc.ascii
 * (or replace .ascii with .info, .html, .dods and other formats)
 * See also http://www.opendap.org/ and 
 * http://www.unidata.ucar.edu/data/
 * and http://www-sdd.fsl.noaa.gov/MADIS/
 * and
 * http://motherlode.ucar.edu/cgi-bin/dods/nph-nc/dods/surface/synoptic/Surface_Synoptic_20061018_0000.nc.ascii?wmoId[0:1:17507],Lat[0:1:17507],Lon[0:1:17507]
 * 
 * 
 * For long term data see ftp://ftp.ncdc.noaa.gov/pub/data/gsod/2006/
 * @author joe
 *
 */
public class ReadBuoyData {

	private static Logger log = Logger.getLogger(ReadBuoyData.class);
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");

	public static void pollServer (String url) throws IOException {
		HttpClient client = new HttpClient ();
		//client.setTimeout(900);
		
		String observationsURL = "http://www.marine.ie/home/publicationsdata/data/buoys/Observations.htm";
		
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
		
		List<BuoyRecord> recs = ParseHTMLBuoyObservations.parseHTML(html);
		
		log.info (recs.size() + " records retrieved from server");
		
		Date oldestRec = Calendar.getInstance().getTime();
		
		
	
		for (BuoyRecord r : recs) {
			if (r.getTimestamp().before(oldestRec)) {
				oldestRec = r.getTimestamp();
			}
		}
		
		log.info ("oldestRec=" + oldestRec);
		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();
		
		String query = "from BuoyRecord where timestamp >= :oldestRec";
		
		List<BuoyRecord> dbrecs = hsession.createQuery(query)
			.setTimestamp("oldestRec", oldestRec)
			.list();
		
		HashMap<String,BuoyRecord> recHash = new HashMap<String,BuoyRecord>(dbrecs.size());
		for (BuoyRecord r : dbrecs) {
			String key = r.getStationId() + hqlf.format(r.getTimestamp());
			recHash.put (key, r);
		}
		
		int nNewRecord = 0;
		for (BuoyRecord r : recs) {
			String key = r.getStationId() + hqlf.format(r.getTimestamp());
			if (recHash.containsKey(key)) {
				BuoyRecord rdb = (BuoyRecord)recHash.get(key);
				boolean saveNeeded = false;
				
				if (rdb.getAtmosphericPressure()==null && r.getAtmosphericPressure() !=null) {
					rdb.setAtmosphericPressure(r.getAtmosphericPressure());
					saveNeeded=true;
				}
				
				if (rdb.getPressureTendency()==null && r.getPressureTendency()!=null) {
					rdb.setPressureTendency(r.getPressureTendency());
					saveNeeded=true;
				}
				
				if (rdb.getCharPressureTendency()==null && r.getCharPressureTendency()!=null) {
					rdb.setCharPressureTendency(r.getCharPressureTendency());
					saveNeeded=true;
				}
				
				if (rdb.getWaveHeight()==null && r.getWaveHeight()!=null) {
					rdb.setWaveHeight(r.getWaveHeight());
					saveNeeded=true;
				}
				
				if (saveNeeded) {
					hsession.save(rdb);
				} 
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
}