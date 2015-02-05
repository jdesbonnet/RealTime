<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="org.apache.commons.net.ftp.FTP"%>
<%@page import="org.apache.commons.net.ftp.FTPFile"%>
<%@page import="org.apache.commons.net.ftp.FTPClient"%>

<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="java.util.List"%>
<%@page import="ie.wombat.rt.tg.TideGaugeRecord"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Map"%>
<ul>
<%
SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

Calendar cal = Calendar.getInstance();

Date now = cal.getTime();

cal.set(Calendar.HOUR,0);
cal.set(Calendar.MINUTE,0);
cal.set(Calendar.SECOND,0);
cal.set(Calendar.MILLISECOND,0);
cal.add(Calendar.HOUR, -96);

Date yesterdayMidnight = cal.getTime();

long t = -System.currentTimeMillis();

Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();
List<TideGaugeRecord> list = hsession.createQuery("from TideGaugeRecord where timestamp > ?")
	.setDate(0,yesterdayMidnight)
	.setCacheable(true)
	.list();

Map<String,String> tsHash = new HashMap<String,String>(list.size());
for (TideGaugeRecord r : list) {
	String key = r.getGaugeId() +  df.format(r.getTimestamp());
	tsHash.put (key, "true");
	out.println ("key=" + key + "<br>");
	out.flush();
}

tx.commit();

t += System.currentTimeMillis();

System.err.println ("poll-tide-gauge.jsp: time to exec initial query: " + t + "ms");

int reply;

FTPClient ftpclient = new FTPClient();

ftpclient.connect("ftp.marine.ie");

reply = ftpclient.getReplyCode();

out.println ("connected! reply=" + reply + "<br>");
out.flush();

ftpclient.login("dcmnr","Marine06");

out.println ("logged in!<br>");
out.flush();


ftpclient.enterLocalPassiveMode();

out.println ("passive mode<br>");
out.flush();

ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
out.println ("binary xfer mode<br>");
out.flush();


ftpclient.changeWorkingDirectory("/oss/Irish Tides Test/");

out.println ("reading files...<br>");
out.flush();

// location, sensor, unit
String[] sensors = {
	"GALWAY___1,0002,m",
	//"GALWAY___1,0004,hPa",
	"INISH____1,0001,m",
	"SKERRIES_1,0001,m",
	//"SKERRIES_1,0004,hPa",
	"KISH_____1,0001,m", // 0001: bubbler, 0002 under water pressure
	"KISH_____2,0002,m", // underwater pressure sensor
	"DODDER___1,0001,m",
	"LIFFEY___1,0001,m",
	"0000041118,0007,cm", // Castletownbear
	"DUBLINPORT,0002,m",
	"HOWTH____1,0001,m",
	"KBEGS____1,0001,m",
	"WEX______1,0001,m",
	//"MALIN____1,0003,hPa"
	"MALIN____1,0004,m"
	//"ARANMORE_1,0001,m"
};


String today;

if (request.getParameter("date") != null) {
	if ("all".equals(request.getParameter("date"))) {
		today = "";
	} else {
		today = request.getParameter("date");
	}
} else {
	today = df.format(now).substring(0,8);
}

HashMap processStnHash = null;
if (request.getParameter("stations") != null) {
	processStnHash = new HashMap();
	String[] stns = request.getParameter("stations").split(",");
	for (int i = 0; i < stns.length; i++) {
		processStnHash.put (stns[i],"true");
	}
}
out.println ("today="+today+"<br>");


for (int k = 0; k < sensors.length; k++) {
	String[] p = sensors[k].split(",");
	String location = p[0];
	String sensor = p[1];
	String unit = p[2];

	if (processStnHash != null && !processStnHash.containsKey(location)) {
		out.println ("skipping station " + location + "<br>");
		continue;
	}


//FTPFile[] files = ftpclient.listFiles(location+sensor+today+"*");
FTPFile[] files = ftpclient.listFiles(location+sensor+"*");

for (int i = 0; i < files.length; i++) {
	out.println ("<li>" + files[i].getName());
	out.flush();
	
	ByteArrayOutputStream baout = new ByteArrayOutputStream();
	
	ftpclient.retrieveFile(files[i].getName(), baout);
	
	String dataStr = new String(baout.toByteArray());
	String[] lines = dataStr.split("\r\n");
	out.println ("<ul>");
	
	tx = hsession.beginTransaction();
	
	for (int j = 0; j < lines.length; j++) {
		if (lines[j].length() < 15) {
			continue;
		}
		String dateStr = lines[j].substring(0,8);
		String timeStr = lines[j].substring(9,15);
		String weStr = lines[j].substring(16);
		
		String tsStr = dateStr + timeStr;
		
		Date ts = df.parse(tsStr);
		
		out.println ("<li> " + lines[j] + " date=" + dateStr 
		+ " time=" + timeStr + " ts=" + ts + " we=" + weStr);
		
		String key = location + tsStr;
		
		if (tsHash.containsKey(key)) {
			out.println (" EXISTS!");
			continue;
		} else {
			out.println (" key=" + key  + " does not exist");
			out.flush();
		}
		
		Float we;
		try {
			we = new Float(weStr);
			if ("cm".equals(unit)) {
				we = new Float(we.floatValue()/100);
			}
		} catch (NumberFormatException e) {
			out.println ("<span style=\"color:red\">Error: " + e.getMessage() + "</span>");
			continue;
		}
		
		
		TideGaugeRecord r = new TideGaugeRecord ();
		r.setGaugeId(location);
		r.setTimestamp(ts);
		r.setWaterElevation(we);
		hsession.save(r);
		System.err.println ("saving TideGaugeRecord loc=" + r.getGaugeId() + " ts=" 
		+ r.getTimestamp());
	}
	out.println ("</ul>");
	tx.commit();
	
} // next record

} // next sensor

out.println ("disconnect<br>");
out.flush();

ftpclient.disconnect();
%>
</ul>
done.
