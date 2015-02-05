<%@page import="ie.wombat.rt.webcam.Webcam"%>
<%@page import="jpl.Ephemeris"%>
<%@page import="java.util.Calendar"%>
<%@page import="ie.wombat.astro.Astro"%>
<%@page import="ie.wombat.astro.AstroDate"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%!
private static double DEG2RAD = Math.PI / 180;
private static double RAD2DEG = 180 / Math.PI;

private static Ephemeris eph = new Ephemeris(
		"/home/joe/Desktop/ssd.jpl.nasa.gov/pub/eph/planets/ascii/de405");


// Harbour Cam
private static double latitudeDeg = 53.269066;
private static double longitudeDeg = -9.0472358;

private static double sinLatitude = Math.sin(latitudeDeg*DEG2RAD);
private static double cosLatitude = Math.cos(latitudeDeg*DEG2RAD);

private static double width = 640;
private static double height = 480;

%> 
<%

double azc = Double.parseDouble(request.getParameter("azc"));
double altc = Double.parseDouble(request.getParameter("altc"));
double fov = Double.parseDouble(request.getParameter("fov"));
double rot = Double.parseDouble(request.getParameter("rot"));



Webcam webcam = new Webcam (
		latitudeDeg, longitudeDeg,
		azc, altc, rot, // azc altc and rotation (degrees)
		width,height, // webcam image dimenaions (pixels)
		fov // fov (degrees)
		);

if (request.getParameter("mapping") != null) {
	webcam.setMappingModel(Integer.parseInt(request.getParameter("mapping")));
}

