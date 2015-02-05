package ie.wombat.rt.tg;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.Station;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

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
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import org.jfree.ui.RectangleInsets;

public class MakeTideChart {

	private static final String[] gaugeIds = {
		"GALWAY___1",
		"SKERRIES_1",
		"KISH_____1", // 0001: bubbler, 0002 under water pressure
		"KISH_____2", // underwater pressure sensor
		"DODDER___1",
		"LIFFEY___1",
		"0000041118", // Castletownbear
		"DUBLINPORT",
		"HOWTH____1",
		"KBEGS____1"
	};
	
	private static final String DEFAULT_CHART_TITLE = "Galway Harbour Tide Gauge";
	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");


	public static JFreeChart createTideChart(int nHour, 
			String chartTitle,
			String stnListStr) {

		
		String[] stnIds;
		if (stnListStr == null) {
			stnIds = new String[1];
			stnIds[0] = "GALWAY___1";
		} else if ("all".equals(stnListStr)){
			stnIds = gaugeIds;
		} else {
			stnIds = stnListStr.split(",");
		}
		
			
		XYDataset[] datasets = createDatasets(stnIds, nHour);
		
		// XYDataset dataset = createDataset();
		
		if (chartTitle == null) {
			chartTitle = DEFAULT_CHART_TITLE;
		}

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
				"Water Elevation (m)", // y-axis label
				datasets[0], // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);

		chart.setBackgroundPaint(Color.white);
		
		/*
		if (includeDataCredit) {
			TextTitle dataCredit = new TextTitle ("Data courtesy Galway Harbour and the Marine Institute");
			dataCredit.setPosition(RectangleEdge.TOP);
			dataCredit.setHorizontalAlignment(HorizontalAlignment.CENTER);
			chart.addSubtitle(dataCredit);
		}
		*/
		
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		
		/*
		// Atmospheric pressure axis
		NumberAxis pAxis = new NumberAxis("Atmospheric Pressure (mb)");
		pAxis.setAutoRangeIncludesZero(false);
		plot.setRangeAxis(1, pAxis);
		plot.setDataset(1, datasets[1]);
		plot.mapDatasetToRangeAxis(1, 1);
		*/
		
		
		// Water elevation renderer
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
		}

		/*
		// Atmospheric pressure renderer
		XYLineAndShapeRenderer pRenderer = new XYLineAndShapeRenderer();
		pRenderer.setSeriesPaint(0, Color.blue);
		pRenderer.setBaseShapesVisible(true);
		pRenderer.setBaseShapesFilled(true);
		plot.setRenderer(1, pRenderer);
		*/
		
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
	
