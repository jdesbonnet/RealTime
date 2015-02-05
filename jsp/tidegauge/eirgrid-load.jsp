<%@page import="java.io.ByteArrayOutputStream"%>


<%@page import="org.hibernate.Session"%>
<%@page import="ie.wombat.db.HibernateUtil"%>
<%@page import="org.hibernate.Transaction"%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.io.File"%>
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.Date"%>
<%@page import="ie.wombat.rt.eirgrid.WindGenerationRecord"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.TimeZone"%>
<%@page import="ie.wombat.rt.eirgrid.PowerRecord"%>
<ul>
<%
	Session hsession = HibernateUtil.currentSession();
	Transaction tx = hsession.beginTransaction();
	
	String table = request.getParameter("table");
	if (table == null) {
		throw new Exception ("no 'table' param: WindGeneration | SystemDemand");
	}
	
	String entity = table + "Record";
	String className = "ie.wombat.rt.eirgrid." + entity;
	
	List allRecords = hsession.createQuery("from " + entity).list();
	HashMap recHash = new HashMap(allRecords.size());
	Iterator iter = allRecords.iterator();
	
	while (iter.hasNext()) {
		PowerRecord r = (PowerRecord)iter.next();
		recHash.put (r.getTimestamp(),r);
	}

	SimpleDateFormat tsdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	tsdf.setTimeZone(TimeZone.getTimeZone("GB-Eire"));
	
	String filename = request.getParameter("filename");
	File csvFile = new File(filename);
	
	LineNumberReader lnr = new LineNumberReader (new FileReader(csvFile));
	
	String line;
	while ( (line = lnr.readLine()) != null) {
		try {
		String[] p = line.split(",");
		String tsstr = p[0] + " " + p[1];
		Date ts = tsdf.parse(tsstr);
		Float power = new Float(p[2]);
		out.println ("<li> ts=" + ts + " power=" + power );
		if (!recHash.containsKey(ts)) {
			//WindGenerationRecord r = new WindGenerationRecord();
			PowerRecord r = (PowerRecord)Class.forName(className).newInstance();
			r.setTimestamp(ts);
			r.setPower(power);
			hsession.save(r);
		} else {
			out.write ("ignoring record ... already exists");
		}
		} catch (Exception e) {
	// ignore
		}
		out.flush();
	}
	
tx.commit();
HibernateUtil.closeSession();
%>
done.
