<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.Arrays"%><%@include file="_header.jsp"%>
<%@page import="java.util.ArrayList"%><%!

%><%
%>
<%@page import="ie.wombat.rt.pothole.DataUploadHandler"%><html>
<head></head>
<body>
<%
String id = request.getParameter("id");
File dataFile = new File (dataDir, id);

DataUploadHandler.createMetadataFile(dataFile);

%>
done!
</body>
</html>

    
