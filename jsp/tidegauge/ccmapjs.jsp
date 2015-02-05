<%@page import="java.text.SimpleDateFormat"
contentType="text/javascript; charset=UTF-8"
%><%!
SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");
%><%
String serverAndPort = request.getScheme() + "://" + request.getServerName() + ":"
	+ request.getServerPort();
String fullContextURL = serverAndPort + request.getContextPath();

int windBarbWidth = 48;
int tIconWidth = 16;
int tgIconWidth = 48; int tgIconHeight = 16;
int apIconWidth = 32; int apIconHeight = 16;
int rhIconWidth = 32; int rhIconHeight = 16;
int textOverlayWidth = 96;
int textOverlayHeight = 16;
int precipOverlayWidth = 24;
int precipOverlayHeight = 24;

float mapCenterLat = 53.5f;
try {
        mapCenterLat = Float.parseFloat(request.getParameter("lat"));
} catch (Exception e) {
        // ignore
}

float mapCenterLon = -7.8f;
try {
        mapCenterLon = Float.parseFloat(request.getParameter("lon"));
} catch (Exception e) {
        // ignore
}

int mapZoom = 7;
try {
        mapZoom = Integer.parseInt(request.getParameter("zoom"));
} catch (Exception e) {
        // ignore
}



%>

/**
 * Current conditions GoogleMap customization map script.
 * (c) 2006,2007 Joe Desbonnet
 * 
 * 26 Oct 2006: Added radar layer
 **/
 
var windBarbWidth=<%=windBarbWidth%>;
var tIconWidth=<%=tIconWidth%>;
var tgIconWidth=<%=tgIconWidth %>; var tgIconHeight=<%=tgIconHeight %>;
var apIconWidth=<%=apIconWidth %>; var apIconHeight=<%=apIconHeight %>;
var rhIconWidth=<%=rhIconWidth %>; var rhIconHeight=<%=rhIconHeight %>;
var fullContextURL="<%=fullContextURL %>";
var serviceURL="<%=fullContextURL%>/jsp/tidegauge";
var textOverlayWidth=<%=textOverlayWidth %>;
var textOverlayHeight=<%=textOverlayHeight %>;
var precipOverlayWidth=<%=precipOverlayWidth %>;
var precipOverlayHeight=<%=precipOverlayHeight %>;

var mapCenterLat = <%=mapCenterLat%>;
var mapCenterLon = <%=mapCenterLon%>;
var mapZoom = <%=mapZoom%>;

var chartWidth = 320;
var chartHeight = 180;
var map;

var stns = new Array();
var radarEl;


// Cache icons once created
var atIcons = new Array();
var stIcons = new Array();
var wbIcons = new Array();
var precipIcons = new Array();


// These icons are global and defined in loadGoogleMap()
var roadMoistIcon;
var roadWetIcon;


/**
 * Convert a heading in degrees (0 = North, 90 = East etc) into
 * 8 points of compass (N, NE, E etc). Heading must be in range
 * 0 - 359.9
 */
 
function degreesToPointsOfCompass (d) {
	if (d >= 0 && d < 22.5) {
		return "N";
	} else if (d >= 22.5 && d < 67.5) {
		return "NE";
	} else if (d >= 67.5 && d < 112.5) {
		return "E";
	} else if (d >= 112.5 && d < 157.5) {
		return "SE";
	} else if (d >= 157.5 && d < 202.5) {
		return "S";
	} else if (d >= 202.5 && d < 247.5) {
		return "SW";
	} else if (d >= 247.5 && d < 292.5) {
		return "W";
	} else if (d >= 292.5 && d < 337.5) {
		return "NW";
	} else if (d >= 337.5 && d < 360) {
		return "N";
	} else {
		return "?";
	}
}

/**
 * Extract time from yyyyMMddHHmmss UTC timestamp 
 * and present as "HH:mm UTC"
 */
function formatTimestamp (ts) {
	if (ts == null || ts.length != 14) {
		return "?";
	}
	return  ts.substring(8,10) 
	+ ":" 
	+ ts.substring(10,12) 
	+ " UTC";
}

