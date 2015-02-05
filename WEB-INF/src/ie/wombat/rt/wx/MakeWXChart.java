package ie.wombat.rt.wx;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.Station;

import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import org.jfree.ui.RectangleInsets;

/**
 * An attempt to make a generic chart class which works for all types
 * of statoins.
 * @author joe
 *
 */
public class MakeWXChart {
	
	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	
	public static JFreeChart createChart (int nHour, String what, String stnListStr) {


		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		List stnIdList = new ArrayList();
		HashMap stnIdHash = new HashMap();
	
		if (stnListStr == null || stnListStr.length() == 0) {
			List stations = hsession.createQuery("from Station").list();
			Iterator iter = stations.iterator();
			while (iter.hasNext()) {
				Station s = (Station)iter.next();
				stnIdList.add(s.getStationId());
				stnIdHash.put (s.getStationId(),"true");
			}
			
		} else {
			String[] ids = stnListStr.split(",");
			for (int i = 0; i < ids.length; i++) {
				String id = ids[i].trim();
				stnIdList.add(id);
				stnIdHash.put(id, "true");
			}
		}
		
		HashMap seriesHash = new HashMap(stnIdList.size());
		TimeSeries[] seriesArray = new TimeSeries[stnIdList.size()];
		
		for (int i = 0; i < stnIdList.size(); i++) {
			String stnId = (String)stnIdList.get(i);
			seriesArray[i] = new TimeSeries(stnId, Minute.class);
			seriesHash.put (stnId, seriesArray[i]);
		}
		

		String whereClause;
		
		if (stnIdList.size() == 1) {
			whereClause = "where timestamp >= '"
					+ hqlf.format(new Date(
							System.currentTimeMillis() - (long)nHour * 3600000L))
					+ "' and stationId='" + (String)stnIdList.get(0) 
					+ "' order by timestamp";
		} else {
			whereClause = "where timestamp >= '"
				+ hqlf.format(new Date(
						System.currentTimeMillis() - (long)nHour * 3600000L))
				+ "' order by timestamp";
		}
		
		List dbrecs = new ArrayList();
		
		dbrecs.addAll(hsession.createQuery("from WUStationRecord " + whereClause).list());
		dbrecs.addAll(hsession.createQuery("from BuoyRecord " + whereClause).list());
		dbrecs.addAll(hsession.createQuery("from NRAStationRecord " + whereClause).list());
		
		for (int i = 1; i < dbrecs.size(); i++) {

			WXRecord r = (WXRecord) dbrecs.get(i);
			
			Minute ts = new Minute(r.getTimestamp());
			
			TimeSeries series = (TimeSeries)seriesHash.get(r.getStationId());
			if (series == null) {
				continue;
			}
			if ("wup".equals(what) && r.getAtmosphericPressureMillibars() != null) {
				
				float hpa = r.getAtmosphericPressureMillibars().floatValue();
				if (hpa > 800 && hpa < 1100) {
					series.addOrUpdate(ts, new Float (hpa));
				}
			} else if ("wxat".equals(what) && r.getAirTemperatureCelsius() != null) {
				float tc = r.getAirTemperatureCelsius().floatValue();
				if (tc > -30 && tc < 100) {
					series.addOrUpdate(ts, new Float (tc));
				}
			} else if ("wxws".equals(what) && r.getWindSpeedKnots() != null) {
				// Wind speed in mph (!)
				float kmph = WXUtil.kn2kmph(r.getWindSpeedKnots().floatValue());
				if (kmph > 0 && kmph < 250) {
					series.addOrUpdate(ts, new Float(kmph));
				}
			} else if ("wxgs".equals(what) && r.getWindMaxGustSpeedKnots() != null) {
				float kmph = WXUtil.kn2kmph(r.getWindMaxGustSpeedKnots().floatValue()); 
				if (kmph > 0 && kmph < 250) {
					series.addOrUpdate(ts, new Float(kmph));
				}
			} else if ("wxwd".equals(what) && r.getWindDirection() != null) {
				float d = r.getWindDirection().floatValue();
				if (d < 0) {
					continue;
				}
				series.addOrUpdate(ts, r.getWindDirection());
			} else if ("wxrh".equals(what) && r.getRelativeHumidity() != null) {
				float rhpc = r.getRelativeHumidity().floatValue();
				if (rhpc > 0 && rhpc <= 100) {
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
		
		Iterator iter = seriesHash.values().iterator();
		while (iter.hasNext()) {
			TimeSeries ts = (TimeSeries)iter.next();
			TimeSeries mats = MovingAverage.createMovingAverage(
		            ts, "-MAVG", 24 * 60, 0
		        );
			tsc.addSeries(mats);
		}
		
		
		return chart;

	}
	
}