String strokeParam = " opacity=\"0.5\" stroke-width=\"2\" fill=\"none\" stroke-dasharray=\"3,3\" ";
String textParam = " style=\"font-size: 16px\" ";

	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
	String ts = request.getParameter("ts");
	Date date = df.parse(ts);
	long t0 = date.getTime();
	
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);

	double JD = AstroDate.calcJD(date);

	// double h = Astro.calcLocalMeanSiderealTime(MJD, longitudeDeg) *
	// Math.PI/12;

	int i = 0, j = 0;
	double TAU;
	double jd;

	double[][] dr = new double[12][4];
	double[] d = new double[12];
	double[] ra = new double[12];
	double[] dec = new double[12];
	double[] alta = new double[12];
	double[] aza = new double[12];

	out.clear();
	response.setContentType("text/plain");
	
	out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	out.write("<svg version=\"1.0\" width=\"640px\" height=\"480px\" \n");
	out.write(" xmlns:svg=\"http://www.w3.org/2000/svg\"\n");
	out.write(" xmlns=\"http://www.w3.org/2000/svg\" >\n<g>\n");

	
	boolean firstPointSun = true;
	boolean firstPointMoon = true;
	boolean firstPointVenus = true;

	StringBuffer sunPath = new StringBuffer();
	StringBuffer moonPath = new StringBuffer();
	StringBuffer venusPath = new StringBuffer();
	StringBuffer moonPos = new StringBuffer();
	StringBuffer sunPos = new StringBuffer();
	StringBuffer venusPos = new StringBuffer();
	
	double x,y,az,alt;
	double[] xy;
	
	for (jd = JD; jd < (JD + 1.0); jd += (1.0 / (24 * 4))) {
		eph.planetary_ephemeris(jd);
		
		date.setTime(t0 + (long)((jd-JD)*24*3600000));

		// double MJD = AstroDate.JDToMJD(JD + (65.184/86400.0) );
		double mjd = AstroDate.JDToMJD(jd);

		//double TAU;

		// Local Mean Sidereal Time in degrees
		double lmst = 15.0 * Astro.calcLocalMeanSiderealTime(mjd,
		webcam.getLongitudeDeg());
		double lmstRad = lmst * DEG2RAD;

		for (i = 1; i <= 11; i++) {
			for (j = 1; j <= 3; j++) {
				dr[i][j] = eph.planet_r[i][j] - eph.planet_r[3][j];
				d[i] = Math.sqrt(dr[i][1] * dr[i][1] + dr[i][2]
					* dr[i][2] + dr[i][3] * dr[i][3]);
				ra[i] = Math.atan2(dr[i][2], dr[i][1]);
				dec[i] = Math.asin(dr[i][3] / d[i]);

				TAU = lmstRad - ra[i];
				
				alta[i] = Math.asin(sinLatitude * Math.sin(dec[i])
					+ cosLatitude * Math.cos(dec[i])
					* Math.cos(TAU));
				
			
				
				aza[i] = Math.atan2 (-Math.sin(TAU)*Math.cos(dec[i]), 
						cosLatitude*Math.sin(dec[i]) - sinLatitude*Math.cos(dec[i])*Math.cos(TAU) );
				
				 
					
			}
		}
		
		System.err.print ("jd=" + jd );
		System.err.print (" t=" + tf.format(date));
		//System.err.print (" moonAlt=" + (int)(alta[10] * RAD2DEG));
		System.err.print (" sunAlt=" + (int)(alta[11] * RAD2DEG));
		//System.err.print (" venusAlt=" + (int)(alta[2] * RAD2DEG));
		//System.err.print (" moonAz=" + (int)(aza[10] * RAD2DEG));
		System.err.print (" sunAz=" + (int)(aza[11] * RAD2DEG));
		
		System.err.print (" sunRA=" + (int)(ra[11] * RAD2DEG));
		
		System.err.print (" sunDEC=" + (int)(dec[11] * RAD2DEG));
		//System.err.print (" venusAz=" + (int)(aza[2] * RAD2DEG));
		System.err.println ("");
		
		
		az = aza[11]; alt = alta[11];
		xy = webcam.toXY(alt,az);
		x = xy[0]; y = xy[1];
		
		//if (x >  0 && x < width && y > 0 && y < height) {
			if (firstPointSun) {
				sunPath.append ("  M " + x + " " + y);
				firstPointSun = false;
			} else {
				sunPath.append ("  L " + x + " " + y);
			}
			
			if (alt >= 0) {
				venusPos.append ("<circle fill=\"red\" r=\"4\" cx=\"" + x + "\" cy=\"" + y + "\"/>\n"); 
				sunPos.append ("<text stroke=\"black\" " + textParam + " x=\"" + (x+5) + "\" y=\"" + (y+5) + "\">");
				sunPos.append (tf.format(date) 
					//+ " a="
					//+ (int)(alta[11] * RAD2DEG)
					//+ " az="
					//+ (int)(aza[11] * RAD2DEG)
					+ "</text>\n");
			}
			
		//} else {
		//	firstPointSun = true;
		//}
		//}
		
		// Moon
		az = aza[10]; alt = alta[10];
		xy = webcam.toXY(alt,az);
		x = xy[0]; y = xy[1];
		if (x >  0 && x < width && y > 0 && y < height) {
			if (firstPointMoon) {
				moonPath.append ("  M " + x + " " + y);
				firstPointMoon = false;
			} else {
				moonPath.append ("  L " + x + " " + y);
			}
		} else {
			firstPointMoon = true;
		}
		if (alt >= 0) {
			moonPos.append ("<circle fill=\"yellow\" r=\"4\" cx=\"" + x + "\" cy=\"" + y + "\"/>\n"); 
			moonPos.append ("<text stroke=\"black\" " 
					+ textParam + " x=\"" + (x-40) + "\" y=\"" + (y+5) + "\">");
			moonPos.append (tf.format(date) 
					+ "</text>\n");
		}
		
		
		// Venus
		az = aza[2]; alt = alta[2];
		xy = webcam.toXY(alt,az);
		x = xy[0]; y = xy[1];
		if (firstPointVenus) {
			venusPath.append ("  M " + x + " " + y);
			firstPointVenus = false;
		} else {
			venusPath.append ("  L " + x + " " + y);
		}
		
		if (alt >= 0) {
			venusPos.append ("<circle fill=\"blue\" opacity=\"0.6\" r=\"4\" cx=\"" + x + "\" cy=\"" + y + "\"/>\n"); 
			venusPos.append ("<text stroke=\"black\" opacity=\"0.6\" " + textParam + " x=\"" + (x+5) + "\" y=\"" + y + "\">");
			venusPos.append (tf.format(date) 
				//+ " a="
				//+ (int)(alta[2] * RAD2DEG)
				//+ " az="
				//+ (int)(aza[2] * RAD2DEG)
				+ "</text>\n");
		}
		
		// next time of day
	}
	
	
	
	out.write("<path stroke=\"red\" stroke-width=\"1\" fill=\"none\" d=\"");
	out.write (sunPath.toString());
	out.write(" \" />\n");
	
	out.write("<path stroke=\"yellow\" stroke-width=\"1\" fill=\"none\" d=\"");
	out.write (moonPath.toString());
	out.write(" \" />\n");
	
	out.write("<path stroke=\"blue\" opacity=\"0.3\" stroke-width=\"1\" fill=\"none\" d=\"");
	out.write (venusPath.toString());
	out.write(" \" />\n");

	out.write (moonPos.toString());
	out.write (sunPos.toString());
	out.write (venusPos.toString());

	// Horizon line
	out.write ("<!-- Horizon -->\n");
	out.write ("<path stroke=\"white\" stroke-width=\"1\" fill=\"none\" d=\"");
	xy = webcam.toXY(0, (azc - 45) *DEG2RAD);
	x = xy[0]; y = xy[1];
	out.write ("  M " + x + " " + y);
	for (az = (azc-45); az < (azc+45); az += 5) {
		xy = webcam.toXY(0,az*DEG2RAD);
		x = xy[0]; y = xy[1];
		out.write ("  L " + x + " " + y);
		out.write ("  L " + x + " " + (y+3));
		out.write ("  L " + x + " " + (y-3));
		out.write ("  M " + x + " " + y);
	}
	out.write(" \" />\n");
	for (az = (azc-45); az < (azc+45); az += 5) {
		xy = webcam.toXY(0,az*DEG2RAD);
		x = xy[0]; y = xy[1];
		out.write ("<text stroke=\"white\"" + textParam 
				+ " x=\"" + (x-15) +  "\" y=\"" + (y+20) + "\">" + az + "</text>\n"); 
	}
	
	// North line
	out.write ("<!-- North -->\n");
	out.write ("<path stroke=\"white\" stroke-width=\"1\" fill=\"none\" d=\"");
	az = -45 * DEG2RAD;
	xy = webcam.toXY(-45,az);
	x = xy[0]; y = xy[1];
	out.write ("  M " + x + " " + y);
	for (alt = -45*DEG2RAD; alt < 45*DEG2RAD; alt += 5*DEG2RAD) {
		xy = webcam.toXY(alt,az);
		x = xy[0]; y = xy[1];
		out.write ("  L " + x + " " + y);
		out.write ("  L " + (x+3) + " " + y);
		out.write ("  L " + (x-3) + " " + y);
		out.write ("  M " + x + " " + y);
	}
	out.write(" \" />\n");
	out.write("</g>\n");

	out.write("</svg>\n");
%>