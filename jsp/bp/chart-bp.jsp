
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%><%@page
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
import="ie.wombat.rt.bp.Charts"
%><%

File dataFile = new File (request.getParameter("f"));

BufferedReader lnr = new BufferedReader(new FileReader(dataFile));

JFreeChart chart = Charts.makeChart(lnr);
OutputStream sout = response.getOutputStream();
response.setContentType("image/png");
ChartUtilities.writeChartAsPNG(sout, chart, 640, 480 );
%>