package ie.wombat.rt;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ie.wombat.rt.HibernateUtil;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;

public class RequestListener implements ServletRequestListener {

	private static Logger log = Logger.getLogger(RequestListener.class);
	
	private static SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd HH:mm:ss");
	
	public void requestDestroyed(ServletRequestEvent arg0) {
		try {
			/*
			HttpServletRequest request = (HttpServletRequest) arg0.getServletRequest();
			String contextPath = request.getContextPath();
			System.err.println("ending request "
					+ request.getRequestURI()
					+ " on context " + contextPath);
			*/
			
			HttpServletRequest request = (HttpServletRequest) arg0.getServletRequest();
			
			System.err.println ("request=" + request.getRequestURI() 
					+ " @ " + df.format(Calendar.getInstance().getTime())
					+ " freeMemory=" + (Runtime.getRuntime().freeMemory()/1024) 
					+ "KB / " + (Runtime.getRuntime().totalMemory()/1024) + "KB"
					);
			
			if (HibernateUtil.isSessionOpen()) {
				Session hsession = HibernateUtil.currentSession();
				
				logStats(hsession.getSessionFactory().getStatistics());
				
				Transaction tx = hsession.getTransaction();
				if (tx.isActive()) {
					//System.err.println("COMMITTING TX");
					tx.commit();
				}
				//System.err.println("CLOSING SESSION");
				HibernateUtil.closeSession();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void requestInitialized(ServletRequestEvent arg0) {
	
	}

	private static void logStats (Statistics stats) {

		stats.logSummary();
		
		double slcHitCount  = stats.getSecondLevelCacheHitCount();
		double slcMissCount = stats.getSecondLevelCacheMissCount();
		double slcHitRatio = slcHitCount / (slcHitCount + slcMissCount);
		log.info("SLC Hit Ratio: " + slcHitRatio);
		
		double queryCacheHitCount  = stats.getQueryCacheHitCount();
		double queryCacheMissCount = stats.getQueryCacheMissCount();
		double queryCacheHitRatio =
		  queryCacheHitCount / (queryCacheHitCount + queryCacheMissCount);

		log.info("Query Hit Ratio: " + queryCacheHitRatio);

	}
}
