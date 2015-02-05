<%@include file="_header.jsp"%><%
	
out.clear();
TemplateRegistry.getInstance().merge("/webcam/webcam-tn.vm",context,out);

%>