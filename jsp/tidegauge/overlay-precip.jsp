<%@page 
contentType="image/png"
import="java.io.OutputStream"
import="ie.wombat.tidegauge.SVGToImage"
import="ie.wombat.template.Context"
import="ie.wombat.template.TemplateRegistry"
%><%OutputStream sout = response.getOutputStream();

int width = 32;
int height = 32;
try {
	width = Integer.parseInt(request.getParameter("w"));
} catch (Exception e) {
	// ignore
}
try {
	height = Integer.parseInt(request.getParameter("h"));
} catch (Exception e) {
	// ignore
}

Context context = new Context();

context.put ("width", new Integer(width));
context.put ("height", new Integer(height));

context.put ("ps", new Integer(request.getParameter("ps")));
context.put ("text", request.getParameter("text"));

String svg = TemplateRegistry.getInstance().mergeToString("/tidegauge/overlay-precip-svg.vm",context);

System.err.println (svg);

SVGToImage.renderSVG(svg,"image/png",sout);
//out.close();%>