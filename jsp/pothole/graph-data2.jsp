<%@page
import="java.io.File"
import="java.io.BufferedReader" 
import="java.io.OutputStream"
import="java.io.InputStreamReader"
import="java.io.FileInputStream"
import="java.io.RandomAccessFile"
import="java.io.LineNumberReader"
import="java.io.FileReader"
import="org.jfree.chart.JFreeChart"
import="org.jfree.chart.ChartUtilities"
import="ie.wombat.rt.pothole.Charts"
%><%
File dataDir = new File ("/var/tmp");
int chartWidth = 1400;
try {
	chartWidth = Integer.parseInt(request.getParameter("w"));
} catch (Exception e) {
	// ignore
}

String id = request.getParameter("id");
File dataFile = new File (dataDir, id);

long t = Long.parseLong(request.getParameter("t"));

BufferedReader lnr = new BufferedReader(new FileReader(dataFile));

JFreeChart chart = Charts.makeChart(lnr, t-2500, t+2500);
OutputStream sout = response.getOutputStream();
response.setContentType("image/png");
ChartUtilities.writeChartAsPNG(sout, chart, 1024, 240);
%>