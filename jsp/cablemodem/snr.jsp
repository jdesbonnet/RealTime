<%@page import="java.util.regex.Matcher"%><%@page import="java.util.regex.Pattern"%><%@page import="org.apache.commons.httpclient.methods.GetMethod"%><%@page import="org.apache.commons.httpclient.HttpClient"%><%

HttpClient httpClient = new HttpClient();
GetMethod get = new GetMethod ("http://192.168.100.1/system.asp");

int status = httpClient.executeMethod(get);

String responseBody = get.getResponseBodyAsString();

String[] bits = responseBody.split(" dBmV");

if (bits.length != 3) {
	out.write ("unexpected response");
}

//out.write ( "" + bits[0].length() + " " + bits[0].substring(1000));
Pattern p = Pattern.compile("([-\\d\\.]+)$");
Matcher matcher = p.matcher(bits[0]);
matcher.find();
float rxPower = Float.parseFloat(matcher.group(0));


matcher = p.matcher(bits[1]);
matcher.find();
float txPower = Float.parseFloat(matcher.group(0));

out.write ( ( System.currentTimeMillis() / 1000 ) + " " + rxPower + " " + txPower + "\n"); 
%>