<%@page 
contentType="image/gif"
import="java.io.OutputStream"
import="ie.wombat.rt.SVGToImage"
import="ie.wombat.template.Context"
import="ie.wombat.template.TemplateRegistry"
import="ie.wombat.util.XmlUtil"
%><%OutputStream sout = response.getOutputStream();

if (TemplateRegistry.getInstance().isInitialized()) {
	TemplateRegistry.getInstance().init(getServletContext().getRealPath("/templates"));
}

int width = 96;
int height = 16;
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

String text = XmlUtil.makeSafe(request.getParameter("text"));
context.put ("text", text);

String svg = TemplateRegistry.getInstance().mergeToString("/tidegauge/overlay-text-svg.vm",context);

//System.err.println (svg);

SVGToImage.renderSVG(svg,"image/png",sout);
//out.close();%>