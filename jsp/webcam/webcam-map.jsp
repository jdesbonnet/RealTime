<%@include file="_header.jsp"%><%
	
out.clear();
TemplateRegistry.getInstance().merge("/webcam/webcam-map.vm",context,out);

%>