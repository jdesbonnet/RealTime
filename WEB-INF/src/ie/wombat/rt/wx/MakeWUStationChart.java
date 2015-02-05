package ie.wombat.rt.wx;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.Station;

import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import org.jfree.ui.RectangleInsets;

public class MakeWUStationChart {
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	
	public static JFreeChart createChart (int nHour, String what) {

		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		List stations = hsession.createQuery("from Station where stationType='WU'").list();
				
		TimeSeries[] seriesArray = new TimeSeries[stations.size()];
		HashMap seriesHash = new HashMap(stations.size());
		for (int i = 0; i < seriesArray.length; i++) {
			Station stn = (Station)stations.get(i);
			seriesArray[i] = new TimeSeries(stn.getStationId(), Minute.class);
			seriesHash.put (stn.getStationId(), seriesArray[i]);
		}
		

		String query = "from WUStationRecord where timestamp >= '"
				+ hqlf.format(new Date(
						System.currentTimeMillis() - (long)nHour * 3600000L))
				+ "' order by timestamp";

		List dbrecs = hsession.createQuery(query).list();
		
		for (int i = 1; i < dbrecs.size(); i++) {
			WUStationRecord r = (WUStationRecord) dbrecs.get(i);
			
			RegularTimePeriod ts = new Minute(r.getTimestamp());
			
			TimeSeries series = (TimeSeries)seriesHash.get(r.getStationId());
			if (series == null) {
				continue;
			}
			if ("wup".equals(what) && r.getAtmosphericPressure() != null) {
				
				float hpa = (float)( r.getAtmosphericPressure().floatValue() * 25.4 * 1.33322);
				if (hpa > 800 && hpa < 1100) {
					series.addOrUpdate(ts, new Float (hpa));
				}
			} else if ("wut".equals(what) && r.getTemperature() != null) {
				float tc = (float)((r.getTemperature().floatValue()-32.0)/1.8);
				if (tc > -30 && tc < 100) {
					series.addOrUpdate(ts, new Float (tc));
				}
			} else if ("wuws".equals(what) && r.getWindSpeed() != null) {
				// Wind speed in mph (!)
				float kmph = WXUtil.kn2kmph(r.getWindSpeed().floatValue());
				if (kmph > 0 && kmph < 250) {
					series.addOrUpdate(ts, new Float(kmph));
				}
			} else if ("wugs".equals(what) && r.getWindMaxGustSpeed() != null) {
				float kmph = WXUtil.kn2kmph(r.getWindMaxGustSpeed().floatValue()); 
				if (kmph > 0 && kmph < 250) {
					series.addOrUpdate(ts, new Float(kmph));
				}
			} else if ("wuwd".equals(what) && r.getWindDirection() != null) {
				float d = r.getWindDirection().floatValue();
				if (d < 0) {
					continue;
				}
				series.addOrUpdate(ts, r.getWindDirection());
			} else if ("wuprecip".equals(what) && r.getPrecipitationRate() != null) {
				float mmph = (float)(r.getPrecipitationRate().floatValue() * 25.4);
				series.addOrUpdate(ts, new Float(mmph));
			} else if ("wurh".equals(what) && r.getRelativeHumidity() != null) {
				if (r.getRelativeHumidity().floatValue() > 0 && r.getRelativeHumidity().floatValue() < 100) {
					series.addOrUpdate(ts, r.getRelativeHumidity());
				}
			}
			
			
		}

		tx.commit();
		HibernateUtil.closeSession();

		String xLabel;
		if (nHour <= 24) {
			Date now = Calendar.getInstance().getTime();
			Date yesterday = new Date(now.getTime() - (long)24 * 3600000L);
			xLabel = "Time (UTC) Date: " + dnyf.format(yesterday) + "/" + df.format(now);
		} else {
			xLabel = "Time & Date (UTC)";
		}
		
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		for (int i = 0; i < seriesArray.length; i++) {
			tsc.addSeries(seriesArray[i]);
		}
	
		String chartTitle="";
		String yLabel = "";
		if ("wup".equals(what)) {
			chartTitle="Atmospheric Pressure";
			yLabel = "Atmospheric Pressure (mb or hPa)";
		} else if ("wut".equals(what)) {
			chartTitle="Temperature";
			yLabel = "Temperature (C)";
		} else if ("wuws".equals(what)) {
			chartTitle="Wind Speed";
			yLabel = "Wind Speed (km/h)";
		} else if ("wugs".equals(what)) {
			chartTitle="Max Gust Speed";
			yLabel = "Max Gust Speed (km/h)";
		} else if ("wuwd".equals(what)) {
			chartTitle="Wind Direction";
			yLabel = "Wind Direction (deg)";
		} else if ("wuprecip".equals(what)) {
			chartTitle="Precipition";
			yLabel = "Preciption Rate (mm/h)";
		} else if ("wurh".equals(what)) {
			chartTitle="Relative Humidity";
			yLabel = "Relative Humidity %";
		}
		
		
		JFreeChart chart;
		
			chart = ChartFactory.createTimeSeriesChart(
				chartTitle, // title
				xLabel, // x-axis label
				yLabel, // y-axis label
				tsc, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);
				chart.setBackgroundPaint(Color.white);
		
		

		XYPlot plot = (XYPlot) chart.getPlot();
		
		
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

	
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
		}
		

		if ("wuprecip".equals(what)) {
			XYBarRenderer br = new XYBarRenderer();
			plot.setRenderer(br);
		} else {
			
		}

		// Date axis (horizontal)
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		if (nHour <= 24) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		} else if (nHour <= 48) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd-MMM"));
		} else {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd-MMM-yyyy"));
		}
		
		/*
		 * Create moving averages
		 */
		/*
		Iterator iter = seriesHash.values().iterator();
		while (iter.hasNext()) {
			TimeSeries ts = (TimeSeries)iter.next();
			TimeSeries mats = MovingAverage.createMovingAverage(
		            ts, "-MAVG", 24 * 60, 0
		        );
			tsc.addSeries(mats);
		}
		*/
		
		
		return chart;

	}
	
}