
<%@page import="ie.wombat.template.TemplateRegistry"%>
<%@page import="ie.wombat.template.Context"%>
<%!
	// Field delimiter
	public static final String SEP = "\t";

	// Record delimiter
	public static final String EOR = "\n";
	
	public static File tripFile = new File ("/var/tmp/trip.dat");
	
	/*
	Context context = new Context(request,response);
	context.put ("YUI","http://yui.yahooapis.com/2.5.0/build");
	//context.put ("locations",locations);
	context.put ("cameraIds",cameraIds);
	context.put ("cameraMetadata",cameraMetadata);
	context.put ("imageStores",imageStores);
	context.put ("contextPath",request.getContextPath());
	context.put ("jsp",this);
	*/
	
	public static void updateTripFile (HttpServletRequest request) 
		throws IOException {
		// Open trip file in append mode
		FileWriter w = new FileWriter(tripFile, true);

		String comment = request.getParameter("comment")
			.replaceAll("\\t"," ")
			.replaceAll("[\\r\\n]"," ");
		
		w.write ("" + System.currentTimeMillis()
				+ SEP + request.getParameter("lat") 
				+ SEP + request.getParameter("lon")
				+ SEP + comment
				+ SEP + request.getParameter("alt")
				+ SEP + request.getParameter("s")
				+ EOR);
		w.close();
	}
%>