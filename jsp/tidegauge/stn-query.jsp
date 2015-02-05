<%@page contentType="text/xml" %>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.List"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.util.XmlUtil"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="ie.wombat.rt.tg.TideGaugeRecord"%>
<%@page import="ie.wombat.rt.Station"%>
<%@page import="ie.wombat.rt.wx.WUStationRecord"%>
<%@page import="ie.wombat.rt.wx.WXUtil"%>
<%@page import="ie.wombat.rt.wx.NRAStationRecord"%>
<%@page import="ie.wombat.rt.wx.BuoyRecord"%>
<%@page import="ie.wombat.rt.wx.APRSWXStationRecord"%>
<%@page import="ie.wombat.rt.wx.WXRecord"%>
<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.rt.datasource.MetEireannRadar"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.util.Map"%>
<%!
SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");
Logger log = Logger.getLogger("ie.wombat.rt.JSP.stn-query");
%><%
 /*
 * Generate a XML report of current weather conditions for 
 * Javascript (eg Google Maps) consumption
 *
 * Units: ws: kmph; ap: hPa; 
 *
 * Mandatory parameters: none
 * Optional parameters: 't' Time (yyyyMMddHHmmss format). Defaults to now.
 * 
 */

long maxCachedQueryAge = 1800 * 1000;
 
File cachedQuery = new File ("/var/tmp/stn-query-cached.xml");
if (cachedQuery.exists() 
		&& (cachedQuery.length() > 0) 
		&& (request.getParameter("nocache")== null) ) {
	if (System.currentTimeMillis() - cachedQuery.lastModified() < maxCachedQueryAge) {
		System.err.println ("stn-query: found in cache");
		out.clear();
		FileReader r = new FileReader(cachedQuery);
		LineNumberReader lnr = new LineNumberReader(r);
		String line;
		while ( (line = lnr.readLine()) != null) {
			out.write(line);
		}
		return;
	} else {
		System.err.println ("stn-query: cached version too old");
	}
} else {
	System.err.println ("stn-query: no cached version found or caching suppressed");
}

// Start script counter (add System.currentTimeMillis() at end to get exec time
long tt = - System.currentTimeMillis();

Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();

// t = now with 20 minute granularity
Date t =  new Date ((System.currentTimeMillis() / 20000L) * 20000L);

// An alternative time can be specified in as 't' parameter (format yyyyMMddHHmmss)
try {
	t = hqlf.parse(request.getParameter("t"));
	t = new Date (t.getTime() + 20 * 60 * 1000);
} catch (Exception e) {
	// ignore
}

String query;

List<Station> stations = hsession.createQuery("from Station").setCacheable(true).list();
Map<String, Station> stnHash = new HashMap<String, Station>(stations.size());

for (Station stn : stations) {
	stnHash.put (stn.getStationId(),stn);
}

Date hourAgo = new Date (t.getTime() - 3600*1000);
Date twoHourAgo = new Date (t.getTime() - 7200*1000);
Date threeHourAgo = new Date (t.getTime() - 10800*1000);

int nStn = stations.size();

Map<String,Date> tsHash = new HashMap<String,Date>(nStn); // timestamp
Map<String,Float> atHash = new HashMap<String,Float>(nStn); // air temperature
Map<String,Float> stHash = new HashMap<String,Float>(nStn); // surface temperature
Map<String,Float> wsHash = new HashMap<String,Float>(nStn); // wind speed
Map<String,Float> wdHash = new HashMap<String,Float>(nStn); // wind direction
Map<String,Float> gsHash = new HashMap<String,Float>(nStn); // gust speed
Map<String,Float> apHash = new HashMap<String,Float>(nStn); // atm pressure
Map<String,Float> rhHash = new HashMap<String,Float>(nStn); // RH
Map<String,String> rcHash = new HashMap<String,String>(nStn); // road condition
Map<String,Float> whHash = new HashMap<String,Float>(nStn); // Wave height
Map<String,Float> wpHash = new HashMap<String,Float>(nStn); // Wave period
Map<String,Integer> psHash = new HashMap<String,Integer>(nStn); // precipitation status
Map<String,Float> weHash = new HashMap<String,Float>(nStn);

query = "from WUStationRecord where timestamp >= ? and timestamp < ? ";
query += " order by timestamp desc";
List<WUStationRecord> list = hsession.createQuery(query)
	.setTimestamp(0,hourAgo)
	.setTimestamp(1,t)
	.setCacheable(true)
	.list();

