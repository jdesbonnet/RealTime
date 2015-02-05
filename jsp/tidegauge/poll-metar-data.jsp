<%@page import="ie.wombat.rt.datasource.ReadMetarData"%>
<%@page import="ie.wombat.rt.Station"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Session"%>
<%
Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();

List<Station> stations = hsession.createQuery("from Station where stationType='METAR'").list();
for (Station stn : stations) {
	ReadMetarData.pollServer(hsession, stn.getStationId());
}
%>
done.
