<%@include file="_header.jsp"%><%

// Select route on map. Draw graph of all datasets on that route.

%>

<html> 
<head> 
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> 
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/> 
<title>Google Maps JavaScript API v3 Example: Polyline Complex</title> 
<script src="http://maps.google.com/maps/api/js?sensor=false"></script> 
<script> 
var pathCoords;
var poly;
var map;

function initialize() {
	var mapCenterPoint = new google.maps.LatLng(53.285, -8.98);
	var myOptions = { zoom: 14, center: mapCenterPoint, mapTypeId: google.maps.MapTypeId.ROADMAP};
	map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
    
    // We create pathCoordinates as an MVCArray so we can
    // manipulate it using the insertAt() method
	pathCoords = new google.maps.MVCArray();
	var polyOpts = {path:pathCoords,strokeColor:'#000000',strokeOpacity:1.0,strokeWeight:3}
    poly = new google.maps.Polyline(polyOpts);
    poly.setMap(map);
    
	// Add a listener for the click event
	google.maps.event.addListener(map, 'click', addLatLng);
}
 
function addLatLng(event) {
	var path = poly.getPath();
	path.insertAt(path.length, event.latLng);
	//var marker = new google.maps.Marker({position: event.latLng,map: map});
	//marker.setTitle("#" + pathCoords.length);
	document.getElementById("route").value += event.latLng + "\n";
}
</script> 
</head> 
<body style="margin:0px; padding:0px;" onload="initialize()"> 
  <div id="map_canvas" style="width: 420px; height: 420px;"></div> 
  <form action="graph-route-submit.jsp" method="GET">
  <textarea id="route" name="route" rows="5" cols="40"></textarea>
  <input type="submit" value="Plot" />
  </form>
</body> 
</html> 