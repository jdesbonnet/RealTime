package ie.wombat.rt.datasource;

import ie.wombat.rt.wx.BuoyRecord;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class ParseHTMLBuoyObservations {

	private static final long DAY = 24L * 3600000L;
	
	/* Old column layout (previous to June 2007) */
	/*
	private static final int RH_COL = 13;

	private static final int DPT_COL = 12;

	private static final int DBT_COL = 11;

	private static final int ST_COL = 10;

	private static final int WH_COL = 9;

	private static final int WP_COL = 8;

	private static final int GS_COL = 7;

	private static final int WS_COL = 6;

	private static final int WD_COL = 5;

	private static final int CPT_COL = 4;

	private static final int APT_COL = 3;

	private static final int AP_COL = 2;

	private static final int TS_COL = 1;
	
	*/
	
	/* New column layout, post June 2007 */
	private static final int RH_COL = 10;

	private static final int DPT_COL = 9;

	private static final int DBT_COL = 8;

	private static final int ST_COL = 13;

	private static final int WH_COL = 11;

	private static final int WP_COL = 12;

	private static final int GS_COL = 6;

	private static final int WS_COL = 5;

	private static final int WD_COL = 7;

	private static final int CPT_COL = 4;

	private static final int APT_COL = 3;

	private static final int AP_COL = 2;

	private static final int TS_COL = 1;
	
	/* Extended table layout (currently just for M1) */
	private static final int E_RH_COL = 10;
	private static final int E_DPT_COL = 9;
	private static final int E_DBT_COL = 8;
	private static final int E_ST_COL = 16;
	private static final int E_WAVEH_COL = 11;
	private static final int E_WAVEP_COL = 12;
	private static final int E_WAVES_COL = 15;
	private static final int E_GS_COL = 6;
	private static final int E_WS_COL = 5;
	private static final int E_WD_COL = 7;
	private static final int E_CPT_COL = 4;
	private static final int E_APT_COL = 3;
	private static final int E_AP_COL = 2;
	private static final int E_TS_COL = 1;
	private static final int E_COND_COL = 18;
	private static final int E_SAL_COL = 19;
	
	
	

	private static Logger log = Logger.getLogger(ParseHTMLBuoyObservations.class);
	
	private static SimpleDateFormat df = new SimpleDateFormat ("dd MMM HH:mm yyyy");
	
	public static List parseHTML (String html) {
		
		//String[] tables = html.split ("<table");
		String[] stations = html.split ("Station ID:");
		
		log.info ("found " + stations.length + " stations");
		
		ArrayList recList = new ArrayList ();
		
		String stationId;
		
		// 3 Oct 2006
		// work around but in Observations HTML page (M3 mislabeled as 62093)
		boolean stn62092done = false;
		
		for (int i = 0; i < stations.length; i++) {
			
			if (stations[i].startsWith("N\\A")) {
				stationId = "FS1";
			} else {
				stationId = stations[i].substring(0,5);
			}
			
			if ("62093".equals(stationId) && ! stn62092done) {
				stationId = "62092";
				stn62092done = true;
			}
			
			handleStation(stationId, stations[i], recList);
		}
		
		/*
		BuoyRecord[] ret = new BuoyRecord[recList.size()];
		recList.toArray(ret);
		return ret;
		*/
		return recList;
	}
	
	private static void handleStation (String stationId, String stationHTML, List recList) {
		
		
		String[] rows = stationHTML.split ("<tr");
		log.info ("  found " + rows.length + " rows");
		
		if (rows.length != 26) {
			return;
		}
		
		System.out.println ("StationID: " + stationId + " rows=" + rows.length);
		
		
		for (int j = 0; j < rows.length; j++) {
			String[] cols = rows[j].split("<td");
			
			if (cols.length != 14) {
				System.err.println ("  ignoring row " + j + " as #col != 14 (cols=" + cols.length + ")");
				continue;
			}
			
			log.info("found suitable station observation with " + cols.length + "cols");
			
			// Debug
			if (log.isDebugEnabled()) {
				for (int i = 0; i < cols.length; i++) {
					log.debug ("    col["+i+"] = " + cols[i]);
				}
			}
			
			BuoyRecord r = handleRow (stationId, cols);
			
			if (r == null) {
				continue;
			}
			
			recList.add(r);
			
			System.out.println ("r=" + r.toString());
		}
		
	}
	
	private static BuoyRecord handleRow (String stationId, String[] cols) {
		
		// Trip HTML crud from columns
		for (int i = 1; i < cols.length; i++) {
			String s = cols[i];
			//log.info ("      before clean col[" + i + "]=" + s );

			int a = s.indexOf(">");
			int b = s.indexOf("<");
			/*
			s = s.replaceAll(" colspan=\"2\" nowrap=\"nowrap\" valign=\"LEFT\" width=\"150\">","");
			s = s.replaceAll(" colspan=\"2\" nowrap=\"nowrap\" valign=\"LEFT\">","");
			s = s.replaceAll(" bgcolor=\"#ebebeb\">","");
			s = s.replaceAll(" valign=\"LEFT\">", "");
			s = s.replaceAll(" VALIGN=\"LEFT\" COLSPAN=\"2\" NOWRAP>", "");
			s = s.replaceAll("</td>","");
			s = s.replaceAll("</tr>","");
			*/
			s = s.substring(a+1,b);
			//log.info(" after clean col[" + i + "]="   + s);
			cols[i] = s;
		}
		
		BuoyRecord r = new BuoyRecord();
		r.setStationId(stationId);
		
		try {
			// Handle year end transition
			int thisYear = Calendar.getInstance().get(Calendar.YEAR);
			String ts = cols[TS_COL] + " " + thisYear;
			Date d = df.parse(ts);
			long dt = (d.getTime() - System.currentTimeMillis())/DAY;
			dt = (dt < 0 ?  -dt:dt);
			if (dt > 60)  {
				log.warn("Year end transition detected dt=" + dt + " days. Applying " + (thisYear-1) + " as year for timestamp=" + cols[TS_COL]);
				ts = cols[TS_COL] + " " + (thisYear-1);
				d = df.parse(ts);
			}
			r.setTimestamp(d);
		} catch (Exception e) {
			// no good if we can't get timestamp
			log.error (e);
			return null;
		}

		
		// All other fields are optional
		
		try {
			r.setAtmosphericPressure(new Float(cols[AP_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setPressureTendency(new Float(cols[APT_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setCharPressureTendency(new Float(cols[CPT_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setWindDirection(new Float(cols[WD_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setWindSpeed(new Float(cols[WS_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setWindMaxGustSpeed(new Float(cols[GS_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setWavePeriod(new Float(cols[WP_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setWaveHeight(new Float(cols[WH_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setSeaTemperature(new Float(cols[ST_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setDryBulbTemperature(new Float(cols[DBT_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setDewPointTemperature(new Float(cols[DPT_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		try {
			r.setRelativeHumidity(new Float(cols[RH_COL]));
		} catch (Exception e) {
			// ignore
		}
		
		return r;
		
	}
	
	
	public static void main (String[] arg) throws Exception {
		
		BasicConfigurator.configure();
		
		File f = new File(arg[0]);
		FileReader r = new FileReader(f);
		LineNumberReader lnr = new LineNumberReader(r);
		StringBuffer buf = new StringBuffer();
		String line;
		while (  (line = lnr.readLine()) != null) {
			buf.append(line);
		}
		
		parseHTML (buf.toString());
	}
}