/** 
 * Create a heading for the info bubble using stnEl from XMl
 **/
function formatBubbleHeading (stnEl) {

	var stnId = stnEl.getAttribute("id");
	var stnName = stnEl.getAttribute("name");
	var ts = stnEl.getAttribute("ts");
	
	var html = "<b>" + stnId;
	if (stnName != null && stnName.length > 0) {
		html + " (" + stnName + ")";
	}
	html += "</b> @ ";
	html += formatTimestamp(ts);
	return html;
}

function chartxh (nhour,stnId,what) {
	var imgEl = document.getElementById("chart-" + escape(stnId));
	imgEl.src= serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
	+ "&what=" + what 
	+ "&nhour=" + nhour;
	var oldLinkEl = document.getElementById ("cl-24");
	oldLinkEl.style.color='blue';
	var oldLinkEl = document.getElementById ("cl-48");
	oldLinkEl.style.color='blue';
	var oldLinkEl = document.getElementById ("cl-96");
	oldLinkEl.style.color='blue';
	var oldLinkEl = document.getElementById ("cl-672");
	oldLinkEl.style.color='blue';
	
	var linkEl = document.getElementById ("cl-" + nhour);
	linkEl.style.color='black';
	linkEl.style.underline=false;
}

/**
 * Generate HTML for chart time span selector links. 
 * IN: 'cp' param list to be passed to chartxh() function
 **/
function timeSpanSelectors (cp) {
	var html;	
	html = "<div style='text-align:center;'";
	html += "<a id=\"cl-24\" href=\"javascript:chartxh(24," + cp +")\">24h</a> ";
	html += "&nbsp;";
	html += "<a id=\"cl-48\" href=\"javascript:chartxh(48," + cp +")\">48h</a> ";
	html += "&nbsp;";
	html += "<a id=\"cl-96\" href=\"javascript:chartxh(96," + cp +")\">4d</a> ";
	html += "&nbsp;";
	html += "<a id=\"cl-672\" href=\"javascript:chartxh(672," + cp +")\">4w</a> ";
	html += "</div>";
	return html;
}

function createTemperatureMarker(stnEl, point, stnId, stnName, ts, t, what) {
	var tf = parseFloat(t);
	var ti = tf.toFixed(0); // round to nearest int
	var markerIcon = new GIcon();
	//var markerImage = serviceURL + "/overlay-temp.jsp?t=" + ti + "&xx=.png";
	var markerImage = fullContextURL + "/gr/wx/t16x16/t" + ti + ".png";
	markerIcon.image = markerImage;
	markerIcon.iconSize = new GSize(tIconWidth, tIconWidth);
	markerIcon.iconAnchor = new GPoint(tIconWidth/2, tIconWidth/2);
	markerIcon.infoWindowAnchor = new GPoint(tIconWidth/2, tIconWidth/2);

	var html = new String();
	
	html += formatBubbleHeading(stnEl);
	
	html += "<br>";
	if (what == "at") {
		html += "Air Temperature: ";
	} else if (what == "st") {
		html += "Surface Temperature: ";
	}
	
	html += parseFloat(t).toFixed(1) + " &deg;C";
	
	
	
	html += "<br>";
	html += "<img ";
	html += "id=\"chart-" + escape(stnId) + "\" ";
	html += "alt=\"chart\" width=\"" 
	+ chartWidth 
	+ "\" height=\"" + chartHeight + "\" " 
	+ " src=\"" + serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
	+ "&what=" + what + "\">";
	
	// Chart timespan selectors
	
	html += "<br>";
	html += timeSpanSelectors("'" + stnId + "','" + what + "'");
	
	var marker = new GMarker(point, markerIcon);
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	return marker;
}

