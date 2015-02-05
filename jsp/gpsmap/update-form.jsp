<%@page import="java.util.Map"%>
<%@include file="_header.jsp" %>
<%

	
	Context context = new Context(request,response);

	// how many tuned in?
	Map<String, Long> ipHash = (Map<String,Long>)getServletContext().getAttribute("ipHash");
	if (ipHash != null) {
		context.put ("nListeners", ipHash.size());
	}

	out.clear();
	TemplateRegistry.getInstance().merge("/gpsmap/update-form.vm",context,out);
%>
