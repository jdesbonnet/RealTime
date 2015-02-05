<!DOCTYPE html> 

<%@page import="java.io.File"%>
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.HashMap"%><html> 
<head> 
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> 
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/> 
<title>Google Maps JavaScript API v3 Example: Complex Icons</title> 
<link href="http://code.google.com/apis/maps/documentation/javascript/examples/default.css" rel="stylesheet" type="text/css" /> 
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script> 
<script type="text/javascript"> 
function initialize() {

	var i;
	
	var mapOpts = {
			zoom: 13,
			center: new google.maps.LatLng(51.76, -9.6),
			mapTypeId: google.maps.MapTypeId.ROADMAP
	}
	var map = new google.maps.Map(document.getElementById("map_canvas"),
                                mapOpts);

	dataImages = {};
	
	var snrImage = [];
	for (var i = 0; i < 30; i++) {  
		snrImage[i] = new google.maps.MarkerImage('overlay-snr.jsp?t=' + i,
	      // This marker is 20 pixels wide by 32 pixels tall.
	      new google.maps.Size(16, 16),
	      // The origin for this image is 0,0.
	      new google.maps.Point(0,0),
	      // The anchor for this image is the base of the flagpole at 0,32.
	      new google.maps.Point(8, 8)
    	);
	}


	// override bad data
	//cells["11011"].lat=51.80273;
	//cells["11011"].lon=-9.566658;
	
 	// Draw locations of towers
  	i = 0;
	for (var cid in cells) {
		var c = cells[cid];

		// cast to int
		cid |= 0;
		
		var towerImage = new google.maps.MarkerImage('overlay-snr.jsp?shape=triangle&t=' + (cid+2)*2,
			      new google.maps.Size(16, 16),
			      new google.maps.Point(0,0),
			      new google.maps.Point(8, 8)
		    	);
		var marker = new google.maps.Marker({
			position: new google.maps.LatLng(c.lat, c.lon + Math.random()*0.005),
			map: map,
			icon: towerImage 
		});

		dataImages[cid] = new google.maps.MarkerImage('overlay-snr.jsp?shape=circle&t=' + (cid+2)*2,
			      new google.maps.Size(16, 16),
			      new google.maps.Point(0,0),
			      new google.maps.Point(8, 8)
		    	);
    	
		i += 4;
 	}
 	
    
  for (var i = 0, n = data.length; i < n; i++) {
	    var rec = data[i];
	    var ll = new google.maps.LatLng(rec[0], rec[1]);
	    var snr = rec[2];
	    var cellId = rec[3] | 0;

	    var marker = new google.maps.Marker({
	        position: ll,
	        map: map,
	        icon: dataImages[cellId]
	    });
	    
	}

 
  	
}
 
/**
 * Data for the markers consisting of a name, a LatLng and a zIndex for
 * the order in which these markers should display on top of each
 * other.
 */
var data = [
<%
	File file = new File("/var/tmp/export_100820161542.csv");
	LineNumberReader lnr = new LineNumberReader(new FileReader(file));
	
	String line;
	// data lat,lon
	double lat,lon;
	// cell tower lat,lon
	double clat, clon;
	int snr;
	int cellId;
	int cellNumber = 0;
	
	HashMap<Integer,String>cellLocations = new HashMap<Integer,String>();
	
	// Cell lat+lon key to index 0, 1, 2...
	HashMap<String,Integer>cellMarkers = new HashMap<String,Integer>();
	
	while ( (line = lnr.readLine()) != null) {
		String[] p = line.split(",");
		
		try {
			lat = Double.parseDouble(p[1]) / 1000000;
			lon = Double.parseDouble(p[2]) / 1000000;
			
			if (lat > 51.8) {
				continue;
			}
		
			snr = Integer.parseInt(p[3]);
			cellId = Integer.parseInt(p[8]);
			
			if (cellId == 65535) {
				continue;
			}
		
			// Bad data for cellid 11011
			if (cellId == 11011) {
				clat = 51.80273;
				clon = -9.566658;
			} else {
				clat = Double.parseDouble(p[9]) / 1000000;
				clon = Double.parseDouble(p[10]) / 1000000;
			}
			
			if (clat == 0 && clon == 0) {
				continue;
			}
			
			if (clon > -7) {
				continue;
			}
			
			// This is used as a key to identify individual cell towers
			// irrespect of the 'cellid'.
			String ll = "\"lat\":" + clat + ",\"lon\":" + clon;
		
			if ( ! cellMarkers.containsKey(ll) ) {
				cellMarkers.put (ll,cellNumber++);	
			}
		
			out.print ("[");
			out.print (lat);
			out.print (",");
			out.print (lon);
			out.print (",");
			out.print (snr);
			out.print (",");
			out.print (cellMarkers.get(ll));
			out.print ("],\n");
		} catch (Exception e) {
			// ignore
		}
	}
%>
];
var cells = {
<%
	for (String ll : cellMarkers.keySet()) {
		out.print ("\"");
		out.print (cellMarkers.get(ll));
		out.print ("\":{");
		out.print (ll);
		out.print ("},\n");
	}
%>
};
</script> 
</head> 
<body onload="initialize()"> 
  <div id="map_canvas"></div> 
</body> 
</html> 