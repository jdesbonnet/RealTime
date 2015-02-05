package ie.wombat.rt.chart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jfree.chart.axis.DateAxis;

public class ChartUtil {

	private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	
	public static String makeTimeAxisLabel (Date from, Date to) {
		
		int nHour = (int) ((to.getTime() - from.getTime()) / 3600000L);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		int fromYday = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(to);
		int toYday = cal.get(Calendar.DAY_OF_YEAR);
		
		if (nHour <= 24) {
			if  (fromYday == toYday) {
				return "Time (UTC) Date: " + dnyf.format(to);
			} else {
				return "Time (UTC) Date: " + dnyf.format(from) + "/" + df.format(to);
			}
		} else {
			return "Time & Date (UTC)";
		}
	}
	public static SimpleDateFormat getDateAxisFormatter (Date from, Date to) {
		int nHour = (int) ((to.getTime() - from.getTime()) / 3600000L);
		if (nHour <= 24) {
			return new SimpleDateFormat("HH:mm");
		} else if (nHour <= 48) {
			return new SimpleDateFormat("HH:mm dd-MMM");
		} else {
			return new SimpleDateFormat("HH:mm dd-MMM-yyyy");
		}
	}
}
