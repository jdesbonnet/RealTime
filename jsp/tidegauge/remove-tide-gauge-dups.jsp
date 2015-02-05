<%@page import="java.util.ArrayList"%>
<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="java.util.List"%>
<%@page import="ie.wombat.rt.tg.TideGaugeRecord"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.text.SimpleDateFormat"%>
<%
SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");


out.println ("Querying...<br>");
out.flush();

Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();
List<TideGaugeRecord> list = hsession
	.createQuery("from TideGaugeRecord "
			+ " where timestamp > 20080601000000 and timestamp < 20080724000000 " 
			+ " order by id").list();


out.println ("Found " + list.size() + " records.<br>");
out.flush();

HashMap<String, TideGaugeRecord> tsHash = new HashMap(list.size());
List<TideGaugeRecord> dupList = new ArrayList<TideGaugeRecord>(list.size());


int ndup = 0;
for (TideGaugeRecord r : list) {
	String key = r.getGaugeId() +  df.format(r.getTimestamp());
	if (tsHash.containsKey(key)) {
		TideGaugeRecord dupr = (TideGaugeRecord)tsHash.get(key);
		/*
		out.println ("removing record id=" + r.getId() 
		+ " gauge=" + r.getGaugeId() + " ts=" + r.getTimestamp()
		+ " duplicates id=" + dupr.getId()
		+ " gauge=" + dupr.getGaugeId() 
		+ " ts=" + dupr.getTimestamp()
		+ "<br>");
		*/
		ndup++;
		dupList.add(r);
		continue;
	} 
	tsHash.put (key, r);
}
out.println ("Found " + ndup + " duplicates.<br>");
//tx.commit();

// Now delete:

int i = 0;
tx = hsession.beginTransaction();
for (TideGaugeRecord r : dupList) {
	hsession.delete(r);
	if (++i % 500 == 0) {
		//tx.commit();
		hsession.flush();
		out.println ("delete count " + i + " <br>");
		System.err.println ("delete count " + i);
		out.flush();
		//tx = hsession.beginTransaction();
	}
}


tx.commit();
HibernateUtil.closeSession();
%>

done.
