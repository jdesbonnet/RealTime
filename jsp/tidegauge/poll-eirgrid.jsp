
<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="org.apache.commons.httpclient.HttpClient"%>
<%@page import="org.apache.commons.httpclient.methods.GetMethod"%>

<%@page import="ie.wombat.rt.eirgrid.PowerRecord"%>
<%@page import="java.util.TimeZone"%>
<%@page import="ie.wombat.util.XmlUtil"%>
<ul>
<%


String tableStartMarker;
String tableEndMarker;
String url;

String table = request.getParameter("table");
if (table == null || "WindGeneration".equals(table)) {
	url = "http://www.eirgrid.com/EirgridPortal/DesktopDefault.aspx?tabid=Wind%20Generation&TreeLinkModID=2314&TreeLinkItemID=57";
	tableStartMarker = "id=\"_ctl2_lstWindGen\"";
	tableEndMarker = "</table>";
	table = "WindGeneration";
} else if ("SystemDemand".equals(table)) {
	url = "http://www.eirgrid.com/EirgridPortal/DesktopDefault.aspx?tabid=System%20Demand&TreeLinkModID=2314&TreeLinkItemID=57";
	tableStartMarker = "id=\"_ctl2_lstSysDemand\"";
	tableEndMarker = "</TABLE>";
} else if ("ForecastDemand".equals(table)) {
	url = "http://www.eirgrid.com/EirgridPortal/DesktopDefault.aspx?tabid=Forecast%20Demands&TreeLinkModID=1875&TreeLinkItemID=244";
	tableStartMarker = "id=\"_ctl2_DataGrid1\"";
	tableEndMarker = "</table>";
} else {
	throw new ServletException ("unrecognized table " + table);
}





SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

Calendar cal = Calendar.getInstance();

Date now = cal.getTime();

cal.set(Calendar.HOUR_OF_DAY,0);
cal.set(Calendar.MINUTE,0);
cal.set(Calendar.SECOND,0);
cal.set(Calendar.MILLISECOND,0);
//cal.add(Calendar.HOUR, -24);

Date yesterdayMidnight = cal.getTime();


Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();


HashMap tsHash = new HashMap(200);
String entity = table + "Record";
String entityClassName  = "ie.wombat.rt.eirgrid." + entity;

List list = hsession.createQuery("from " + entity + " where timestamp >= ?").setDate(0,yesterdayMidnight).list();
Iterator iter = list.iterator();
while (iter.hasNext()) {
	PowerRecord r = (PowerRecord)iter.next();
	tsHash.put (r.getTimestamp(),r);
}

HttpClient client = new HttpClient();


GetMethod get = new GetMethod(url);

int status = client.executeMethod(get);

if (status != 200) {
	throw new ServletException ("Server returned status " + status);
}

String responseBody = get.getResponseBodyAsString();

// Eleminate crud leading up to table
int tableStartIndex = responseBody.indexOf(tableStartMarker);
out.println ("table found at " + tableStartIndex + "<br>");
responseBody = responseBody.substring(tableStartIndex);
int tableEndIndex = responseBody.indexOf(tableEndMarker);
out.println ("table end at " + tableEndIndex + "<br>");
responseBody = responseBody.substring(0,tableEndIndex);

/*
out.println ("<pre>");
out.println ("responseBody.length="+ responseBody.length());
out.println (XmlUtil.makeSafe(responseBody));
out.println ("</pre>");
*/


responseBody = responseBody.replaceAll("[^0-9:]"," ");


String[] p = responseBody.split("\\s+");


for (int i = 0; i < p.length; i++) {
	out.println ("<li> i="+i+ " " + p[i]);
}



out.flush();

// look for "00:00"
int startIndex = -1;
for (int i = 0; i < p.length; i++) {
	if ("00:00".equals(p[i])) {
		startIndex = i;
		break;
	}
}

if (startIndex == -1) {
	throw new ServletException ("data not found, no 00:00 time found");
}

out.println ("startIndex=" + startIndex);


// Times are in local time and not UTC (bad idea!)
Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
Calendar ltCal = Calendar.getInstance(TimeZone.getTimeZone("GB-Eire"));

ltCal.set(Calendar.SECOND,0);
ltCal.set(Calendar.MILLISECOND,0);

int pinc;
int poffset;
if ("ForecastDemand".equals(table)) {
	pinc = 5;
	poffset = 4;
	// tau is 3 days
	ltCal.add(Calendar.HOUR_OF_DAY,24 * 3);
} else {
	pinc = 2;
	poffset = 1;
}


int h,m;
for (int i = startIndex; i < p.length; i+=pinc) {
	String time = p[i];
	h = Integer.parseInt(time.substring(0,2));
	m = Integer.parseInt(time.substring(3,5));
	int power = Integer.parseInt(p[i+poffset]);
	
	ltCal.set(Calendar.HOUR_OF_DAY, h);
	ltCal.set(Calendar.MINUTE,m);
	
	utcCal.setTime(ltCal.getTime());
	Date uts = utcCal.getTime();
	
	out.println ("<li> ts=" + uts + " h=" + h + " m=" + m + " power=" + power);
	
	if (tsHash.containsKey(uts)) {
		out.println ("ALREADY EXISTS");
	} else {
		PowerRecord r = (PowerRecord)Class.forName(entityClassName).newInstance();
		r.setTimestamp(uts);
		r.setPower(new Float(power));
		hsession.save(r);
	}
	
}


%>