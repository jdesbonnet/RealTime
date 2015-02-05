<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%>
<%@include file="_header.jsp"%><%
	
	response.setContentType("application/json");
	//response.setContentType("text/plain");
	out.clear();
	out.write ("{ points: [");
	
	FileReader r = new FileReader(tripFile);
	LineNumberReader lnr = new LineNumberReader(r);
	
	String line;
	String lastT="0";
	while ( ( line = lnr.readLine() ) != null) {
		String[] p = line.split ("\\t");
		out.write ("{"
		+ "t:" + p[0]
		+ ", lat: " + p[1] 
		+ ", lon: " + p[2]	
		+ ( (p.length >= 4 && p[3].length()>0) ? ", comment:\"" + p[3] + "\"" : "")
		+ ", alt: " + p[4]
		+ ", s: " + p[5]

		+ "},\n");
		lastT = p[0];
	}
	
	out.write ("{} ], lastUpdate: " + lastT + " }");
	
	// Keep track of # people listening
	Map<String, Long> ipHash = (Map<String,Long>)getServletContext().getAttribute("ipHash");
	if (ipHash == null) {
		ipHash = new HashMap<String,Long>();
		getServletContext().setAttribute("ipHash",ipHash);
	}
	String addr = request.getRemoteAddr();
	Long now = new Long(System.currentTimeMillis());
	ipHash.put(addr,now);
	
	// Remove old sessions
	for (String ip : ipHash.keySet()) {
		if ( (now.longValue() - ipHash.get(ip).longValue())  > 1800000 ) {
			ipHash.remove(ip);
		}
	}
%>