function createTideGaugeMarker(stnEl, point, stnId, stnName, ts, t, what) {
	var tf = parseFloat(t);
	var ti = tf.toFixed(2); // round to nearest int
	var markerIcon = new GIcon();
	var markerImage = serviceURL + "/overlay-tg.jsp?we=" + tf + "&xx=.png";
	markerIcon.image = markerImage;
	markerIcon.iconSize = new GSize(tgIconWidth, tgIconHeight);
	markerIcon.iconAnchor = new GPoint(tgIconWidth/2, tgIconHeight/2);
	markerIcon.infoWindowAnchor = new GPoint(tgIconWidth/2, tgIconHeight/2);

	var html = new String();
	
	html += formatBubbleHeading(stnEl);
	html += "<br>Water Elevation: ";
	html += parseFloat(t).toFixed(2) + "m";
	
	
	
	html += "<br>";
	html += "<img ";
	html += "id=\"chart-" + escape(stnId) + "\" ";
	html += "alt=\"chart\" width=\"" 
	+ chartWidth 
	+ "\" height=\"" + chartHeight + "\" " 
	+ " src=\"" + serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
	+ "&what=" + what + "\">";
	
		// Chart timespan selectors
	
	html += "<br>";
	html += timeSpanSelectors("'" + stnId + "','" + what + "'");
	
	var marker = new GMarker(point, markerIcon);
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	return marker;
}

function createAirPressureMarker(stnEl, point, stnId, stnName, ts, ap, what) {
	var api = parseInt(ap);
	var apf = parseFloat(ap);
	var markerIcon = new GIcon();
	var markerImage = serviceURL + "/overlay-ap.jsp?ap=" + api + "&xx=.png";
	markerIcon.image = markerImage;
	markerIcon.iconSize = new GSize(apIconWidth, apIconHeight);
	markerIcon.iconAnchor = new GPoint(apIconWidth/2, apIconHeight/2);
	markerIcon.infoWindowAnchor = new GPoint(apIconWidth/2, apIconHeight/2);

	var html = new String();
	
	html += formatBubbleHeading(stnEl);
	html += "<br>Atmospheric Pressure: ";
	html += apf.toFixed(1) + "hPa";
	
	
	html += "<br>";
	html += "<img ";
	html += "id=\"chart-" + escape(stnId) + "\" ";
	html += "alt=\"chart\" width=\"" 
	+ chartWidth 
	+ "\" height=\"" + chartHeight + "\" " 
	+ " src=\"" + serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
	+ "&what=" + what + "\">";
	
	// Chart timespan selectors
	
	html += "<br>";
	html += timeSpanSelectors("'" + stnId + "','" + what + "'");
	
	var marker = new GMarker(point, markerIcon);
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	return marker;
}

function createRHMarker(stnEl, point, stnId, stnName, ts, rh, what) {
	var rhi = parseInt(rh);
	var markerIcon = new GIcon();
	//var markerImage = serviceURL + "/overlay-rh.jsp?rh=" + rhi + "&xx=.png";
	var markerImage = fullContextURL + "/gr/wx/rh32x16/rh" + rhi + ".png";
	markerIcon.image = markerImage;
	markerIcon.iconSize = new GSize(rhIconWidth, rhIconHeight);
	markerIcon.iconAnchor = new GPoint(rhIconWidth/2, rhIconHeight/2);
	markerIcon.infoWindowAnchor = new GPoint(rhIconWidth/2, rhIconHeight/2);

	var html = new String();
	
	html += formatBubbleHeading(stnEl);
	html += "<br>Relative Humidity: ";
	html += rhi + "%";
	
	
	html += "<br>";
	html += "<img ";
	html += "id=\"chart-" + escape(stnId) + "\" ";
	html += "alt=\"chart\" width=\"" 
	+ chartWidth 
	+ "\" height=\"" + chartHeight + "\" " 
	+ " src=\"" + serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
	+ "&what=" + what + "\">";
	
	// Chart timespan selectors
	
	html += "<br>";
	html += timeSpanSelectors("'" + stnId + "','" + what + "'");
	
	var marker = new GMarker(point, markerIcon);
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	return marker;
}
   
