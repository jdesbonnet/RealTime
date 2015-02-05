<%@page import="ie.wombat.rt.datasource.ReadBuoyData"%>
<%
ReadBuoyData.pollServer("http://www.marine.ie/home/publicationsdata/data/buoys/Observations.htm");
%>
done.