for (WUStationRecord r :  list ) {
	String stnId = r.getStationId();
	if (r.getTemperature() != null && ! atHash.containsKey(stnId)) {
		//float tc = (r.getTemperature().floatValue() - 32.0f) * 5.0f / 9.0f;
		//atHash.put (stnId, new Float(tc));
		atHash.put (stnId, WXUtil.f2c(r.getTemperature()));
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindSpeed() != null && ! wsHash.containsKey(stnId)) {
		wsHash.put (stnId, WXUtil.kn2kmph(r.getWindSpeed()) );
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindDirection() != null && ! wdHash.containsKey(stnId)) {
		wdHash.put (stnId, r.getWindDirection());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getAtmosphericPressure() != null && !apHash.containsKey(stnId)) {
		//float hpa = (float)( r.getAtmosphericPressure().floatValue() * 25.4 * 1.33322);
		//apHash.put (stnId, new Float(hpa));
		apHash.put (stnId, WXUtil.inHg2mb(r.getAtmosphericPressure()));
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getRelativeHumidity() != null && !rhHash.containsKey(stnId)) {
		rhHash.put (stnId, r.getRelativeHumidity());
		tsHash.put (stnId, r.getTimestamp());
	}
	
}

// NRA data can frequently lag by 1 - 2 hours. So query for two (three?) hours in past
// Only the most recent record will be used.
query = "from NRAStationRecord where timestamp >= ? and timestamp < ? ";
query += " order by timestamp desc";
List<NRAStationRecord> nralist = hsession.createQuery(query)
	.setTimestamp(0,threeHourAgo)
	.setTimestamp(1,t)
	.setCacheable(true)
	.list();

for (NRAStationRecord r :  nralist ) {
	String stnId = r.getStationId();
	if (r.getAirTemperature() != null && ! atHash.containsKey(stnId)) {
		atHash.put (stnId, r.getAirTemperature());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getRoadTemperature() != null && ! stHash.containsKey(stnId)) {
		stHash.put (stnId, r.getRoadTemperature());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindSpeed() != null && ! wsHash.containsKey(stnId)) {
		// Wind speed already in kmph
		wsHash.put (stnId, r.getWindSpeed());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindDirection() != null && ! wdHash.containsKey(stnId)) {
		wdHash.put (stnId, r.getWindDirection());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getRelativeHumidity() != null && !rhHash.containsKey(stnId)) {
		rhHash.put (stnId, r.getRelativeHumidity());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getRoadCondition() != null && !rcHash.containsKey(stnId)) {
		rcHash.put (stnId, r.getRoadCondition());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getPrecipitationStatus() != null && !psHash.containsKey(stnId)) {
		psHash.put (stnId, r.getPrecipitationStatus());
		tsHash.put (stnId, r.getTimestamp());
	}
}

List<BuoyRecord> buoylist = hsession.createQuery("from BuoyRecord where timestamp >= ? and timestamp < ? "
		+ " order by timestamp desc")
	.setTimestamp(0,hourAgo)
	.setTimestamp(1,t)
	.setCacheable(true)
	.list();
for (BuoyRecord r :  buoylist ) {
	String stnId = r.getStationId();
	if (r.getDryBulbTemperature() != null && ! atHash.containsKey(stnId)) {
		atHash.put (stnId, r.getDryBulbTemperature());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getSeaTemperature() != null && ! stHash.containsKey(stnId)) {
		stHash.put (stnId, r.getSeaTemperature());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getRelativeHumidity() != null && !rhHash.containsKey(stnId)) {
		rhHash.put (stnId, r.getRelativeHumidity());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWaveHeight() != null && ! whHash.containsKey(stnId)) {
		whHash.put (stnId, r.getWaveHeight());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWavePeriod() != null && ! wpHash.containsKey(stnId)) {
		wpHash.put (stnId, r.getWavePeriod());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindSpeed() != null && ! wsHash.containsKey(stnId)) {
		wsHash.put (stnId, WXUtil.kn2kmph(r.getWindSpeed()));
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindDirection() != null && ! wdHash.containsKey(stnId)) {
		wdHash.put (stnId, r.getWindDirection());
		tsHash.put (stnId, r.getTimestamp());
	}
}


List<APRSWXStationRecord> aprsList = hsession
	.createQuery("from APRSWXStationRecord where timestamp >= ? and timestamp < ?" 
	+ " order by timestamp desc")
	.setTimestamp(0,hourAgo)
	.setTimestamp(1,t)
	.setCacheable(true)
	.list();

for (APRSWXStationRecord r : aprsList) {
	String stnId = r.getStationId();
	if (r.getAirTemperature() != null && ! atHash.containsKey(stnId)) {
		float tc = (r.getAirTemperature().floatValue() - 32.0f) * 5.0f / 9.0f;
		atHash.put (stnId, new Float(tc));
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindSpeed() != null && ! wsHash.containsKey(stnId)) {
		wsHash.put (stnId, WXUtil.kn2kmph(r.getWindSpeed()));
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindDirection() != null && ! wdHash.containsKey(stnId)) {
		wdHash.put (stnId, r.getWindDirection());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getAtmosphericPressure() != null && !apHash.containsKey(stnId)) {
		apHash.put (stnId, r.getAtmosphericPressure());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getRelativeHumidity() != null && !rhHash.containsKey(stnId)) {
		rhHash.put (stnId, r.getRelativeHumidity());
		tsHash.put (stnId, r.getTimestamp());
	}
	
	if (r.getPrecipitationLast60m() != null && r.getPrecipitationLast60m().floatValue() > 0) {
		psHash.put (stnId, new Integer(1));
		tsHash.put (stnId, r.getTimestamp());
	}
	
}



