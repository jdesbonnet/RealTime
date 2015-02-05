<%@page import="ie.wombat.rt.Station"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="ie.wombat.rt.wx.WUStationRecord"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.rt.datasource.ReadWUData"%>
<%
	Session hsession = HibernateUtil.currentSession();
	Transaction tx = hsession.beginTransaction();
	
	List stations = hsession.createQuery("from Station where stationType='WU'").list();
	Iterator iter;
	iter = stations.iterator();
	while (iter.hasNext()) {
		Station stn = (Station)iter.next();
		ReadWUData.pollServer(hsession, stn.getStationId());
	}
	
	/*
	tx.commit();
	tx = hsession.beginTransaction();
	
	List recs = hsession.createQuery("from WUStationRecord").list();
	HashMap dupHash = new HashMap(recs.size());
	iter = recs.iterator();
	int nDeleted = 0;
	
	while (iter.hasNext()) {
		WUStationRecord r = (WUStationRecord)iter.next();
		String key = r.getStationId() + r.getTimestamp();
		if (dupHash.containsKey(key)) {
	WUStationRecord fr = (WUStationRecord)dupHash.get(key);
	out.println ("Record #" + r.getId() 
	+ " stn=" + r.getStationId()
	+ " ts=" + r.getTimestamp()
	+ " duplicates record #"
	+ fr.getId()
	+ "<br>\n"
	);
	hsession.delete(r);
	nDeleted++;
		} else {
	dupHash.put (key,r);
		}
	}
	out.println ("<p>Deleted " + nDeleted + " duplicate records");
	*/
%>
done.
