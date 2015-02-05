<%@page 
contentType="image/png"
import="java.io.OutputStream"
import="ie.wombat.rt.SVGToImage"
import="ie.wombat.template.Context"
import="ie.wombat.template.TemplateRegistry"
%><%!// was -1 -> 22
private static final float MIN_T = 0;
private static final float MAX_T = 20;
//private static String[] colors = {"red","orange","yellow","green","cyan","blue","violet"};
private static String[] colors = {"red","orange","yellow","green","cyan","blue","violet","#ff80cc","red"};

private int[] rc = {0,     0,    255, 255};
private int[] gc = {0,   255,  255,   0};
private int[] bc = {255, 255,     0,   0};%><%// reload icons periodically
response.setHeader ("refresh", "10");

OutputStream sout = response.getOutputStream();


//System.err.println ("c=" + request.getParameter("c"));

int nc = rc.length;
float R = (MAX_T - MIN_T) / (float)(nc-1);

float t = Float.parseFloat(request.getParameter("t"));


int r,g,b;

int index = (int) ((t - MIN_T) / R);
if (index < 0) {
	index = 0;
	r = rc[0]; g = gc[0]; b = bc[0];
} else if (index >= rc.length-1 ) {
	index = rc.length-2;
	r = rc[nc-1]; g = gc[nc-1]; b = bc[nc-1];
} else {

	float tr = (t - MIN_T) /R;
	tr -= (float)(int)tr;
	r = rc[index] + (int)(tr * (float)(rc[index+1] - rc[index]));
	g = gc[index] + (int)(tr * (float)(gc[index+1] - gc[index]));
	b = bc[index] + (int)(tr * (float)(bc[index+1] - bc[index]));
}


/*
String color = "rgb(" + r + "," 
		+ g + "," 
		+ b + ")";
*/
int c = Integer.parseInt(request.getParameter("t")) % colors.length;
String color = colors[c];

Context context = new Context();
context.put ("color",color);
context.put ("temp", request.getParameter("t"));

// Display temps <= 0C in white for emphasis
String textColor="black";
if (t <= 0) {
	textColor="white";
}
context.put ("textColor",textColor);
context.put ("shape",request.getParameter("shape"));

String svg = TemplateRegistry.getInstance().mergeToString("/celltowers/overlay-marker-svg.vm",context);

SVGToImage.renderSVG(svg,"image/png",sout);
//out.close();%>