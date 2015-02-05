/*
 *
 */

package ie.wombat.rt.fireeagle;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;


public class FireEagleConsumer extends HttpServlet {

	private static Logger log = Logger.getLogger(FireEagleConsumer.class);
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private static StringBuffer locLog = new StringBuffer();
	private static String lastLocTs;
	private static String prevPoint;
	
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    	
    	 OAuthConsumer consumer = null;
    	
    	 
         try {
        	 
             consumer = CookieConsumer.getConsumer("fireeagle",
                     getServletContext());
            
             OAuthAccessor accessor = CookieConsumer.getAccessor(request,
                     response, consumer);
            
             OAuthMessage result = CookieConsumer.CLIENT
                     .invoke(
                             accessor,
                             "https://fireeagle.yahooapis.com/api/0.1/user.xml",
                             null);
             String responseBody = result.getBodyAsString();
             
             response.setContentType("text/plain");
             PrintWriter out = response.getWriter();
             //out.print(responseBody);
             
             SAXReader reader = new SAXReader();
             Document document = reader.read(result.getBodyAsStream());
             
             Element rootEl = document.getRootElement();
             
             String locTs = rootEl.valueOf("//located-at");
             
             if (locTs.equals(lastLocTs)) {
            	 writeLocLog(out);
            	 return;
             }
             lastLocTs = locTs;
             
             String point = rootEl.valueOf("//georss:point");
             
             if (point.equals(prevPoint)) {
            	 writeLocLog(out);
            	 return;
             }
             prevPoint = point;
             
             String[] ll = point.split (" ");
             //Double lat = new Double(ll[0]);
             //Double lon = new Double(ll[1]);
             
             /*
             if (locTs.endsWith(":00")) {
            	 locTs = locTs.substring(0,locTs.length()-3) + "00";
             }
             Date ts = df.parse(locTs);
             */
             
             locLog.append ("{lat:"+ll[0]+",lon:"+ll[1]+"},");
             
             writeLocLog(out);
             
         } catch (Exception e) {
             CookieConsumer.handleException(e, request, response, consumer);
         }
    	 
    }

    private void writeLocLog (Writer out) throws IOException {
    	out.write ("{ points: [");
    	out.write (locLog.toString());
    	out.write ("]}");
    }
 
    private static final long serialVersionUID = 1L;

}