List<WXRecord> metarList = hsession
	.createQuery("from METARRecord where timestamp >= ? and timestamp < ?" 
		+ " order by timestamp desc")
	.setTimestamp(0,hourAgo)
	.setTimestamp(1,t)
	.setCacheable(true)
	.list();

for (WXRecord r : metarList) {
	String stnId = r.getStationId();
	if (r.getAirTemperatureCelsius() != null && ! atHash.containsKey(stnId)) {
		atHash.put (stnId, r.getAirTemperatureCelsius());
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindSpeedKnots() != null && ! wsHash.containsKey(stnId)) {
		wsHash.put (stnId, WXUtil.kn2kmph(r.getWindSpeedKnots()));
		tsHash.put (stnId, r.getTimestamp());
	}
	if (r.getWindDirection() != null && ! wdHash.containsKey(stnId)) {
		wdHash.put (stnId, r.getWindDirection());
		tsHash.put (stnId, r.getTimestamp());
	}
	
}


List<TideGaugeRecord> tgList = hsession
	.createQuery("from TideGaugeRecord where timestamp >= ? and timestamp < ? " 
	+ " order by timestamp desc")

	.setTimestamp(0,hourAgo)
	.setTimestamp(1,t)
	.setCacheable(true)
	.list();

for (TideGaugeRecord r : tgList) {
	String stnId = r.getGaugeId();
	if (r.getWaterElevation() != null && ! weHash.containsKey(stnId)) {
		weHash.put (stnId, r.getWaterElevation());
		tsHash.put (stnId, r.getTimestamp());
	}
}



out.clear();
%><?xml version="1.0"?>
<!--  Units:  ws,gs: kmph; ap: hPa; at,st: degC; -->
<!--  Timestamp: <%=hqlf.format(Calendar.getInstance().getTime())%> -->
<!--  QueryTime: <%=hqlf.format(t) %> -->
<%@page import="org.apache.log4j.Logger"%>
<results>
<%
for (String stnId : stnHash.keySet()) {

	// TODO: why is this necessary?
	if (stnId == null) {
		continue;
	}
	
	Station stn = (Station)stnHash.get(stnId);
	if (stn.getLatitude() == null) {
		continue;
	}
	Date ts = (Date)tsHash.get(stnId);
	
	Float at = atHash.get(stnId);
	Float ws = wsHash.get(stnId);
	Float wd = wdHash.get(stnId);
	Float gs = gsHash.get(stnId);
	Float st = stHash.get(stnId);
	Float ap = apHash.get(stnId);
	Float rh = rhHash.get(stnId);
	Float we = weHash.get(stnId);
	
	String rc = (String)rcHash.get(stnId);
	// RC is unpredictable string: make XML safe
	if (rc!=null) {
		rc=rc.replaceAll("&", "&amp;");
	}
	
	Float wh = whHash.get(stnId);
	Float wp = wpHash.get(stnId);
	Integer ps = (Integer)psHash.get(stnId);

	//System.err.println ("*********************" + stnId);
	
	out.write ("<stn id=\"" + XmlUtil.makeSafe(stnId) + "\"");
	if (stn.getName() != null) {
		out.write (" name=\"" 
		+ XmlUtil.makeSafe(stn.getName()) 
		+ "\"");
	}
	
	if (stn.getLatitude() != null && stn.getLongitude() != null) {
		out.write (" lat=\"" + stn.getLatitude());
		out.write ("\" lon=\"" + stn.getLongitude());
		out.write ("\"");
	}
	
	if (ts != null) {
		out.write (" ts=\"" + hqlf.format(ts) + "\"");
	}
	
	if (ap != null) {
		out.write (" ap=\"" + ap + "\"");
	}
	
	if (at != null) {
		out.write (" at=\"" + at + "\"");
	}
	if (st != null) {
		out.write (" st=\"" + st + "\"");
	}
	if (ws != null) {
		out.write (" ws=\"" + ws + "\"");
	}
	if (wd != null) {
		out.write (" wd=\"" + wd + "\"");
	}
	if (rh != null) {
		out.write (" rh=\"" + rh + "\"");
	}
	if (rc != null) {
		out.write (" rc=\"" + rc +"\"");
	}
	if (wh != null) {
		out.write (" wh=\"" + wh +"\"");
	}
	if (wp != null) {
		out.write (" wp=\"" + wp +"\"");
	}
	if (ps != null) {
		out.write (" ps=\"" + ps +"\"");
	}
	if (we != null) {
		out.write (" we=\"" + we +"\"");
	}
	out.write (" />\n");
	
	
}

// Add latest radar data in compact form
MetEireannRadar.writeRadarElement("/var/tmp/latest_radar.gif",out);

tt += System.currentTimeMillis();

System.err.println ("stn-query: " + tt + "ms");
%>
</results>