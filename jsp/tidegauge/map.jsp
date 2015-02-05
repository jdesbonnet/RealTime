<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%
String uri = request.getScheme() + "://" + request.getServerName() + ":" 
	+ request.getServerPort() + request.getRequestURI();

SimpleDateFormat hf = new SimpleDateFormat ("dd MMM, HH00 zzz");
SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");
Calendar cal = Calendar.getInstance();
cal.set(Calendar.MILLISECOND,0);
cal.set(Calendar.SECOND,0);
cal.set(Calendar.MINUTE,0);

Date lastHour = cal.getTime();

//String serverAndPort = request.getScheme() + "://" + request.getServerName() + ":"
	//+ request.getServerPort();
//String fullContextURL = serverAndPort + request.getContextPath();

%>

<html>
<head>
<title>Weather Map of Ireland</title>

<style type="text/css">
#map {
width:500px;
height:660px;
}
</style>
<xscript src="http://maps.google.com/maps?file=api&amp;v=2&amp;"></xscript>
<script src="https://maps.googleapis.com/maps/api/js?libraries=geometry,visualization"></script>
<script src="ccmapjs.jsp"></script>
<script>
google.maps.event.addDomListener(window, 'load', loadGoogleMap);
</script>
</head>

<body>

<form style="border:0;margin:0;">
<select onChange="javascript:updateWeatherMap(this.value);">
<option value="at">Air Temperature (&deg;C)</option>
<option value="w">Wind Speed/Direction</option>
<option value="at,w">Air Temperature (&deg;C) + Wind</option>
<option value="st">Surface/Sea Temperature (&deg;C)</option>
<option value="ap">Atmospheric Pressure</option>
<option value="rh">Relative Humidity</option>
<option value="precip">Precipitation</option>
<option value="wxr">Precipitation (Radar)</option>
<option value="rc">Road conditions</option>
<option value="wave">Wave height/period</option>
<option value="we">Tide Gauge</option>
</select>

<!-- 
<select onChange="javascript:updateWeatherMap(this.value);">
<option value="now"><%=hf.format(lastHour)%> (most recent)</option>
<%
for (int i = 0; i < 12; i++) {
	cal.add(Calendar.HOUR, - 1);
	Date d = cal.getTime();
%>
	<option value="<%=hqlf.format(d)%>"><%=hf.format(d) %></option>
<%
}
%>
</select>
-->
</form>

<div id="map"></div>

</body>
</html>
