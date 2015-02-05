<%@include file="_header.jsp"%><%@page 
import="org.w3c.dom.Document,org.apache.batik.dom.GenericDOMImplementation,org.w3c.dom.DOMImplementation"
import="java.awt.Rectangle"
import="org.apache.batik.svggen.SVGGraphics2D"
import="org.jfree.chart.ChartUtilities"%><%
	// Select route on map. Draw graph of all datasets on that route.


	String[] route = request.getParameter("route").split("\n");

	List<double[]> routePoints = new ArrayList<double[]>(route.length);
	for (String latLng : route) {
		String[] p = latLng.substring(1, latLng.length() - 2).split(",");
		double[] ll = new double[2];
		ll[0] = Double.parseDouble(p[0]);
		ll[1] = Double.parseDouble(p[1]);
		routePoints.add(ll);
	}
	
	
	JFreeChart chart = Charts.chartRoute (dataDir, routePoints);
	
	String outputFormat = "png";
	if ("svg".equals(request.getParameter("of"))) {
		outputFormat = "svg";
	}
	
	if ("png".equals(outputFormat)) {
		// PNG
		response.setContentType("image/png");
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 1024, 480);
	} else {
		// SVG
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document doc = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
		chart.draw(svgGenerator, new Rectangle (0,0,640,480));
		response.setContentType("image/svg+xml");
		svgGenerator.stream(out, true /* use css */);
	}
%>