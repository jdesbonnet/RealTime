<%@page import="jpl.Ephemeris"%>
<%@page import="java.util.Calendar"%>
<%@page import="ie.wombat.astro.Astro"%>
<%@page import="ie.wombat.astro.AstroDate"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%!
	private static Ephemeris eph = new Ephemeris ("/home/joe/Desktop/ssd.jpl.nasa.gov/pub/eph/planets/ascii/de405");
%>
<%
	double DEG2RAD = Math.PI/ 180;
	double RAD2DEG = 180 / Math.PI;

	
	SimpleDateFormat cidf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String ts = request.getParameter("ts");

		Date date = cidf.parse(ts);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		// Doc cam
		double latitudeDeg = 53.269066;
		double longitudeDeg = -9.0472358;

		double cosLatitude = Math.cos(latitudeDeg * DEG2RAD);
		double sinLatitude = Math.sin(latitudeDeg * DEG2RAD);

		double JD = AstroDate.calcJD(date);

		// double MJD = AstroDate.JDToMJD(JD + (65.184/86400.0) );
		double MJD = AstroDate.JDToMJD(JD);

		double TAU;

		// Local Mean Sidereal Time in degrees
		double lmst = 15.0 * Astro.calcLocalMeanSiderealTime(MJD, longitudeDeg);
		double lmstRad = lmst * Math.PI / 180;

		// double h = Astro.calcLocalMeanSiderealTime(MJD, longitudeDeg) *
		// Math.PI/12;

		int i = 0, j = 0;

		

		/*
		 * This is the call to "planetary_ephemeris", which will put planetary
		 * positions into the array "planet_r", and planetary velocities into
		 * the array "planet_rprime".
		 */
		eph.planetary_ephemeris(JD);

		double[][] dr = new double[12][4];
		double[] d = new double[12];
		double[] ra = new double[12];
		double[] dec = new double[12];
		double[] alta = new double[12];
		double[] aza = new double[12];

		for (i = 1; i <= 11; i++) {
			for (j = 1; j <= 3; j++) {
				dr[i][j] = eph.planet_r[i][j] - eph.planet_r[3][j];
				d[i] = Math.sqrt(dr[i][1] * dr[i][1] + dr[i][2] * dr[i][2]
						+ dr[i][3] * dr[i][3]);
				ra[i] = Math.atan2(dr[i][2], dr[i][1]);
				dec[i] = Math.asin(dr[i][3] / d[i]);

				TAU = lmstRad - ra[i];
				alta[i] = Math.asin(sinLatitude * Math.sin(dec[i])
						+ cosLatitude * Math.cos(dec[i]) * Math.cos(TAU));
				aza[i] = Math.atan2 (-Math.sin(TAU)*Math.cos(dec[i]), 
						cosLatitude*Math.sin(dec[i]) - sinLatitude*Math.cos(dec[i])*Math.cos(TAU) );

			}
		}

		// 2 = venus, 10 = moon, 11 = sun
		System.out.print(JD);
		
		int[] planetIds = {2,10,11};
		
		double venusX = aza[2] * RAD2DEG * 11.6 + 32;
		double venusY = alta[2] * RAD2DEG * -11.6 + 326.14;
		
		double moonX = aza[10] * RAD2DEG * 11.6 + 32;
		double moonY = alta[10] * RAD2DEG * -11.6 + 326.14;
		
		double sunX = aza[11] * RAD2DEG * 11.6 - 200;
		double sunY = alta[11] * RAD2DEG * -11.6 + 326.14;
		
		
		out.clear();
		response.setContentType("text/plain");
		out.write ("<?xml version=\"1.0\"?>\n");
		
		out.write ("<svg version=\"1.0\" width=\"640px\" height=\"480px\" \n");
		out.write (" xmlns:svg=\"http://www.w3.org/2000/svg\"\n");
		out.write (" xmlns=\"http://www.w3.org/2000/svg\" >\n");
	
		out.write ("<!-- az=" 
				+ (aza[2] * RAD2DEG) 
				+ " alt=" + (alta[2] * RAD2DEG)
				+ "-->\n");
		
		out.write ("<g>\n");
		out.write ("<circle cx=\"" + venusX + "\" cy=\""
				+ venusY + "\" r=\"3\" />\n");
		out.write ("<circle cx=\"" + sunX + "\" cy=\""
				+ sunY + "\" r=\"6\" fill=\"red\" />\n");
		out.write ("<circle cx=\"" + moonX + "\" cy=\""
				+ moonY + "\" r=\"6\" fill=\"yellow\" />\n");
		out.write ("</g>\n");
		
		out.write ("</svg>\n");
%>