function createWindBarb(stnEl, point, stnId, stnName, ws, wd) {
	
	var wsi = parseFloat(ws).toFixed(0);
	var wdi = parseFloat(wd).toFixed(0);
	
	var markerImage = serviceURL + "/overlay-windbarb.jsp?s=" + wsi 
	+ "&d=" + wdi + "&xx=.png";
	
	//var markerImage = fullContextURL + "/wxi/wind/" + windBarbWidth + "/" + windBarbWidth + "/" + ws + "/" + wd + "/icon.png";
	
	var markerIcon = new GIcon();
	markerIcon.image = markerImage;
	markerIcon.iconSize = new GSize(windBarbWidth, windBarbWidth);
	markerIcon.iconAnchor = new GPoint(windBarbWidth/2, windBarbWidth/2);
	markerIcon.infoWindowAnchor = new GPoint(windBarbWidth/2, windBarbWidth/2);

	var html = new String();
	html += formatBubbleHeading(stnEl);
	
	html += "<br>Wind speed: " + wsi + "km/h (";
	html += degreesToPointsOfCompass(wd) + ")<br>";
	
	html += "<img "
		+ "id=\"chart-" + escape(stnId) + "\" "
		+ "alt=\"chart\" width=\"" 
		+ chartWidth + "\" height=\"" + chartHeight + "\" " 
		+ " src=\"" + serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
		+ "&what=ws\">";
		
	// Chart timespan selectors
	var what="ws";
	
	html += "<br>";
	html += timeSpanSelectors("'" + stnId + "','" + what + "'");
	
	var marker = new GMarker(point, markerIcon);
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	return marker;
}

  
function createTextOverlay(stnEl, point, stnId, stnName, text) {

	var markerImage = serviceURL + "/overlay-text.jsp?w=" 
	+ textOverlayWidth 
	+ "&h=" + textOverlayHeight 
	+ "&text=" + escape(text)
	+ "&xx=.png";
	
	
	//var markerImage = fullContextURL + "/wxi/text/"+textOverlayWidth+"/"+textOverlayHeight+"/"+escape(text)+"/icon.png"; 	
	
	var markerIcon = new GIcon();
	markerIcon.image = markerImage;
	markerIcon.iconSize = new GSize(textOverlayWidth, textOverlayHeight);
	markerIcon.iconAnchor = new GPoint(0, 0);
	markerIcon.infoWindowAnchor = new GPoint(0, 0);

	var html = new String();
	html += formatBubbleHeading(stnEl);
	
	html += "<br>";
	
	
	html += text;
	
	var marker = new GMarker(point, markerIcon);
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	return marker;
}
 
