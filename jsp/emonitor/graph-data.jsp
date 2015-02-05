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
//File dataFile = new File ("/var/tmp/electricity.dat");
File dataFile = new File ("/home/joe/workspace/RealTime/scripts/electricity.dat");

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
	
	String[] p = line.split("\t");
	
	if (p.length != 2) {
		continue;
	}
	
	// Due to firmware but, errors were not terminated with CRLF
	// Remove E04 errors from start of lines
	while (p[1].startsWith("E04")) {
		p[1] = p[1].substring(3);
	}
	
	if (p[1].trim().length() != 23) {
		continue;
	}
	
	t = new Date(Long.parseLong(p[0])*1000L);
	
	dt = (int)((System.currentTimeMillis() - t.getTime())/1000);
	if (  (dt > 6*3600) || (dt < 0*3600) ) {
		continue;
	}
	

	
	String[] bytes = p[1].trim().split(" ");
	if (bytes.length != 8) {
		continue;
	}
	adc = (Integer.parseInt(bytes[3],16) & 0x0f) << 8;
	adc += 	Integer.parseInt(bytes[4],16);
	
	
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
