package ie.wombat.rt.wx;

import ie.wombat.rt.HibernateUtil;

import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class MakeNRAStationChart {

	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	
	private static Class regularTimePeriodClass = org.jfree.data.time.Week.class;
	
	public static JFreeChart createChart (int nHour, String what) {

		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		Map<String, TimeSeries> seriesHash = new HashMap();

		Calendar cal = Calendar.getInstance();
		Date toTime = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, -nHour);
		Date fromTime = cal.getTime();
		
		//Date fromTime = new Date(System.currentTimeMillis() - (long)nHour * 3600000L);
		//Date toTime = new Date(System.currentTimeMillis());
		
		String query = "from NRAStationRecord where "
			+ " timestamp >= ? and timestamp < ? order by timestamp";

		List<NRAStationRecord> dbrecs = hsession.createQuery(query)
			.setTimestamp(0, fromTime)
			.setTimestamp(1, toTime)
			.setCacheable(true)
			.list();
		
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		
		makeSeries(what, dbrecs, seriesHash, 0L, true);
		tsc.addSeries(seriesHash.get("mean"));
		
		
		// Last year mean
		cal = Calendar.getInstance();
		long offset = cal.getTimeInMillis();
		cal.add(Calendar.YEAR, -1);
		offset -= cal.getTimeInMillis();
		toTime = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, -nHour);
		fromTime = cal.getTime();
		
		query = "from NRAStationRecord where "
			+ " timestamp >= ? and timestamp < ? order by timestamp";

		dbrecs = hsession.createQuery(query)
			.setTimestamp(0, fromTime)
			.setTimestamp(1, toTime)
			.setCacheable(true)
			.list();
		
		makeSeries(what, dbrecs, seriesHash, offset, true);
		tsc.addSeries(seriesHash.get("mean"));
	
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
		
		
		
		/*
		for (TimeSeries ts : seriesHash.values()) {
			tsc.addSeries(ts);
		}
		*/
		
	
		String chartTitle="";
		String yLabel = "";
		if ("nraat".equals(what)) {
			chartTitle="Air Temperature";
			yLabel = "Temperature (C)";
		} else if ("nrart".equals(what)) {
            chartTitle="Road Temperature";
            yLabel = "Temperature (C)";
        } else if ("nraws".equals(what)) {
			chartTitle="Wind Speed";
			yLabel = "Wind Speed (km/h)";
		} else if ("wugs".equals(what)) {
			chartTitle="Max Gust Speed";
			yLabel = "Max Gust Speed (km/h)";
		} else if ("nrawd".equals(what)) {
			chartTitle="Wind Direction";
			yLabel = "Wind Direction (deg)";
		} else if ("wuprecip".equals(what)) {
			chartTitle="Precipition";
			yLabel = "Preciption Rate (mm/h)";
		} else if ("nrarh".equals(what)) {
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
		
		
		//JFreeChart chart = new JFreeChart(plot);
		//chart.setBackgroundPaint(Color.white);
		return chart;

	}
	
	private static void makeSeries (String what, 
			List<NRAStationRecord> dbrecs,
			Map seriesHash,
			long offset,
			boolean meanOnly) {
		
		Map<RegularTimePeriod, Float> meanSigma = new HashMap();
		Map<RegularTimePeriod, Integer> meanCount = new HashMap();
		
		TimeZone tz = TimeZone.getDefault();
		
		for (int i = 1; i < dbrecs.size(); i++) {
			NRAStationRecord r = (NRAStationRecord) dbrecs.get(i);
			Date t = new Date(r.getTimestamp().getTime() + offset);
			RegularTimePeriod ts = RegularTimePeriod.createInstance(regularTimePeriodClass,t,tz);
			//RegularTimePeriod ts = regularTimePeriodClass.newInstance();
			//ts.createInstance(arg0, arg1, arg2)
			
			
			Number n = null;
			if ("nraat".equals(what) && r.getAirTemperature() != null) {
				//series.addOrUpdate(ts, r.getAirTemperature());
				n = r.getAirTemperature();
			} else if ("nrart".equals(what) && r.getAirTemperature() != null) {
                //series.addOrUpdate(ts, r.getRoadTemperature());
				n = r.getSurfaceTemperatureCelsius();
            } else if ("nraws".equals(what) && r.getWindSpeed() != null) {
				//series.addOrUpdate(ts, r.getWindSpeed());
            	n = r.getWindSpeed();
			} else if ("nrawd".equals(what) && r.getWindDirection() != null) {
				float d = r.getWindDirection().floatValue();
				if (d < 0) {
					continue;
				}
				//series.addOrUpdate(ts, r.getWindDirection());
				n = r.getWindDirection();
			}  else if ("nrarh".equals(what) && r.getRelativeHumidity() != null) {
				//series.addOrUpdate(ts, r.getRelativeHumidity());
				n = r.getRelativeHumidity();
			}
			
			if (n == null) {
				continue;
			}
			
			if (! meanOnly) {
				TimeSeries series = (TimeSeries)seriesHash.get(r.getStationId());
				if (series == null) {
					//continue;
					series = new TimeSeries(r.getStationId(), regularTimePeriodClass);
					seriesHash.put (r.getStationId(), series);
				}
				
				series.addOrUpdate(ts,n);
			}
			
			// update sigma and count for mean calculation
			Float f = meanSigma.get(ts);
			if ( f == null) f = 0f;
			f +=  n.floatValue();
			meanSigma.put (ts, f);
				
			Integer j = meanCount.get(ts);
			if (j == null) j = 0;
			meanCount.put(ts,j+1);
		}
			
		
		TimeSeries meanTs = new TimeSeries("mean", regularTimePeriodClass);
		
		for (RegularTimePeriod ts : meanSigma.keySet()) {
			float f = meanSigma.get(ts).floatValue();
			int j = meanCount.get(ts).intValue();
			float mean = f/(float)j;
			meanTs.add(ts,new Float(mean));
		}
		
		seriesHash.put("mean", meanTs);
		
	}
	
}