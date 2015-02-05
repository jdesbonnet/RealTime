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
// This version for the PIC-WEB JSON data
//

//File dataFile = new File ("/var/tmp/electricity.dat");
File dataFile = new File ("/home/joe/e2.dat");

FileReader r = new FileReader(dataFile);
LineNumberReader lnr = new LineNumberReader(r);
TimeSeries series = new TimeSeries("e", Second.class);

String line;
Date t;
int adc,dt;
while (  (line = lnr.readLine()) != null ) {
	
	if (line.length() == 0) {
		continue;
	}
	
	String[] p = line.split(" ");
	
	if (p.length != 2) {
		continue;
	}
	
	t = new Date(Long.parseLong(p[0])*1000L);
	
	dt = (int)((System.currentTimeMillis() - t.getTime())/1000);
	if (  (dt > 24*3600) || (dt < 0*3600) ) {
		continue;
	}
	
	System.err.println (line);
	
	String[] recs = p[1].split(";");
	
	if (recs.length < 2) {
			continue;
	}
	String[] fields = recs[1].split(",");
	
	adc = Integer.parseInt(fields[1],16);
	
	Second ts = new Second(t);
	series.addOrUpdate(ts, adc);
	
	//out.println (t + " " + adc + "<br>\n");
	
	
}


TimeSeriesCollection tsc = new TimeSeriesCollection();
tsc.addSeries(series);

String xLabel="Time (UTC)";
String yLabel="Current ADC value";
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