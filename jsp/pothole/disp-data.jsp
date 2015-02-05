<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.Arrays"%><%@include file="_header.jsp"%><%
	response.setContentType("text/plain");
	File dataFile = new File (dataDir, request.getParameter("id"));



//Process sensor log to get change in orientation (movement) vs time
FileReader r = new FileReader(dataFile);
LineNumberReader lnr = new LineNumberReader(r);

int i = 0;
float[] po = new float[3];
float[] o = new float[3];
float[] deltao = new float[3];

double d;
float d2;
long t=0,t0=0;

String line;
double lat,lon;
float gx,gy,gz,g2;
double g;
while (  (line = lnr.readLine()) != null ) {
	String[] p = line.split(" ");
	if (p.length == 0) {
		continue;
	}
	t = Long.parseLong(p[1]);
	if (t0 == 0) {
		t0 = t;
	}
	if ("L".equals(p[0])) {
		lat = Double.parseDouble(p[2]);
		lon = Double.parseDouble(p[3]);
		//out.write (lat + ", " + lon + "<br>\n");
	} else if ("G".equals(p[0])) {
		gx = Float.parseFloat (p[2]);
		gy = Float.parseFloat (p[3]);
		gz = Float.parseFloat (p[4]);
		g2 = gx*gx +gy*gy + gz*gz;
		g = Math.sqrt((double)g2);
		out.write ( (t-t0) + " " + g + "\n");
	}
	
}

%>
