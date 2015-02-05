package ie.wombat.rt.datasource;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.tg.TideGaugeRecord;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @deprecated obsolete, nolonger used see poll-tide-gauge.jsp instead
 * This datasource is now obsolete.
 * @author joe
 *
 */
public class ReadGauge {
	private static Logger log = Logger.getLogger(ReadGauge.class);

	// Date format used in directory on server
	private static SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd");

	// Timestamps in CSV files
	private static SimpleDateFormat timestampf = new SimpleDateFormat(
			"dd/MM/yyyy HH:mm:ss");

	private static String serviceURL = "http://212.17.41.155/tidegaugedata/";

	private static Map<String,String> gaugeName = new HashMap<String,String>();

	public ReadGauge() {
		gaugeName.put("gal1", "Galway");

	}

	public void updateGaugeDB () {
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		updateGaugeDB(now);
	}
	public void updateGaugeDB (Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		List<TideGaugeRecord> recs = readGauge("gal1", date);

		cal.set(Calendar.MILLISECOND,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.HOUR_OF_DAY,0);
		
		Date startOfDay = cal.getTime();
		
		cal.add(Calendar.HOUR, 24);
		Date endOfDay = cal.getTime();
		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();
		
		String query = "from TideGaugeRecord where timestamp >= ? and timestamp < ?";
		List<TideGaugeRecord> dbrecs = hsession.createQuery(query)
			.setTimestamp(0,startOfDay)
			.setTimestamp(0,endOfDay)
			.list();
		
		Map<Date, TideGaugeRecord> recHash = new HashMap(dbrecs.size());
		for (TideGaugeRecord r : dbrecs) {
			recHash.put (r.getTimestamp(), r);
		}
		
	
		int nNewRecord = 0;
		for (TideGaugeRecord r : recs) {
			if (recHash.containsKey(r.getTimestamp())) {
				TideGaugeRecord rdb = (TideGaugeRecord)recHash.get(r.getTimestamp());
				boolean saveNeeded = false;
				if (rdb.getWaterElevation()==null && r.getWaterElevation() !=null) {
					rdb.setWaterElevation(r.getWaterElevation());
					saveNeeded=true;
				}
				if (rdb.getAtmosphericPressure()==null && r.getAtmosphericPressure()!=null) {
					rdb.setAtmosphericPressure(r.getAtmosphericPressure());
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
	public List<TideGaugeRecord> readGauge(String gaugeId, Date date) {

		String gaugeLocation = (String) gaugeName.get(gaugeId);

		String weServiceURL = serviceURL + gaugeLocation
				+ "/Water%20Elevation/";
		String apServiceURL = serviceURL + gaugeLocation
				+ "/Atmospheric%20Pressure/";

		HttpClient client = new HttpClient();
		client.getParams().setAuthenticationPreemptive(true);

		Credentials defaultcreds = new UsernamePasswordCredentials("Galway",
				"G0lw0y");
		client.getState().setCredentials(
				new AuthScope("212.17.41.155", 80, AuthScope.ANY_REALM),
				defaultcreds);

		String dateStr = datef.format(date);

		String weurl = weServiceURL + "gal1_we_" + dateStr + ".csv";
		String apurl = apServiceURL + "gal1_ap_" + dateStr + ".csv";

		Map<Date,TideGaugeRecord> recs = new TreeMap();

		/*
		 * Retrieve water elevation
		 */
		log.info("requesting " + weurl);

		try {
			String[] lines = readCsv(client, weurl);
			for (int i = 1; i < lines.length; i++) {
				String[] col = lines[i].split(",");
				try {
					Date timestamp = timestampf.parse(col[0]);
					Integer wecm = new Integer(col[1].trim());
					TideGaugeRecord r = new TideGaugeRecord();
					r.setGaugeId("gal1");
					r.setTimestamp(timestamp);
					r.setWaterElevation(new Float(wecm.floatValue()/100));

					recs.put(timestamp, r);

				} catch (NumberFormatException e) {
					log.error(e);
				} catch (ParseException e) {
					log.error(e);
				}
			}

		} catch (HttpException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}

		/*
		 * Retrieve atmospheric pressure
		 */
		log.info("requesting " + apurl);

		try {
			String[] lines = readCsv(client, apurl);
			for (int i = 1; i < lines.length; i++) {
				String[] col = lines[i].split(",");
				try {
					Date timestamp = timestampf.parse(col[0]);
					Integer ap = new Integer(col[1].trim());

					TideGaugeRecord r;
					if (recs.containsKey(timestamp)) {
						r = (TideGaugeRecord) recs.get(timestamp);
					} else {
						r = new TideGaugeRecord();
						recs.put(timestamp, r);
					}

					r.setGaugeId("gal1");
					r.setTimestamp(timestamp);
					r.setAtmosphericPressure(ap);

				} catch (NumberFormatException e) {
					log.error(e);
				} catch (ParseException e) {
					log.error(e);
				}
			}

		} catch (HttpException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}

		return new ArrayList(recs.values());

	}

	private String[] readCsv(HttpClient client, String url)
			throws HttpException, IOException {

		GetMethod get = new GetMethod(url);

		int status = client.executeMethod(get);
		if (status != 200) {
			throw new HttpException("Server returned error status code "
					+ status);
		}
		String responseStr = get.getResponseBodyAsString();

		return responseStr.split("\n");
	}
}
