<%@include file="_header.jsp" %>
<%

	Context context = new Context(request,response);
	context.put ("YUI","http://yui.yahooapis.com/2.5.2/build");
	out.clear();
	TemplateRegistry.getInstance().merge("/gpsmap/fireeaglemap.vm",context,out);
%>
