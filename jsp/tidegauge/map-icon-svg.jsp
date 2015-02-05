<%!
private static final float MIN_T = -1;
private static final float MAX_T = 22;
//int[] rc = {0,     0,   0, 255, 255};
//int[] gc = {0,   255, 255, 255,   0};
//int[] bc = {255, 255,   0,   0,   0};

int[] rc = {0,     0,    255, 255};
int[] gc = {0,   255,  255,   0};
int[] bc = {255, 255,     0,   0};
//private static final float R = (MAX_T - MIN_T) / (float)NC;
%><%@ page contentType="image/svg+xml" %><%
//response.setContentType("image/svg+xml");

float R = (MAX_T - MIN_T) / (float)(rc.length-1);

float t = Float.parseFloat(request.getParameter("t"));

int index = (int) ((t - MIN_T) / R);
if (index < 0) {
	index = 0;
}
if (index >= rc.length-1 ) {
	index = rc.length-2;
}

float tr = (t - MIN_T) /R;
tr -= (float)(int)tr;

int r = rc[index] + (int)(tr * (float)(rc[index+1] - rc[index]));
int g = gc[index] + (int)(tr * (float)(gc[index+1] - gc[index]));
int b = bc[index] + (int)(tr * (float)(bc[index+1] - bc[index]));


String rgb = "rgb(" + r + "," 
		+ g + "," 
		+ b + ")";
%><?xml version="1.0"?>
<svg version="1.1"
     baseProfile="full"
     xmlns="http://www.w3.org/2000/svg"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:ev="http://www.w3.org/2001/xml-events"
     width="16" height="16" >
<g id="dummy">
<rect x="0" y="0" width="16" height="16" rx="5" ry="5" style="fill:<%=rgb%>;" />
<text x="2" y="12" style="font-size:10px; font-weight:bold;" fill="black"><%=(int)t%></text>
</g>
</svg>
