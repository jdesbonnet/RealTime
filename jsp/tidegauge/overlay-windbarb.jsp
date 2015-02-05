<%@page import="ie.wombat.rt.SVGToImage"%>
<%@page import="ie.wombat.rt.wx.WXUtil"%>
<%@page 
contentType="image/png"
import="java.io.OutputStream"
import="ie.wombat.template.Context"
import="ie.wombat.template.TemplateRegistry"
%><%OutputStream sout = response.getOutputStream();

if (TemplateRegistry.getInstance().isInitialized()) {
	TemplateRegistry.getInstance().init(getServletContext().getRealPath("/templates"));
}

// speed (in kmph)
float s = Float.parseFloat(request.getParameter("s"));
// direction (in degrees)
float d = Float.parseFloat(request.getParameter("d"));

int width = 48;
try {
	width = Integer.parseInt(request.getParameter("w"));
} catch (Exception e) {
	// ignore
}
int height = width;


Context context = new Context();

context.put ("width", new Integer(width));
context.put ("height", new Integer(height));

context.put ("s",new Float(s));
context.put ("d",new Integer((int)d));

// Speed in knots (from km/h)
float sk = WXUtil.kmph2kn(s);

// Calculate number of barbs, half barbs and flags
int n = (int)( (sk+2.0)/5.0f);
int nFlag = n / 10;
n -= nFlag * 10;
int nBarb = n / 2;
n -= nBarb * 2;
int nHalfBarb = n ;

context.put ("speed", new Integer((int)sk));
context.put ("nFlag",new Integer(nFlag));
context.put ("nBarb",new Integer(nBarb));
context.put ("nHalfBarb", new Integer(nHalfBarb));

//System.err.println ("nFlag=" + nFlag + " nBarb=" + nBarb + " nHalfBarb=" + nHalfBarb);

String svg = TemplateRegistry.getInstance().mergeToString("/tidegauge/overlay-windbarb-svg.vm",context);

//System.err.println (svg);

SVGToImage.renderSVG(svg,"image/png",sout);
//out.close();%>