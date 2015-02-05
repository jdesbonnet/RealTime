<%@page import="java.util.TimeZone"%>
<h2>Default TZ</h2>

<%=TimeZone.getDefault() %>


<h2>TZ List</h2>
<%
		String[] tzIds = java.util.TimeZone.getAvailableIDs();
		for (int i = 0; i < tzIds.length; i++) {
			out.println (tzIds[i] +"<br>");
		}
%>
