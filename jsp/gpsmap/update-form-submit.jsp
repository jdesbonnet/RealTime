<%@page import="java.io.FileWriter"%>
<%@include file="_header.jsp"%><%
	updateTripFile(request);
	response.sendRedirect("update-form.jsp");
%>