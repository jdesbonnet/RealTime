<%@page import="org.apache.commons.httpclient.methods.GetMethod"%><%@page import="org.apache.commons.httpclient.HttpClient"%><%

HttpClient httpClient = new HttpClient();
GetMethod get = new GetMethod ("http://192.168.100.1/system.asp");

int status = httpClient.executeMethod(get);

String responseBody = get.getResponseBodyAsString();

String[] bits = responseBody.split(" dBmV");

if (bits.length != 3) {
	out.write ("unexpected response");
}


int dBmVIndex = responseBody.indexOf(" dBmV");
int i = 0;
while (Character.isDigit(responseBody.charAt(dBmVIndex+i))) {
		i--;
}
Float dBmV = new Float(responseBody.substring(dBmVIndex+i, dBmVIndex));

out.write ( ( System.currentTimeMillis() / 1000 ) +  dBmV + "\n"); 
%>