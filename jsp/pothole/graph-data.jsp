<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.Arrays"%><%@include file="_header.jsp"%>
<%@page import="java.util.ArrayList"%><%!
public float magnitude (float[] v) {
	return (float)Math.sqrt((double)(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]));
}
public float toUnitVector (float[] v) {
	float m = magnitude(v);
	v[0] /= m;
	v[1] /= m;
	v[2] /= m;
	return m;
}
public void zeroVector (float[] v) {
	v[0] = 0;
	v[1] = 0;
	v[2] = 0;
}
%><%

// Display map and accelerometer graph

int chartWidth = 1400;
try {
	chartWidth = Integer.parseInt(request.getParameter("w"));
} catch (Exception e) {
	// ignore
}
%>
<%@page import="ie.wombat.rt.pothole.TimePosition"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="ie.wombat.rt.pothole.TimeAccelerationPosition"%><html>
<head></head>
<body>
<table>
<tr>
<td>
<div id='chart_div' style='width: <%=chartWidth/2%>px; height: 240px;'></div>
</td>
<td>
<div id='chart2_div' style='width: <%=chartWidth/2%>px; height: 240px;'></div>
</td>
</tr>
<tr>
<td>
<form>
<button type="button" onClick="advanceChart(-1)">&lt;</button>
<button type="button" onClick="advanceChart(1)">&gt;</button>
<span id="chartTimes">xxx</span>
</form>
</td></tr>

</table>

<div id='map_div' style='width: <%=chartWidth %>px; height: 520px;'></div>
<div id="info"></div>
<%
String id = request.getParameter("id");
File dataFile = new File (dataDir, id);
BufferedReader lnr = new BufferedReader(new FileReader(dataFile));


int binSize = 1000;
try {
	binSize = Integer.parseInt(request.getParameter("bs"));
} catch (Exception e) {
	// ignore
}

ArrayList<TimePosition> timepositions = new ArrayList<TimePosition>();

// Pass 1: get position vs time
String line;
ArrayList<TimeAccelerationPosition> tapRecords = new ArrayList<TimeAccelerationPosition>();
ArrayList<TimeAccelerationPosition> allTapRecords = new ArrayList<TimeAccelerationPosition>();

long t=0;
long t0 = 0;

while (  (line = lnr.readLine()) != null ) {
	
	if (line.length() == 0) {
		continue;
	}
	
	String[] p = line.split(" ");
	
	String recType = p[0];
	
	if (recType.equals("V")) {
		continue;
	}
	
	t = Long.parseLong(p[1]);
	
	if (t0 == 0) {
		t0 = t;
	}
	
	if ("L".equals(p[0])) {
		if (p.length < 4) {
			out.write("expecting 4 items or more in record: " + line + "<br>\n");
		}
		TimePosition tp = new TimePosition();
		tp.t = t;
		tp.latitude = Double.parseDouble(p[2]);
		tp.longitude = Double.parseDouble(p[3]);
		timepositions.add(tp);
		
		// Linear interpolation of lat/lon for each record
		if (tapRecords.size () > 1 && timepositions.size() > 2) {
			// tpm1 is tp[i-1]
			TimePosition tpm1 = timepositions.get(timepositions.size()-2);
			double dt = (double)(tp.t - tpm1.t);
			double rlat = (tp.latitude - tpm1.latitude)/dt;
			double rlon = (tp.longitude - tpm1.longitude)/dt;
			for (TimeAccelerationPosition tap : tapRecords) {
				tap.latitude = tpm1.latitude + (tap.t - tpm1.t) * rlat;
				tap.longitude = tpm1.longitude + (tap.t - tpm1.t) * rlon;
			}
		}
		
		allTapRecords.addAll(tapRecords);
		tapRecords.clear();
		continue;
	} 
	
	if ("G".equals(p[0])) {
		if (p.length < 5) {
			out.write("expecting 5 items or more in record: " + line + "<br>\n");
			continue;
		}
		TimeAccelerationPosition tap = new TimeAccelerationPosition();
		tap.t = t;
		tap.g = new float[3];
		tap.g[0] = Float.parseFloat (p[2]);
		tap.g[1] = Float.parseFloat (p[3]);
		tap.g[2] = Float.parseFloat (p[4]);
		tapRecords.add(tap);
	}
	
		
}
lnr.close();