	public static JFreeChart createMeter (String gaugeId, Long recordId) {
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();
		TideGaugeRecord rec = (TideGaugeRecord)hsession.load(TideGaugeRecord.class, recordId);
		return createMeter (gaugeId, rec);
	}
	public static JFreeChart createMeter (String gaugeId, Date d) {
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		String query = "from TideGaugeRecord where gaugeId=? and timestamp >= ?"
				+ " order by timestamp";
		List<TideGaugeRecord> list = hsession.createQuery(query).setString(0,
				gaugeId).setTimestamp(1, d).setCacheable(true).setMaxResults(5)
				.list();
		if (list.size() > 0) {
			TideGaugeRecord rec = list.get(0);
			return createMeter(gaugeId, rec);
		} else {
			return createMeter (gaugeId, new TideGaugeRecord());
		}
	}
	public static JFreeChart createMeter(String gaugeId, TideGaugeRecord tgr) {

		float elevation = tgr.getWaterElevation().floatValue();
		DefaultValueDataset dataset = new DefaultValueDataset(elevation);
		
		Color transparent = new Color (0,0,0,0);
		
		MeterPlot plot = new MeterPlot(dataset);
		plot.setRange(new Range(-5, 5));
		
		/*
		plot.addInterval(new MeterInterval("'Spring' Tide Low Water", new Range(-8, -3),
				Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 0, 255, 64)));
		plot.addInterval(new MeterInterval("Normal Low Tide", new Range(-3, 0),
				Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 255, 0, 64)));
		plot.addInterval(new MeterInterval("Normal High Tide", new Range(0, 3),
				Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 255, 0, 64)));
		plot.addInterval(new MeterInterval("'Spring' Tide", new Range(3, 5),
				Color.lightGray, new BasicStroke(2.0f), new Color(255, 255, 0,
						64)));
		plot.addInterval(new MeterInterval("Flood Alert", new Range(5, 10),
				Color.lightGray, new BasicStroke(2.0f), new Color(255, 0, 0,
						128)));
		*/
		
		plot.addInterval(new MeterInterval("", new Range(-5, -2),
				Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 0, 255, 64)));
		plot.addInterval(new MeterInterval("", new Range(-2, 0),
				Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 255, 0, 64)));
		plot.addInterval(new MeterInterval("", new Range(0, 2),
				Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 255, 0, 64)));
		
		plot.addInterval(new MeterInterval("", new Range(2, 3),
				Color.lightGray, new BasicStroke(2.0f), new Color(255, 255, 0,
						64)));
		plot.addInterval(new MeterInterval("", new Range(3, 5),
				Color.lightGray, new BasicStroke(2.0f), new Color(255, 0, 0,
						128)));
		
		plot.setBackgroundAlpha(0);
		plot.setNeedlePaint(Color.darkGray);
		plot.setDialBackgroundPaint(Color.white);
		plot.setDialOutlinePaint(Color.gray);
		plot.setDialShape(DialShape.CHORD);
		plot.setMeterAngle(260);
		plot.setTickLabelsVisible(true);
		plot.setTickLabelFont(new Font("Dialog", Font.BOLD, 10));
		plot.setTickLabelPaint(Color.darkGray);
		plot.setTickSize(1.0);
		plot.setTickPaint(Color.lightGray);

		plot.setValuePaint(Color.black);
		plot.setValueFont(new Font("Dialog", Font.BOLD, 14));
		
		plot.setUnits("m");
		
		
		JFreeChart chart = new JFreeChart("Tide Gauge",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		
		/*
		JFreeChart chart = new JFreeChart(plot);
		*/
		chart.setBackgroundPaint(new Color(255,255,255,0));
		//chart.setBackgroundImageAlpha(0.5f);
		
		return chart;
	}

	


	public static JFreeChart createHistogramChart(int nHour, String chartTitle) {

		HistogramDataset dataset = new HistogramDataset();
		

		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		String query = "from TideGaugeRecord where gaugeId='GALWAY___1' and timestamp >= ?"
			+ " order by timestamp";
		

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1 * nHour);
		
		List dbrecs = hsession.createQuery(query).setDate(0,cal.getTime()).list();

		TideGaugeRecord[] ra = new TideGaugeRecord[dbrecs.size()];
		dbrecs.toArray(ra);

		double[] we = new double[ra.length];
		double[] ap = new double[ra.length];
		
		for (int i = 1; i < dbrecs.size() - 1; i++) {
			TideGaugeRecord r = (TideGaugeRecord) dbrecs.get(i);
			we[i] = r.getWaterElevation().doubleValue();
			ap[i] = r.getAtmosphericPressure().doubleValue();
		}
		
		//dataset.addSeries("WE", we, 1000,200,1000);
		dataset.addSeries("AP", ap, 1000,950,1050);

		tx.commit();
		HibernateUtil.closeSession();

		 JFreeChart chart = ChartFactory.createHistogram(
		            "Histogram Demo",
		            null,
		            null,
		            dataset,
		            PlotOrientation.VERTICAL,
		            true,
		            false,
		            false
		        );

		

		return chart;

	}


	public static XYDataset[] createDatasets(String[] stnIds, int nHours) {

		/*
		TimeSeries apSeries = new TimeSeries("Atmospheric Pressure (mb)",
				Second.class);
	
		*/
		
		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		
		HashMap tsHash = new HashMap(stnIds.length);
		//HashMap stnNameHash = new HashMap(stnIds.length);
		
		for (int i = 0; i < stnIds.length; i++) {
			
			String stnName;
			
			List list = hsession.createQuery("from Station where stationId=?").setString(0, stnIds[i]).list();
			
			if (list.size() == 1) {
				Station stn = (Station)list.get(0);
				stnName = stn.getName();
			} else {
				stnName = stnIds[i];
			}
			tsHash.put (stnIds[i], new TimeSeries(stnName));
		}
		
		
		String query = "from TideGaugeRecord where timestamp >= ?"
				+ " order by timestamp";
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1 * nHours);
		List dbrecs = hsession.createQuery(query).setDate(0,cal.getTime()).list();
		
		System.err.println ("" + dbrecs.size() + " records returned");
		
		long prevt = 0;
		
		
		for (int i = 1; i < dbrecs.size() - 1; i++) {
			TideGaugeRecord r = (TideGaugeRecord) dbrecs.get(i);
			
			TimeSeries weSeries = (TimeSeries)tsHash.get(r.getGaugeId());
			if (weSeries == null) {
				continue;
			}
			
			Second ts = new Second(r.getTimestamp());
			
			if ( (r.getTimestamp().getTime() - prevt) > 15*60*1000) {
				
				System.err.println ("break in data at ts=" 
						+ hqlf.format(r.getTimestamp()));
				
			}
			weSeries.addOrUpdate(ts, r.getWaterElevation());
			//apSeries.addOrUpdate(ts, r.getAtmosphericPressure());
			
			prevt = r.getTimestamp().getTime();
			
			
		}

		tx.commit();
		HibernateUtil.closeSession();

		/*
		TimeSeriesCollection[] datasets = new TimeSeriesCollection[2];
		datasets[WE_IDX] = new TimeSeriesCollection();
		datasets[AP_IDX] = new TimeSeriesCollection();
		
		datasets[WE_IDX] = new TimeSeriesCollection();
		datasets[WE_IDX].addSeries(weSeries);
		datasets[AP_IDX] = new TimeSeriesCollection();
		datasets[AP_IDX].addSeries(apSeries);
		*/
		//TimeSeriesCollection[] datasets = new TimeSeriesCollection[weSeriesHash.size()];
		TimeSeriesCollection[] datasets = new TimeSeriesCollection[2];
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		datasets[0] = tsc;
		
		datasets[1] = new TimeSeriesCollection();
		
		for (int i = 0; i < stnIds.length; i++) {	
			TimeSeries ts = (TimeSeries)tsHash.get(stnIds[i]);
			tsc.addSeries(ts);	
		}
		
		return datasets;

	}
}