function createRoadConditionOverlay(stnEl, point, stnId, stnName, rc) {

	var marker;
	if (rc == "Moist") {
		marker = new GMarker(point, roadMoistIcon);
	} else if (rc == "Wet") {
		marker = new GMarker(point, roadWetIcon);
	} else {
		// fall back to text overlay
		marker = createTextOverlay(stnEl, point, stnId, stnName, rc);
	}
	
	var html = new String();
	html += formatBubbleHeading(stnEl);
	html += "<br>";
	html += rc;

	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});

	return marker;	
}
	
			
function createPrecipOverlay(stnEl, point, stnId, stnName, ps) {
		
	// Icon index at http://www.econym.demon.co.uk/googlemaps/geicons.htm
	var rainIcon = new GIcon();
	rainIcon.image = "http://maps.google.com/mapfiles/kml/pal4/icon40.png";
	rainIcon.shadow = "http://maps.google.com/mapfiles/kml/pal4/icon40s.png";
	rainIcon.iconSize = new GSize(32, 32);
	rainIcon.shadowSize = new GSize (59,32);
	rainIcon.iconAnchor = new GPoint(16, 16);
	rainIcon.infoWindowAnchor = new GPoint(16, 16);
	var html = new String();
	
	html += formatBubbleHeading(stnEl);
	
	html += "<br>";
	
	var ps = stnEl.getAttribute("ps");
	if (ps == "0") {
		html += "No rain";
	} else if (ps == "1") {
		html += "Light rain";
	} else if (ps == "2") {
		html += "Medium rain";
	} else if (ps == "3") {
		html += "Heavy rain";
	}
	
	var marker;
	if (ps == "0") {
		marker = new GMarker(point, noRainIcon);
	} else {
		marker = new GMarker(point, rainIcon);
	}
	
	
	var what="pr";
	var html = new String();
	
	html += formatBubbleHeading(stnEl);
	
	html += "<br>Precipitation: " + ps + " mm/h";
	
	html += "<br>";
	html += "<img ";
	html += "id=\"chart-" + escape(stnId) + "\" ";
	html += "alt=\"chart\" width=\"" 
	+ chartWidth 
	+ "\" height=\"" + chartHeight + "\" " 
	+ " src=\"" + serviceURL + "/stn-chart.jsp?stnid=" + escape(stnId)
	+ "&what=" + what + "\">";
	
	// Chart timespan selectors
	
	html += "<br>";
	html += timeSpanSelectors("'" + stnId + "','" + what + "'");
	
	GEvent.addListener(marker, 'click', function() {
	marker.openInfoWindowHtml(html);
	});
	
	
	
	
	return marker;
}

    
function loadGoogleMap() {
	
	var mapOptions = {
		center: { lat: 53, lng: -8},
		zoom: 6
	};
	var map = new google.maps.Map(document.getElementById('map'), mapOptions);
            
	roadMoistIcon = new GIcon();
	roadMoistIcon.image = fullContextURL + "/gr/weather/moist32.png";
	roadMoistIcon.iconSize = new GSize(32, 32);
	roadMoistIcon.iconAnchor = new GPoint(16, 16);
	roadMoistIcon.infoWindowAnchor = new GPoint(16, 16);
	
	roadWetIcon = new GIcon();
	roadWetIcon.image = fullContextURL + "/gr/weather/wet32.png";
	roadWetIcon.iconSize = new GSize(32, 32);
	roadWetIcon.iconAnchor = new GPoint(16, 16);
	roadWetIcon.infoWindowAnchor = new GPoint(16, 16);
	
	heavyRainIcon = new GIcon();
	heavyRainIcon.image = fullContextURL + "/gr/weather/threedrop24.png";
	heavyRainIcon.iconSize = new GSize(24, 24);
	heavyRainIcon.iconAnchor = new GPoint(12, 12);
	heavyRainIcon.infoWindowAnchor = new GPoint(12, 12);
	
	mediumRainIcon = new GIcon();
	mediumRainIcon.image = fullContextURL + "/gr/weather/onedrop24.png";
	mediumRainIcon.iconSize = new GSize(24, 24);
	mediumRainIcon.iconAnchor = new GPoint(12, 12);
	mediumRainIcon.infoWindowAnchor = new GPoint(12, 12);
	
	noRainIcon = new GIcon();
	noRainIcon.image = fullContextURL + "/gr/wx/noprecip24.png";
	noRainIcon.iconSize = new GSize(24, 24);
	noRainIcon.iconAnchor = new GPoint(12, 12);
	noRainIcon.infoWindowAnchor = new GPoint(12, 12);
	
	/*
	lightRainIcon = new GIcon();
	lightRainIcon.image = fullContextURL + "/gr/weather/moist16.png";
	lightRainIcon.iconSize = new GSize(16, 16);
	lightRainIcon.iconAnchor = new GPoint(8, 8);
	lightRainIcon.infoWindowAnchor = new GPoint(8, 8);
	*/

	
	
	GDownloadUrl(serviceURL + "/stn-query.jsp", 
		function(data, responseCode) {
			var xml = GXml.parse(data);
			stns = xml.documentElement.getElementsByTagName("stn");
			var radarEls = xml.documentElement.getElementsByTagName("radar");
			if (radarEls.length > 0) {
				radarEl = radarEls[0];
			}
			updateWeatherMap("at");
		}
	);
		
}


/**
 * @param feature Can be one of 
 * 'w' (wind) 
 * 'at' (temperature)
 * 'st' (sea/surface temperature)
 */
