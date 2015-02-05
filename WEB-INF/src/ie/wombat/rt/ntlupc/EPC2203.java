package ie.wombat.rt.ntlupc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class EPC2203 {

	public static float[] pollModem (String hostname) throws IOException {
		
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setParameter("http.socket.timeout", new Integer(1000));
		
		GetMethod get = new GetMethod ("http://" + hostname + "/system.asp");

		int status = httpClient.executeMethod(get);
		if (status != 200) {
			throw new IOException ("Unexpected HTTP status: " + status);
		}

		String responseBody = get.getResponseBodyAsString();
		
		int dBmVIndex = responseBody.indexOf(" dBmV",0);
		float rxPower = getFloatFromString(responseBody, dBmVIndex);
		
		dBmVIndex = responseBody.indexOf(" dBmV",dBmVIndex+1);
		float txPower = getFloatFromString(responseBody, dBmVIndex);
		
		float[] ret = new float[2];
		ret[0] = rxPower;
		ret[1] = txPower;
		
		return ret;
	}
	
	private static float getFloatFromString(String str, int endIndex) {
		char c;
		int i;
		for (i = -1 ; i > -8; i--) {
			c = str.charAt(endIndex+i);
			if (c != '.' && c != '-' && !Character.isDigit(c)) {
				break;
			}
		}
		return Float.parseFloat( str.substring(endIndex+i+1, endIndex));
	}
	
	public static void main (String[] arg) throws Exception {
		
		if (arg.length != 1) {
			System.err.println ("Parameters: modem-hostname-or-ip");
			return;
		}
		
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		Date now = new Date();
		
		float[] signal = pollModem(arg[0]);
		System.out.println (df.format(now) + "\t" + now.getTime() 
				+ "\t" + signal[0] + "\t" + signal[1]);
	}
}
