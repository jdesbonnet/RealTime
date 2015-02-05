<%@page import="ie.wombat.rt.datasource.ReadGauge"%>
<%
	ReadGauge r = new ReadGauge();
	r.updateGaugeDB();
	r.updateGaugeDB(new java.util.Date (System.currentTimeMillis() - 24*3600000));
%>
done.