function updateWeatherMap (flist) {
	
	// Parse feature list and set appropriate boolean vars
	
	var fa = flist.split (",");
	var hasWind = false;
	var hasAirTemp = false;
	var hasSurfaceTemp = false;
	var hasPrecip = false;
	var hasWxr = false;
	var hasWave = false;
	var hasAP = false;
	var hasRH = false;
	var hasRC = false;
	var hasWE = false;
	
	var i;
	for (i = 0; i < fa.length; i++) {
		if (fa[i] == "w") {
			hasWind = true;
		} else if (fa[i] == "at") {
			hasAirTemp = true;
		} else if (fa[i] == "st") {
			hasSurfaceTemp = true;
		} else if (fa[i] == "precip") {
			hasPrecip = true;
		} else if (fa[i] == "wave") {
			hasWave = true;
		} else if (fa[i] == "ap") {
			hasAP = true;
		} else if (fa[i] == "rh") {
			hasRH = true;
		} else if (fa[i] == "rc") {
			hasRC = true;
		} else if (fa[i] == "wxr") {
			hasWxr = true;
		} else if (fa[i] == "we") {
			hasWE = true;
		}
	}
		
	map.clearOverlays(); // wait until list minute to clear overlays
	for (var i = 0; i < stns.length; i++) {
    	var point = new GLatLng(parseFloat(stns[i].getAttribute("lat")),
                            parseFloat(stns[i].getAttribute("lon")));
       

        var gs = stns[i].getAttribute("gs");
        
        var st = stns[i].getAttribute("st");
		var ap = stns[i].getAttribute("ap");
        var rh = stns[i].getAttribute("rh");
        var rc = stns[i].getAttribute("rc");
        var we = stns[i].getAttribute("we");
       
        
        if (hasWind) {
        	if (wbIcons[i] != null) {
        		map.addOverlay(wbIcons[i]);
        	} else {
        		var ws = stns[i].getAttribute("ws");
				var wd = stns[i].getAttribute("wd");
        		if (ws != null && wd != null) {
					wbIcons[i] = createWindBarb(stns[i],
						point, 
						stns[i].getAttribute("id"), 
						stns[i].getAttribute("name"),
						ws, wd);
					map.addOverlay(wbIcons[i]);
				}
			}
		}
		
		if (hasAirTemp) {
			if (atIcons[i] != null) {
				map.addOverlay(atIcons[i]);
			} else {
				var at = stns[i].getAttribute("at");
				if (at != null) {
					atIcons[i] = createTemperatureMarker(
					stns[i],
					point, 
					stns[i].getAttribute("id"), 
					stns[i].getAttribute("name"),
					stns[i].getAttribute("ts"),
					at,
					"at"
					);
					map.addOverlay(atIcons[i]);
				}
			}
		}
		
		if (hasSurfaceTemp && st != null) {
			map.addOverlay (createTemperatureMarker(
			stns[i],
			point, 
			stns[i].getAttribute("id"), 
			stns[i].getAttribute("name"),
			stns[i].getAttribute("ts"),
			st,
			"st"
			));
		}
		
		// Atmospheric Pressure
		if (hasAP && ap != null) {
			/*
			map.addOverlay (createTextOverlay(
			stns[i],
			point, 
			stns[i].getAttribute("id"), 
			stns[i].getAttribute("name"),
			parseInt(ap,10) + "mb"
			));
			*/
			map.addOverlay (createAirPressureMarker(
					stns[i],
					point, 
					stns[i].getAttribute("id"), 
					stns[i].getAttribute("name"),
					stns[i].getAttribute("ts"),
					ap,
					"ap"
					));
		}
		
		// Relative Humidity
		if (hasRH && rh != null) {
			/*
			map.addOverlay (createTextOverlay(
			stns[i],
			point, 
			stns[i].getAttribute("id"), 
			stns[i].getAttribute("name"),
			parseInt(rh,10) + "%"
			));
			*/
			map.addOverlay (createRHMarker(
					stns[i],
					point, 
					stns[i].getAttribute("id"), 
					stns[i].getAttribute("name"),
					stns[i].getAttribute("ts"),
					rh,
					"rh"
					));
		}
		
		// Road conditions
		if (hasRC && rc != null) {
			map.addOverlay (createRoadConditionOverlay(
				stns[i],
				point, 
				stns[i].getAttribute("id"), 
				stns[i].getAttribute("name"),
				rc
			));
		}
		
		if (hasWave) {
			if (waveIcons[i] != null) {
				map.addOverlay(waveIcons[i]);
			} else {
				var wh = stns[i].getAttribute("wh");
        		var wp = stns[i].getAttribute("wp");
				if ( wh != null && wp != null) {
        			waveIcons[i] = createTextOverlay(
        			stns[i],
        			point, 
					stns[i].getAttribute("id"), 
					stns[i].getAttribute("name"),
					wh + "m / " + wp + "s"
					);
					map.addOverlay(waveIcons[i]);
				}
			}
		}
		
		if (hasPrecip) {
			if (precipIcons[i] != null) {
				map.addOverlay(precipIcons[i]);
			} else {
				var ps = stns[i].getAttribute("ps");
				if ( ps != null) {
        			precipIcons[i] = createPrecipOverlay(
        			stns[i],
        			point, 
					stns[i].getAttribute("id"), 
					stns[i].getAttribute("name"),
					ps);
					map.addOverlay(precipIcons[i]);
				}
			}
		}
		
		// Water Elevation (tide gauge)
		if (hasWE && we != null) {
		/*
			map.addOverlay (createTextOverlay(
			stns[i],
			point, 
			stns[i].getAttribute("id"), 
			stns[i].getAttribute("name"),
			we
			));
		*/
		 	map.addOverlay (createTideGaugeMarker(
					stns[i],
					point, 
					stns[i].getAttribute("id"), 
					stns[i].getAttribute("name"),
					stns[i].getAttribute("ts"),
					we,
					"we"
					));
		}
		
		
	}
	
	if (hasWxr) {
		var nrows = parseInt(radarEl.getAttribute ("rows"),10);
		var ncols = parseInt(radarEl.getAttribute ("cols"),10);
		var dlat = parseFloat(radarEl.getAttribute ("dlat"),10);
		var dlon = parseFloat(radarEl.getAttribute ("dlon"),10);
		var lat0 = parseFloat(radarEl.getAttribute ("lat0"),10);
		var lon0 = parseFloat(radarEl.getAttribute ("lon0"),10);
		var data = radarEl.getAttribute ("data");
		
		var i;
		var row=0;
		var col=0;
		var lat = lat0 * 1.0;
		var lon = lon0 * 1.0;
		
		for (i = 0; i < data.length; i++) {
			// s between 0 - 8
			s = parseInt (data.charAt(i),10);
			if (isNaN(s)) {
				continue;
			}
			// Ignore s=0,1; one drop symbol for s=2,3,4; 3 drop symbol for s=5,6,7,8
			switch (s) {
				case 0:
				case 1:
					map.addOverlay(new GMarker(new GLatLng(lat,lon),noRainIcon));
					break;
				case 2:
				case 3:
				case 4:
					// Add random jiggle -- looks better that way. This random element
					// also (on avarage) more accurately places the icon as (lat,lon)
					// is the top-left corner of the grid square
					var point  = new GLatLng(lat + dlat * Math.random(),
									lon - dlon * Math.random());
					map.addOverlay(new GMarker(point,mediumRainIcon));
					break;
				case 5:
				case 6:
				case 7:
				case 8:
					/*
					var point  = new GLatLng(lat + dlat * Math.random(),
									lon - dlon * Math.random());
					*/
					var point  = new GLatLng(lat,lon);
					map.addOverlay(new GMarker(point,heavyRainIcon));
					break;
			}
			
			/*
			if ( s > 1) {
				var point  = new GLatLng(lat,lon);
				if ( s >=2 && s <=4) {
					map.addOverlay(new GMarker(point,mediumRainIcon));
				} else {
					map.addOverlay(new GMarker(point,heavyRainIcon));
				}
			}
			*/
			col++;
			lon += dlon;
			if (col >= ncols-1) {
				lon = lon0;
				col=0;
				lat -= dlat;
				row++;
			} 
		}
	}
		
	
	
}

