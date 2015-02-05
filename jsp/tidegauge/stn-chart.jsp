<%@page 
contentType="image/png"
import="java.io.OutputStream"
import="ie.wombat.template.Context"
import="ie.wombat.template.TemplateRegistry"
import="org.jfree.chart.JFreeChart"
import="org.jfree.chart.ChartUtilities"
import="ie.wombat.rt.wx.MakeStnChart"
%><%!public final static int MAX_WIDTH=640;
public final static int MAX_HEIGHT=480;
public final static int DEFAULT_WIDTH=320;
public final static int DEFAULT_HEIGHT=180;%><%response.setContentType("image/png");
int width = DEFAULT_WIDTH;
try {
	width = Integer.parseInt(request.getParameter("width"));
} catch (Exception e) {
	// ignore
}

int height = DEFAULT_HEIGHT;
try {
	height = Integer.parseInt(request.getParameter("height"));
} catch (Exception e) {
	// ignore
}

if (width > MAX_WIDTH) {
	width = MAX_WIDTH;
}

if (height > MAX_HEIGHT) {
	height = MAX_HEIGHT;
}

int nhour = 24;
try {
	nhour = Integer.parseInt(request.getParameter("nhour"));
} catch (Exception e) {
	// ignore
}

String what = "at";
if (request.getParameter("what") != null) {
	what=request.getParameter("what");
}
String stnId = request.getParameter("stnid");

JFreeChart chart = MakeStnChart.createChart (nhour,stnId, what);

OutputStream sout = response.getOutputStream();
ChartUtilities.writeChartAsPNG(sout, chart, width, height);%>