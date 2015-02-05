<%@page import="java.util.Calendar"%>
<%@page import="ie.wombat.astro.Sun"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.TimeZone"%>

<%
int blocksPerHour = 4;
double lat = 53.3;
double lon = -9;

if (request.getParameter("lat") != null) {
	try {
		lat = Double.parseDouble(request.getParameter("lat"));
	} catch (NumberFormatException e) {
		
	}
}

if (request.getParameter("lon") != null) {
	try {
		lon = Double.parseDouble(request.getParameter("lon"));
	} catch (NumberFormatException e) {
		
	}
}

%>
<html>
<head>

<style type="text/css">
TABLE.dayNight {border:0px; padding: 0px;}
TABLE.dayNight TD {border: 0px; padding: 0px;}
TABLE.dayNight TD.n {background: #00a; width:5px;} 
TABLE.dayNight TD.d {background: #ff0; width:5px;}
TABLE.dayNight TD.ne {background: #008; width:5px;} 
TABLE.dayNight TD.de {background: #fe0; width:5px;}
</style>
</head>

<body>


<table class="dayNight" cellpadding="0" cellspacing="1">
<thead>
<tr align="center">
<td></td>
<%
for (int i = 0; i < 24; i++) {
%>
<td colspan="<%=blocksPerHour%>"><%=i%></td>
<%
}
%>
</tr>

<%
SimpleDateFormat df = new SimpleDateFormat ("dd MMM");
TimeZone tz = TimeZone.getDefault();

Calendar cal = Calendar.getInstance();
cal.set(Calendar.YEAR,2007);
cal.set(Calendar.MONTH, 0);
cal.set(Calendar.DAY_OF_MONTH,1);

long tzOffset = 0;

for (int i = 0; i < 52; i++) {
	
	Date date = cal.getTime();
	
	double[] riseSet = Sun.calcRiseSet(date,tzOffset,lat,lon, Sun.SUNRISE);
	
	out.println ("<tr><td>" + df.format(date) + "</td>");
	
	
	// Sun never rises
	if (riseSet[Sun.RISE] == Sun.BELOW_HORIZON) {
		for (int j = 0; j < 24*blocksPerHour; j++) {
			if ( (j/blocksPerHour)%2 == 0) {
				out.print ("<td class=\"ne\"></td>");
			} else {
				out.print ("<td class=\"n\"></td>");
			}
		}
		out.print ("</tr>");
		cal.add(Calendar.HOUR,24*7);
		continue;
	}
	
	// Sun never sets
	if (riseSet[Sun.SET] == Sun.ABOVE_HORIZON ) {
		for (int j = 0; j < 24*blocksPerHour; j++) {
			if ( (j/blocksPerHour)%2 == 0) {
				out.print ("<td class=\"de\"></td>");
			} else {
				out.print ("<td class=\"d\"></td>");
			}
		}
		out.print ("</tr>");
		cal.add(Calendar.HOUR,24*7);
		continue;
	}
	
	
		
		
	if (tz.inDaylightTime(date)) {
		riseSet[Sun.RISE] += 1.0;
		riseSet[Sun.SET] += 1.0;
	}
	int r = (int)(riseSet[Sun.RISE] * blocksPerHour);
	int s = (int)(riseSet[Sun.SET] * blocksPerHour);
	
	for (int j = 0; j < 24*blocksPerHour; j++) {
		if ( (j/blocksPerHour)%2 == 0) {
			if ( j < r) {
				out.print ("<td class=\"ne\"></td>");
			} else if ( j >=r && j <= s) {
				out.print ("<td class=\"de\"></td>");
			} else {
				out.print ("<td class=\"ne\"></td>");
			}
		} else {
		if ( j < r) {
			out.print ("<td class=\"n\"></td>");
		} else if ( j >=r && j <= s) {
			out.print ("<td class=\"d\"></td>");
		} else {
			out.print ("<td class=\"n\"></td>");
		}
		}
	}
	
	out.println ("</tr>");
	
	cal.add(Calendar.HOUR,24*7);
}
%>
</thead>


</body>

</html>