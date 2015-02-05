<%@page import="ie.wombat.rt.pothole.DataUploadHandler"%><%@include file="_header.jsp"%><%@page import="java.io.File"%>
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%><%@page import="org.jfree.io.IOUtils"%><%
File sensorLogFile = new File (dataDir, "pothole-" + System.currentTimeMillis()  + ".dat");
File metadataFile = new File (dataDir, "pothole-" + System.currentTimeMillis()  + ".inf");

System.err.println ("Creating log file " + sensorLogFile.getPath());
java.io.OutputStream fout = new java.io.FileOutputStream(sensorLogFile);
java.io.InputStream in = request.getInputStream();
IOUtils.getInstance().copyStreams(in,fout);
fout.close();

// Extract metadata
DataUploadHandler.createMetadataFile(sensorLogFile);
%>
