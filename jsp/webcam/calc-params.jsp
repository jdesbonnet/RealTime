<%@page import="ie.wombat.astro.Astro"%>
<%@page import="ie.wombat.astro.AstroDate"%>
<%@page import="ie.wombat.astro.PlanetEphemeris"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%!
	private static final double DEG2RAD = Math.PI/180;
	private static final double RAD2DEG = 180/Math.PI;

	private static double[] raDecToAltAz (Date date, 
				double latitudeDeg, double longitudeDeg, 
				double ra, double dec) {
		double sinLatitude = Math.sin(latitudeDeg*DEG2RAD);
		double cosLatitude = Math.cos(latitudeDeg*DEG2RAD);
		
		double JD = AstroDate.calcJD(date);
		double MJD = AstroDate.JDToMJD(JD);
		double lmst = 15.0 * Astro.calcLocalMeanSiderealTime(MJD, longitudeDeg);
		double lmstRad = lmst * Math.PI / 180;
		
		double TAU = lmstRad - ra;
		double alt = Math.asin(sinLatitude * Math.sin(dec)
			+ cosLatitude * Math.cos(dec) * Math.cos(TAU));
	
		double az = Math.atan2 (- Math.sin(TAU)*Math.cos(dec), 
			cosLatitude*Math.sin(dec) - sinLatitude*Math.cos(dec)*Math.cos(TAU) 
			);
		
		double[] ret = new double[2];
		ret[0] = alt;
		ret[1] = az;
		return ret;
	}

%>
<%

	SimpleDateFormat dtf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

	double latitudeDeg = Double.parseDouble(request.getParameter("latitude"));
	double longitudeDeg= Double.parseDouble(request.getParameter("longitude"));
	
	double x1 = Double.parseDouble(request.getParameter("x1"));
	double y1 = Double.parseDouble(request.getParameter("y1"));
	double x2 = Double.parseDouble(request.getParameter("x2"));
	double y2 = Double.parseDouble(request.getParameter("y2"));
	
	int objId1 = Integer.parseInt(request.getParameter("obj1"));
	int objId2 = Integer.parseInt(request.getParameter("obj2"));
	
	String ts1 = request.getParameter("ts1");
	String ts2 = request.getParameter("ts2");
	
	Date d1 = dtf.parse(ts1);
	Date d2 = dtf.parse(ts2);
	
	PlanetEphemeris pe = new PlanetEphemeris();
	
	double[] raa= new double [12];
	double[] deca = new double[12];
	
	double ra1,dec1,ra2,dec2,az1,alt1,az2,alt2;
	
	// First observation
	
	pe.getRaDec(d1,raa,deca);
	
	ra1 = raa[objId1];
	dec1 = deca[objId1];
	
	double[] aa;
	
	aa = raDecToAltAz(d1,latitudeDeg,longitudeDeg,ra1,dec1);
	alt1 = aa[0];
	az1 = aa[1];
	
	
	// Second observation
	
	pe.getRaDec(d2,raa,deca);
	
	ra2 = raa[objId2];
	dec2 = deca[objId2];
	
	
	aa = raDecToAltAz(d2,latitudeDeg,longitudeDeg,ra2,dec2);
	alt2 = aa[0];
	az2 = aa[1];
	
%>
<table>
<tr>
<td>
x1=<%=x1 %><br>
y1=<%=y1 %><br>
ra1=<%=ra1*RAD2DEG%><br>
dec1=<%=dec1*RAD2DEG%><br>
alt1=<%=alt1*RAD2DEG %><br>
az1=<%=az1*RAD2DEG %><br>
</td>
<td>
x2=<%=x2 %><br>
y2=<%=y2 %><br>
ra2=<%=ra2*RAD2DEG%><br>
dec2=<%=dec2*RAD2DEG%><br>
alt2=<%=alt2*RAD2DEG %><br>
az2=<%=az2*RAD2DEG %><br>
</td>
</tr>
</table>

<% 
	double dx = x1 - x2;
	double dy = y1 - y2;
	double daz = az1 - az2;
	double dalt = alt1 - alt2;
	double A = alt1 + alt2;
	
	double rotAng = Math.atan (  (dy*daz - dx*dalt) / (dx*daz + dx*A));
	double s = dx / ( (daz - alt2 * Math.tan(rotAng)) * Math.cos(rotAng));
	

	// Not accurate -- rot not accounted for
	double azc = az1 - (x1/s);
	double altc = alt1 - (y1/s);
	
%>

theta=<%=rotAng*RAD2DEG%> deg <br>
s=<%=s%> <br>
azc=<%=azc*RAD2DEG%> deg<br> 
altc=<%=altc*RAD2DEG%> deg<br>