%>
<script>
var d = [
<%

long bin0 = allTapRecords.get(0).t / binSize;

long bin,prevBin=0, prevt=0;
int i = 0;
int n=0;
double lat=0,lon=0;

i=0;

// Unit vector of current acceleration record
float[] d = new float[3];
float[] G = new float[3];
float[] Gu = new float[3];
float Gm;
float[] sigma_g = new float[3];

float[][] bin_g = new float[1000][3];

float e=0,ez=0;

float m;
float gz;
StringBuffer mapbuf = new StringBuffer();
StringBuffer pointloc = new StringBuffer();
lat = 0;
lon = 0;

double bin_lat=0;
double bin_lon=0;
//TimePosition tp = timepositions.get(tpIndex++);

for (TimeAccelerationPosition tap : allTapRecords) {
	
	bin = tap.t / binSize;
		
	bin_g[n][0] = tap.g[0];
	bin_g[n][1] = tap.g[1];
	bin_g[n][2] = tap.g[2];
		
	sigma_g[0] += tap.g[0];
	sigma_g[1] += tap.g[1];
	sigma_g[2] += tap.g[2];
	
	bin_lat += tap.latitude;
	bin_lon += tap.longitude;
		
	if (bin > prevBin) {
		if (n > 1) {
			
			// Center location of this bin?
			bin_lat /= (double)n;
			bin_lon /= (double)n;
			
			// What is the mean gravity vector for this bin?
			G[0] = sigma_g[0] / n;
			G[1] = sigma_g[1] / n;
			G[2] = sigma_g[2] / n;
				
			// Gravity unit vector
			Gu[0] = G[0];
			Gu[1] = G[1];
			Gu[2] = G[2];
			Gm = toUnitVector(Gu);
				
			// Measure 'vibration'
			e = 0; ez = 0;
			float gzMin=1000; float gzMax =-1000;
			for (i = 0; i < n; i++) {
				// Part of acceleration parallel to gravity vector
				gz = bin_g[i][0]*Gu[0] + bin_g[i][1]*Gu[1] + bin_g[i][2]*Gu[2];
				gzMin = gz < gzMin ? gz : gzMin;
				gzMax = gz > gzMax ? gz : gzMax;
				
				d[0] = bin_g[i][0] - G[0];
				d[1] = bin_g[i][1] - G[1];
				d[2] = bin_g[i][2] - G[2];
				m = magnitude(d);
					
				e += m*m;
				// Part of acceleration parallel to gravity vector
				gz = d[0]*G[0] + d[1]*G[1] + d[2]*G[2];
				//gz = bin_g[i][0]*Gu[0] + bin_g[i][1]*Gu[1] + bin_g[i][2]*Gu[2];
				ez += gz*gz;
			} // calc bin 'bumpiness'
			e = (float)Math.sqrt(e/n);
			ez = (float)Math.sqrt(ez/n);

			out.write ("[" 
				//+ "new Date(" + t + ")"
				//+ "," 
				+ e 
				+ "," + ez 
				+ "," + G[0]
				+ "," + G[1]
				+ "," + G[2]
				+ "," + Gm
				+ "," + (gzMax - gzMin)*5
			
	            + "],\n");
				
		}
		
		prevBin = bin;
		n = 0;
		zeroVector(sigma_g);
		bin_lat=0;
		bin_lon=0;
	}

	n++;
}
%>
];
var g_t0 = <%= t0 %>;
var g_binSize = <%= binSize %>;
var md = [ <%= mapbuf.toString() %> ];
var ptloc = [ 
<%
for (TimePosition tp : timepositions) {
	out.write ( "[" + tp.t + "," + tp.latitude + "," + tp.longitude + "],\n"); 
}
%> ];

</script>
<script type='text/javascript' src='http://www.google.com/jsapi'></script>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

<script type='text/javascript'>
//google.load('visualization', '1', {'packages':['annotatedtimeline']});
google.load("visualization", "1", {'packages':["linechart"]});
google.setOnLoadCallback(createChart);

var marker;
var map;

var g_page = 0;
var pageSize = 100;

var g_chart;
var g_data;

function drawChart () {
	g_data.removeRows(0,g_data.getNumberOfRows());
	var startIndex = g_page * pageSize;
	var endIndex = startIndex + pageSize*3;
	if (endIndex > d.length) {
		endIndex = d.length;
	}
	g_data.addRows(d.slice (startIndex,endIndex));
	g_chart.draw(g_data, {displayAnnotations: true, width: <%=chartWidth/2%>, height: 240, legend: 'bottom'});
	var ts = new Date(g_t0 + startIndex * g_binSize);
	var te = new Date(g_t0 + endIndex * g_binSize);
	document.getElementById("chartTimes").innerHTML = "From " + ts + " to " + te; 
}

function advanceChart (delta) {
	g_page += delta;
	if (g_page < 0) { 
		g_page = 0;
	}
	drawChart();
}
function createChart() {
	var myOptions = {
		zoom: 13, center: new google.maps.LatLng(53.3, -9),
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	map = new google.maps.Map(document.getElementById("map_div"), myOptions);
	marker = new google.maps.Marker({
		position: new google.maps.LatLng(ptloc[0][1], ptloc[0][2]), 
		map: map
	});   
	  
	var i,n;
	var route = []; var pt;
	for (i = 0, n = ptloc.length-1; i < n; i++) {
		pt = ptloc[i];
		route.push (new google.maps.LatLng(pt[1], pt[2]));
	}
	var routeLine = new google.maps.Polyline({
      path: route,
      strokeColor: "#FF0000",
      strokeOpacity: 1.0,
      strokeWeight: 2
    });
	routeLine.setMap(map);

	 
    g_data = new google.visualization.DataTable();
	g_data.addColumn('number','|vib|');
	g_data.addColumn('number','|vib.g|');
	g_data.addColumn('number','Gx');
	g_data.addColumn('number','Gy');
	g_data.addColumn('number','Gz');
	g_data.addColumn('number','Gm');
	g_data.addColumn('number','dgz');

	
	g_chart = new google.visualization.LineChart(document.getElementById('chart_div'));

	google.visualization.events.addListener(g_chart, 'onmouseover', 
		function (a) {
			document.getElementById("info").innerHTML = a.row + "," + a.column;
			marker.setPosition(new google.maps.LatLng(d[a.row][7], d[a.row][8]));
		}
	);
	google.visualization.events.addListener(g_chart, 'select', 
			function (a) {
				//alert(dispObj(a));
				document.getElementById("info").innerHTML = "***" + ptloc[g_chart.getSelection()[0].row];
				var pt = ptloc[g_chart.getSelection()[0].row];
				var imgUrl = "graph-data2.jsp?id=<%=id%>&t=" + pt[0];
				//alert(imgUrl);
				document.getElementById("chart2_div").innerHTML="<img src='"
					+ imgUrl + "'/>";
			}
		);

	drawChart();
	
}
function dispObj (o) {
	if (o == undefined) return "(null)";
	var d = "";
	for (var i in o) {
		d += i + "=" + o[i] + "; ";
	}
	return d;
}
</script>
</body>
</html>