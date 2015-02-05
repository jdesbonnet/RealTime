package ie.wombat.rt.wx;

import ie.wombat.rt.HibernateUtil;

import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.plot.PolarPlot;

import org.jfree.chart.renderer.DefaultPolarItemRenderer;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MakeWindChart {

	private static Logger log = Logger.getLogger(MakeWindChart.class);

	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private static final float KNOTS_TO_KMPH = 1.852f;

	public static JFreeChart createWindChart(int nHour, String stationId, String chartTitle) {

		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		String query = "from BuoyRecord"
				+ " where stationId='" + stationId 
				+ "' and timestamp >= '"
				+ hqlf.format(new Date(System.currentTimeMillis()
						- (long) nHour * 3600000L))
				+ "' order by timestamp";

		List dbrecs = hsession.createQuery(query).list();

		XYSeries series = new XYSeries("Wind Speed (km/h)");
		XYSeries gustSeries = new XYSeries ("Max Gust Speed (km/h)");
		
		TimeSeries ws_series = new TimeSeries("Wind Speed (km/h)",
				org.jfree.data.time.Minute.class);
		TimeSeries gs_series = new TimeSeries("Max Gust Speed (km/h)",
				org.jfree.data.time.Minute.class);
		TimeSeries wd_series = new TimeSeries("Wind Direction",
				org.jfree.data.time.Minute.class);

		Iterator iter = dbrecs.iterator();
		while (iter.hasNext()) {
			BuoyRecord r = (BuoyRecord) iter.next();
			if (r.getWindSpeed() == null || r.getWindDirection() == null) {
				continue;
			}
			float speedKmph = r.getWindSpeed().floatValue() * KNOTS_TO_KMPH;
			float gustSpeedKmph = r.getWindMaxGustSpeed().floatValue() * KNOTS_TO_KMPH;
			float direction = r.getWindDirection().floatValue();
			log.debug("speed=" + speedKmph + " dir=" + direction);

			series.addOrUpdate(r.getWindDirection(), new Float(speedKmph));
			gustSeries.addOrUpdate(r.getWindDirection(), new Float(gustSpeedKmph));

			Minute m = new Minute(r.getTimestamp());
			ws_series.addOrUpdate(m, speedKmph);
			gs_series.addOrUpdate(m, gustSpeedKmph);
			wd_series.addOrUpdate(m, direction);
		}

		// polar chart
		//XYDataset data = new XYSeriesCollection(series);
		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(series);
		data.addSeries(gustSeries);
		
		JFreeChart chart = ChartFactory.createPolarChart(
				"Wind Trend (" + nHour + " hour)", data, true, false, false);

		PolarPlot plot = (PolarPlot) chart.getPlot();
		DefaultPolarItemRenderer r = new DefaultPolarItemRenderer();

		r.setSeriesFilled(0, true);
		r.setSeriesPaint(0, Color.blue);
		plot.setRenderer(r);

		tx.commit();
		HibernateUtil.closeSession();

		return chart;

	}

}
