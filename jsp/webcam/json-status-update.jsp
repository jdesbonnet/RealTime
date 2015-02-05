<%@include file="_header.jsp"%><%
	
	response.setContentType("application/json");
	//response.setContentType("text/plain");
	out.clear();
	out.write ("([");
	for (String cameraId : cameraIds) {
		ImageStore store = imageStores.get(cameraId);
		long t = store.getLastImageTimestamp().getTime();
		long now = System.currentTimeMillis();
		out.write ("{id:\"" + cameraId + "\"");
		//out.write (", \"lastImageTs\": " +  store.getLastImageTimestamp().getTime());
		out.write (", age: " + (now-t));
		out.write (", status: " + (store.isAlive() ? "1":"0") );
		out.write ("},");
	}
	out.write ("{} ])");
%>