package ie.wombat.rt.eirgrid;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.chart.ChartMaker;

import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.util.List;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import org.jfree.ui.RectangleInsets;

public class EirgridChartMaker implements ChartMaker {


	private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");

	public static JFreeChart createChart (int nHour) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 6);
		Date to = cal.getTime();
		
		cal.add(Calendar.HOUR_OF_DAY, nHour * -1);
		
		Date from = cal.getTime();
		
		EirgridChartMaker c = new EirgridChartMaker();
		return c.makeChart (from, to);
		
	}

	public  JFreeChart makeChart(Date from, Date to) {

		System.err.println ("from=" + from);
		System.err.println ("to=" + to);
		
		XYDataset dataset = createDatasets(from,to);
		
		int nHour = (int)((to.getTime() - from.getTime()) / 3600000);
		
		

		String xLabel;
		if (nHour <= 24) {
			Date now = Calendar.getInstance().getTime();
			Date yesterday = new Date(now.getTime() - (long)24 * 3600000L);
			xLabel = "Time (UTC) Date: " + dnyf.format(yesterday) + "/" + df.format(now);
		} else {
			xLabel = "Time & Date (UTC)";
		}
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				null, // title
				xLabel, // x-axis label
				"Electrical Power (MW)", // y-axis label
				dataset, // data
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

		// Date axis (horizontal)
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		if (nHour <= 24) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		} else if (nHour <= 48) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd-MMM"));
		} else {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd-MMM-yyyy"));
		}
		return chart;

	}
	

	public static XYDataset createDatasets(Date from, Date to) {

		
		
		//String whereClause = "where timestamp >= ? and timestamp < ?";
		
		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

			
		
		String query;
		List dbrecs;
		
		TimeSeries wgSeries = new TimeSeries("Wind", Minute.class);
		query = "from WindGenerationRecord where timestamp >= ? and timestamp < ?" + " order by timestamp";
		dbrecs = hsession.createQuery(query).setTimestamp(0,from).setTimestamp(1,to).list();
		for (int i = 1; i < dbrecs.size() - 1; i++) {
			PowerRecord r = (PowerRecord) dbrecs.get(i);
			Minute ts = new Minute(r.getTimestamp());
			wgSeries.addOrUpdate(ts, r.getPower());
		}
		
		TimeSeries sdSeries = new TimeSeries("System Demand", Minute.class);
		query = "from SystemDemandRecord where timestamp >= ? and timestamp < ?" + " order by timestamp";
		dbrecs = hsession.createQuery(query).setTimestamp(0,from).setTimestamp(1,to).list();
		for (int i = 1; i < dbrecs.size() - 1; i++) {
			PowerRecord r = (PowerRecord) dbrecs.get(i);
			Minute ts = new Minute(r.getTimestamp());
			sdSeries.addOrUpdate(ts, r.getPower());
		}
		
		TimeSeries fdSeries = new TimeSeries("Forecast", Minute.class);
		query = "from ForecastDemandRecord where timestamp >= ? and timestamp < ?" + " order by timestamp";
		dbrecs = hsession.createQuery(query).setTimestamp(0,from).setTimestamp(1,to).list();
		for (int i = 1; i < dbrecs.size() - 1; i++) {
			PowerRecord r = (PowerRecord) dbrecs.get(i);
			Minute ts = new Minute(r.getTimestamp());
			fdSeries.addOrUpdate(ts, r.getPower());
		}
		

		tx.commit();
		HibernateUtil.closeSession();

		TimeSeriesCollection tsc = new TimeSeriesCollection();
		tsc.addSeries(wgSeries);
		tsc.addSeries(sdSeries);
		tsc.addSeries(fdSeries);
		return tsc;

	}
}