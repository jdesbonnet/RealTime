package ie.wombat.rt.datasource;

import ie.wombat.rt.wx.METARRecord;
import ie.wombat.rt.wx.WXUtil;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.sf.jweather.Weather;
import net.sf.jweather.metar.Metar;

public class ReadMetarData {

	private static final Logger log = Logger.getLogger(ReadMetarData.class);
	
	//private final static String serviceURL = "http://www.findu.com/cgi-bin/rawwx.cgi";

	/** Metar fetch timeout in ms **/
	private final static int METAR_FETCH_TIMEOUT = 20000;
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	public static void pollServer(Session hsession, String station) {

		//String metarData = MetarFetcher.fetch(station);

		Metar metar;

		try {
			
			metar = Weather.getMetar(station, METAR_FETCH_TIMEOUT);
			//metar = MetarParser.parse(metarData);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			return;
		}
		
		//log.info("received METAR for " + station);

		METARRecord r = new METARRecord();
		r.setTimestamp(metar.getDate());
		r.setStationId(station);		

		r.setAirTemperature(metar.getTemperatureInCelsius());

		if (metar.getPressure() != null) {
			float inHg = metar.getPressure().floatValue();
			float hPa = WXUtil.inHg2mb(inHg);
			r.setAtmosphericPressure(new Float(hPa));
		}

		r.setWindSpeed(metar.getWindSpeedInKnots());

		if (metar.getWindDirection() != null) {
			r
					.setWindDirection(new Float(metar.getWindDirection()
							.floatValue()));
		}

		r.setWindMaxGustSpeed(metar.getWindGustsInKnots());

		String query = "from METARRecord where stationId='" + station
				+ "' and timestamp='" + hqlf.format(r.getTimestamp()) + "'";

		List rec = hsession.createQuery(query).list();
		if (rec.size() > 0) {
			return;
		}

		hsession.save(r);

	}
}
