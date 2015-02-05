
<%@page import="java.util.Arrays"%><%@include file="_header.jsp"%><%
	File files[] = dataDir.listFiles();
	Arrays.sort(files);
	for (File file : files) {
		String filename = file.getName();
		if (! filename.startsWith("pothole-") 
				&& ! filename.startsWith("trip-")
				&& ! filename.startsWith("kathump-") ) {
			continue;
		}
		if ( ! filename.endsWith (".dat")) {
			continue;
		}
		out.write ("<li> <a href=\"graph-data.jsp?id=" + file.getName() + "\">");
		out.write (file.getName());
		out.write ("</a> (" + (file.length()/1024) + " kB) ");
		out.write ("<a href=\"reprocess-upload.jsp?id=" + file.getName() + "\">P</a>");
		out.write ("</li>");
	}
%>
