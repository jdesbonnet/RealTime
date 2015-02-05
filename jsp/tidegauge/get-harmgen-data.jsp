<%@page import="java.util.Calendar"%>
<%@page import="ie.wombat.rt.tg.TideGaugeRecord"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.db.HibernateUtil"%>
<%@page import="org.hibernate.Session"%>

<%
Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();

String query = "from TideGaugeRecord order by timestamp";
List recs = hsession.createQuery(query).list();


Iterator iter = recs.iterator();
response.setContentType("text/plain");
out.clear();
while (iter.hasNext()) {
	TideGaugeRecord r = (TideGaugeRecord)iter.next();
	out.print (r.getTimestamp().getTime() / 1000);
	out.print (" ");
	out.print (r.getWaterElevation().doubleValue()/100.0);
	out.print ("\n");
}

tx.commit();
HibernateUtil.closeSession();
%>