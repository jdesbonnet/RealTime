<%@page 
import="java.io.OutputStream"
import="java.io.LineNumberReader"
import="java.io.FileReader"
import="java.io.File"
import="java.util.Date"
import="java.util.Arrays"
import="java.util.ArrayList"
import="org.jfree.data.time.TimeSeriesCollection"
import="org.jfree.chart.ChartUtilities"
import="org.jfree.data.time.Second"
import="org.jfree.data.time.TimeSeries"
import="org.jfree.chart.JFreeChart"
import="org.jfree.chart.ChartFactory"
%><%
//
// 
//

//File dataFile = new File ("/var/tmp/ef.dat");
File dataFile = new File ("/home/joe/workspace/RealTime/scripts/gasrefl.dat");

FileReader r = new FileReader(dataFile);
LineNumberReader lnr = new LineNumberReader(r);
TimeSeries v0series = new TimeSeries("v0", Second.class);
TimeSeries dvseries = new TimeSeries("dv", Second.class);
TimeSeries dseries = new TimeSeries("d", Second.class);

String line;
Date t;
int dt,dv,d,v0;
while (  (line = lnr.readLine()) != null ) {
	
	if (line.length() == 0) {
		continue;
	}
	
	String[] p = line.split("\t");
	
	if (p.length != 2) {
		continue;
	}
	
	try {
		t = new Date(Long.parseLong(p[0])*1000L);
	} catch (Exception e) {
			continue;
	}
	
	dt = (int)((System.currentTimeMillis() - t.getTime())/1000);
	if (  (dt > 24*3600) || (dt < 0*3600) ) {
		//continue;
	}
	
	System.err.println (line);
	
	
	String[] bytes = p[1].trim().split(" ");
	if (bytes.length != 7) {
		continue;
	}
	
	if (! "52".equals(bytes[0]) ) {
			continue;
	}
	
	dv = Integer.parseInt(bytes[2],16) << 8;
	dv += 	Integer.parseInt(bytes[3],16);
	
	v0 = Integer.parseInt(bytes[4],16) << 8;
	v0 += 	Integer.parseInt(bytes[5],16);
	
	d = v0 - dv;
	
	Second ts = new Second(t);
	
	if (v0 < 5000) {
		v0series.addOrUpdate(ts, v0);
	}
	
	if (dv < 5000) {
		dvseries.addOrUpdate(ts, dv);
	}
	
	if (d > -2000 && d < 2000) {
		dseries.addOrUpdate(ts,v0-dv);
	}
	
	
}


TimeSeriesCollection tsc = new TimeSeriesCollection();
tsc.addSeries(v0series);
tsc.addSeries(dvseries);
tsc.addSeries(dseries);

String xLabel="Time (UTC)";
String yLabel="Photocurrent (C discharge time)";
int width = 640;
int height = 320;
JFreeChart chart = ChartFactory.createTimeSeriesChart(null, // title
		xLabel, // x-axis label
		yLabel, // y-axis label
		tsc, // data
		false, // create legend?
		true, // generate tooltips?
		false // generate URLs?
		);
response.setContentType("image/png");
OutputStream sout = response.getOutputStream();
ChartUtilities.writeChartAsPNG(sout, chart, width, height);%>