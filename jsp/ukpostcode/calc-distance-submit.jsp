
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.File"%>
<%@page import="java.io.LineNumberReader"%><%

	// UK Post codes: http://en.wikipedia.org/wiki/UK_postcodes
	// also see http://www.pjenkins.co.uk/blog/index.php/2007/04/04/uk_post_code_distance_calculation/
	
	
	//
	// Load postcode database from CSV file
	//
	File csvFile = new File ("/var/tmp/uk-postcodes.csv");
	FileReader fr = new FileReader(csvFile);
	LineNumberReader lnr = new LineNumberReader(fr);
	
	Map<String,Long>xHash = new HashMap<String,Long>();
	Map<String,Long>yHash = new HashMap<String,Long>();
	Map<String,Float>latHash = new HashMap<String,Float>();
	Map<String,Float>lonHash = new HashMap<String,Float>();
	
	// Skip header
	lnr.readLine();
	
	String line;
	while (  ( line = lnr.readLine() ) != null) {
		String p[] = line.split(",");
		String code = p[0];
		Long x = new Long(p[1]);
		Long y = new Long(p[2]);
		Float lat = new Float(p[3]);
		Float lon = new Float(p[4]);
		
		xHash.put(code,x);
		yHash.put(code,y);
		latHash.put(code,lat);
		lonHash.put(code,lon);
	}
	
	fr.close();
	
	String code0 = request.getParameter("code0").toUpperCase().replaceAll("\\s+","");
	String code1 = request.getParameter("code1").toUpperCase().replaceAll("\\s+","");
	
	// Format is one of format is one of "A9 9AA", "A99 9AA", "AA9 9AA", 
	// "AA99 9AA", "A9A 9AA" or "AA9A 9AA",
	// Using term 'digits' to mean 0-9 and A-Z.
	// Only interested in the "out-code" (left part). If code is 4 digits
	// or less assume that we have just the out-code which can be 2,3 or 4 digits.
	// Else trim the last 3 digits from end to yield out-code.
	if (code0.length()>4) {
		code0 = code0.substring(0,code0.length()-3);
	}
	
	if (code1.length()>4) {
		code1 = code1.substring(0,code1.length()-3);
	}

		
	if ( ! xHash.containsKey(code0) ) {
		out.write ("Error: could not find " + code0);
		return;
	}
	
	if ( ! xHash.containsKey(code1) ) {
		out.write ("Error: could not find " + code1);
		return;
	}
	
	Long x0 = xHash.get(code0);
	Long y0 = yHash.get(code0);
	Long x1 = xHash.get(code1);
	Long y1 = yHash.get(code1);
	
	float lat0 = latHash.get(code0);
	float lon0 = lonHash.get(code0);
	float lat1 = latHash.get(code1);
	float lon1 = lonHash.get(code1);
	
	// SW and NE corners to autozoom map
	float swLat = lat0 < lat1 ? lat0 : lat1;
	float swLon = lon0 < lon1 ? lon0 : lon1;
	float neLat = lat0 < lat1 ? lat1 : lat0;
	float neLon = lon0 < lon1 ? lon1 : lon0;
	
	
	long dx = x1 - x0;
	long dy = y1 - y0;
	long d2 = dx*dx + dy*dy;
	
	// Distance in meters
	double d = Math.sqrt((double)d2);
	
	
	// km rounded to one decimal place
	double dkm = (double)((int)(d/100))/10;
	
	// Miles rounded to one decimal place
	double dmiles =  (double)((int)((d * 0.621371192)/100))/10;
	
	
%>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style>
#mapContainer {width:100%;height:400px;}
</style>
<script src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript">
function initialize() {
    var latlng = new google.maps.LatLng(<%=lat0%>, <%=lon0%>);
    var myOptions = {
      zoom: 8,
      center: latlng,
      scaleControl: true,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("mapContainer"), myOptions);
    var sw = new google.maps.LatLng(<%=swLat%>, <%=swLon%>);
    var ne = new google.maps.LatLng(<%=neLat%>, <%=neLon%>);
    var bounds = new google.maps.LatLngBounds(sw,ne);
    
    var marker0 = new google.maps.Marker({
        position: new google.maps.LatLng(<%=lat0%>,<%=lon0%>), 
        map: map, 
        title:"<%=code0%>"
    }); 
    
    var marker1 = new google.maps.Marker({
        position: new google.maps.LatLng(<%=lat1%>,<%=lon1%>), 
        map: map, 
        title:"<%=code1%>"
    });

    map.fitBounds (bounds);
}

</script>
</head>
<body onload="initialize()">


<table border="1">
<thead>
<tr>
<td>Out-Code</td>
<td>X-Coordinate</td>
<td>Y-Coordinate</td>
<td>Latitude (deg)</td>
<td>Longitude (deg)</td>
<td>LOS Distance</td>
</tr>
</thead>

<tbody>
<tr>
<td><%=code0%></td>
<td><%=x0%></td><td><%=y0%>
<td><%=latHash.get(code0)%></td><td><%=lonHash.get(code0)%></td>
<td rowspan="2"><%=dkm%> km (<%=dmiles%> miles)</td>
</tr>
<tr>
<td><%=code1%></td>
<td><%=x1%></td><td><%=y1%>
<td><%=latHash.get(code1)%></td><td><%=lonHash.get(code1)%></td>
</tr>

<tr>
<td colspan="6">
<div id="mapContainer"></div>
</td>
</tr>

</tbody>
</table>

</body>
</html>
