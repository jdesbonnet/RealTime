package ie.wombat.rt.tg;

import ie.wombat.rt.HibernateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Port offsets:
 * Ballina +45m; Bundoran +44m; Slign +49m; 
 * Donegal harbour + 44m; Limerick + 1h41m; 
 * Kilkee +24m; Blacksod Bay +30m; 
 * Westport (Inisraher) +21m; Enniscrone(?) + 45m;
 *  
 * @author joe
 *
 */

public class TideAnalysis {

	private static SimpleDateFormat hqlf = new SimpleDateFormat ("yyyyMMddHHmmss");
	
	public static void getLastHighTide () {

		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();
		
		String query = "from TideGaugeRecord where timestamp >= '" 
			+ hqlf.format(new Date(System.currentTimeMillis()-24*3600000)) 
			+ "' order by timestamp desc"
			;
			

		List dbrecs = hsession.createQuery(query).list();
		
		if (dbrecs.size() < 10) {
			return;
		}
		
		TideGaugeRecord[] ra = new TideGaugeRecord[dbrecs.size()];
		dbrecs.toArray(ra);
		
		int w0,w1,w2;
		for (int i = 1; i < dbrecs.size()-1; i++) {
			w0 = ra[i-1].getWaterElevation().intValue();
			w1 = ra[i].getWaterElevation().intValue();
			w2 = ra[i+1].getWaterElevation().intValue();
			if (w1 > w0 && w1 > w2) {
				System.err.println ("High tide at " + ra[i].getTimestamp() + " we=" + w1);
			}
		}
		
		tx.commit();
		HibernateUtil.closeSession();
	}
}
