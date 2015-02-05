<%@page import="ie.wombat.tidegauge.ReadAPRSWXData"%>
<%@page import="ie.wombat.rt.Station"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.rt.datasource.ReadNRAData"%>
<%
Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();

List stations = hsession.createQuery("from Station where stationType='APRSWX'").list();
Iterator iter = stations.iterator();
while (iter.hasNext()) {
	Station stn = (Station)iter.next();
	ReadAPRSWXData.pollServer(hsession, stn.getStationId());
}
%>
done.
