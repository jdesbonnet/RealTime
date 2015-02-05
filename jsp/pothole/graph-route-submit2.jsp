<%@include file="_header.jsp"%>
<html> 
<head> 
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> 
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/> 
<title>Google Maps JavaScript API v3 Example: Polyline Complex</title> 
<script src="http://maps.google.com/maps/api/js?sensor=false"></script> 
<script> 

</script> 
</head> 
<body style="margin:0px; padding:0px;" onload="initialize()"> 
  <div id="map_canvas" style="width: 420px; height: 420px;"></div> 
<script>

<%
	// Select route on map. Draw graph of all datasets on that route.

	String[] p;

	// Point A
	String p0 = request.getParameter("p0");
	p = p0.substring(1, p0.length() - 2).split(",");
	double x0 = Double.parseDouble(p[1]);
	double y0 = Double.parseDouble(p[0]);

	// Point B
	String p1 = request.getParameter("p1");
	p = p1.substring(1, p1.length() - 2).split(",");
	double x1 = Double.parseDouble(p[1]);
	double y1 = Double.parseDouble(p[0]);
%>
var points = [
<%
	// Eqn of line a1 * x + b1 * y = c1
	double b1 = x1 - x0;
	double a1 = y1 - y0;
	
	double l = Math.sqrt(a1*a1 + b1*b1);
	
	// v is unit vector perpindicular to AB. Divide by |AB| to get unit vector.
	double vi = (y1 - y0) / l;
	double vj = - (x1 - x0) / l;

	
	double d, t;
	double xpp, ypp;
	int n = 0;
	File[] files = dataDir.listFiles();
	List<TimeAccelerationPosition> points = new ArrayList<TimeAccelerationPosition>();
	for (File f : files) {
		if (!f.getName().startsWith("trip-")) {
			continue;
		}
		System.err.println(f.getPath());

		try {
			List<TimeAccelerationPosition> tapRecords = getTimeAccelerationPositionRecords(f);
			for (TimeAccelerationPosition tap : tapRecords) {
				
				// d = perpindicular distance to line
				d = ((y0 - tap.latitude) * b1  -  (x0 - tap.longitude) * a1) / l;
					
				// Intersection point is found my moving along the perpindicular unit vector v by d units.
				xpp = tap.longitude - vi*d;
				ypp = tap.latitude - vj*d;
				        

				if (d < 0.0005 && d > -0.0005) {
					points.add(tap);
					if (n%500 == 0) {
							out.write("[" + ypp + "," + xpp + "],\n");
					}
					n++;
					/*
					if (n > 400) {
						break;
					}
					*/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
%>
];


var pathCoords;
var poly;
var map;

function initialize() {
	var mapCenterPoint = new google.maps.LatLng(53.285, -8.98);
	var myOptions = { zoom: 14, center: mapCenterPoint, mapTypeId: google.maps.MapTypeId.ROADMAP};
	map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
    
    // We create pathCoordinates as an MVCArray so we can
    // manipulate it using the insertAt() method
    /*
	pathCoords = new google.maps.MVCArray();
	var polyOpts = {path:pathCoords,strokeColor:'#000000',strokeOpacity:1.0,strokeWeight:3}
    poly = new google.maps.Polyline(polyOpts);
    poly.setMap(map);
	*/
	var linePoints = [
		new google.maps.LatLng(<%=y0%>, <%=x0%>),
		new google.maps.LatLng(<%=y1%>, <%=x1%>)
	];
	var line = new google.maps.Polyline({
		path: linePoints,
		strokeColor: "#FF0000",
		strokeOpacity: 1.0,
		strokeWeight: 2
	});
 	line.setMap(map);
	
    var i,n;
    n = points.length;
    if (n > 4000) {
        n = 4000;
    }
    for (i = 0; i < n; i++) {
        var pt = new google.maps.LatLng(points[i][0],points[i][1]);
		var marker = new google.maps.Marker({position: pt,map: map});
    }
    
    
}
 

</script>
</body> 
</html> 