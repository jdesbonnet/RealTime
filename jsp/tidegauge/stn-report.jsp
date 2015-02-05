<%@page import="java.util.HashMap"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="ie.wombat.rt.wx.WUStationRecord"%>
<%@page import="ie.wombat.rt.Station"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.db.HibernateUtil"%>
<%@page import="ie.wombat.rt.datasource.ReadWUData"%>
<%
	Session hsession = HibernateUtil.currentSession();
	Transaction tx = hsession.beginTransaction();
	
	SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd");
	Date startDate = df.parse(request.getParameter("start_date"));
	Date now = Calendar.getInstance().getTime();
	
	long binSize = 3600000;
	int arraySize = (int) ((now.getTime() - startDate.getTime()) / binSize);
	
	out.println ("nRows=" + arraySize);
	
	HashMap stnHash = new HashMap();
	
	List recs = hsession.createQuery("from WUStationRecord").list();
	
	int index;
	
	Iterator iter = recs.iterator();
	while (iter.hasNext()) {
		WUStationRecord rec = (WUStationRecord)iter.next();
		index = (int)((rec.getTimestamp().getTime() - startDate.getTime()) / binSize);
		if (index < 0 || index >= arraySize) {
	continue;
		}
		
		float[] pa = (float[])stnHash.get(rec.getStationId());
		if (pa == null) {
	pa = new float[arraySize];
	stnHash.put(rec.getStationId(), pa);
		}
		 
		pa[index] = rec.getAtmosphericPressure().floatValue();
		
	}
	
	String[] stnIds = new String[stnHash.size()];
	stnHash.keySet().toArray(stnIds);
	
	response.setContentType("text/plain");
	
	out.print ("ROW ");
	for (int i = 0; i < stnIds.length; i++) {
		out.print (stnIds[i] + " ");
	}
	out.print ("\n");
	
	for (int i = 0; i < arraySize; i++) {
		out.print (i);
		out.print (" ");
		for (int j = 0; j < stnIds.length; j++) {
	float[] pa = (float[])stnHash.get(stnIds[j]);
	out.print (pa[i]);
	out.print (" ");
		}
		out.print ("\n");
	}
%>
