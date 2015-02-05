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
Calendar cal = Calendar.getInstance();
cal.set(Calendar.MILLISECOND,0);
cal.set(Calendar.SECOND,0);
cal.set(Calendar.MINUTE,0);
cal.set(Calendar.HOUR_OF_DAY,0);
long midnight = cal.getTime().getTime();
long now = System.currentTimeMillis();

Iterator iter = recs.iterator();
response.setContentType("text/plain");
out.clear();
while (iter.hasNext()) {
	TideGaugeRecord r = (TideGaugeRecord)iter.next();
	double t = (double)(r.getTimestamp().getTime() - midnight) / (3600000);
	out.print (t);
	out.print (" ");
	out.print (r.getWaterElevation());
	out.print (" ");
	out.print (r.getAtmosphericPressure());
	out.print ("\n");
}

tx.commit();
HibernateUtil.closeSession();
%>