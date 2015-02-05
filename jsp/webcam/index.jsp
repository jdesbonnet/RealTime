<%@include file="_header.jsp"%><%
	
out.clear();
TemplateRegistry.getInstance().merge("/webcam/index.vm",context,out);

%>