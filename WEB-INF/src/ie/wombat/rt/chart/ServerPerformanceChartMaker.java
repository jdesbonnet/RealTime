package ie.wombat.rt.chart;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class ServerPerformanceChartMaker implements ChartMaker {

	private static final int MAX_TOMCAT_MEM = 1000;
	private static final float MAX_LOAD_AVG = 2;

	private static Logger log = Logger
			.getLogger(ServerPerformanceChartMaker.class);

	//private static SimpleDateFormat dnyf = new SimpleDateFormat ("dd MMM");
	//private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");

	private static File[] dirs = { new File("/home/tomcat"),
			new File("/home/joe"), new File("/var/tmp") };

	public JFreeChart makeChart(Date from, Date to) {

		TimeSeriesCollection loadAverageCollection = new TimeSeriesCollection();
		TimeSeriesCollection eventCountCollection = new TimeSeriesCollection();
		TimeSeriesCollection memoryCollection = new TimeSeriesCollection();
		TimeSeriesCollection networkCollection = new TimeSeriesCollection();

		//networkCollection.addSeries(new TimeSeries("Test", Second.class));
		try {
			memoryCollection.addSeries(getTomcatMemTimeSeries(from, to));
			loadAverageCollection.addSeries(getLoadAverageTimeSeries(from, to));

			TimeSeries[] statts;

			statts = getStatTimeSeries(from, to, dirs, "tomcat");
			eventCountCollection.addSeries(statts[0]);
			eventCountCollection.addSeries(statts[1]);
			memoryCollection.addSeries(statts[3]);

			statts = getStatTimeSeries(from, to, dirs, "mysql");
			eventCountCollection.addSeries(statts[0]);
			eventCountCollection.addSeries(statts[1]);
			//memoryCollection.addSeries(statts[3]); //rss

			statts = getNetworkTimeSeries(from, to, dirs);
			networkCollection.addSeries(statts[0]);
			networkCollection.addSeries(statts[1]);

		} catch (Exception e) {
			log.error(e);
		}

		//int nHour = (int)(to.getTime() - from.getTime() / 3600000L);

		String xLabel = ChartUtil.makeTimeAxisLabel(from, to);

		
		NumberAxis loadAverageAxis = new NumberAxis("Load Average");
		XYPlot loadAveragePlot = new XYPlot(loadAverageCollection, null,
				loadAverageAxis, getPlotRenderer());
		//loadAveragePlot.setBackgroundPaint(Color.lightGray);
		//loadAveragePlot.setDomainGridlinePaint(Color.white);
		//loadAveragePlot.setRangeGridlinePaint(Color.white);

		NumberAxis memoryAxis = new NumberAxis("Memory (MB)");
		XYPlot memoryPlot = new XYPlot(memoryCollection, null, memoryAxis,
				getPlotRenderer());

		NumberAxis eventCountAxis = new NumberAxis("Events/s");
		XYPlot eventCountPlot = new XYPlot(eventCountCollection, null,
				eventCountAxis, getPlotRenderer());
		
		NumberAxis networkAxis = new NumberAxis("Network (KB/s)");
		XYPlot networkPlot = new XYPlot(networkCollection, null,
				networkAxis, getPlotRenderer());
		
		
		DateAxis axis = new DateAxis(xLabel);
		axis.setDateFormatOverride(ChartUtil.getDateAxisFormatter(from, to));
		CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(axis);
		cplot.setGap(10.0);
		cplot.setDomainGridlinePaint(Color.lightGray);
		cplot.setDomainGridlinesVisible(true);
		cplot.setOrientation(PlotOrientation.VERTICAL);
		cplot.add(loadAveragePlot);
		cplot.add(memoryPlot);
		cplot.add(eventCountPlot);
		cplot.add(networkPlot);
		
		

		JFreeChart chart = new JFreeChart("Server Performance",
				JFreeChart.DEFAULT_TITLE_FONT, cplot, false);

		/*
		chart.setBackgroundPaint(Color.white);
		TextTitle source = new TextTitle(
		        "Source: http://www.publicdebt.treas.gov/opd/opdhisms.htm",
		        new Font("Dialog", Font.PLAIN, 10));
		source.setPosition(RectangleEdge.BOTTOM);
		source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		chart.addSubtitle(source);
		 */

		LegendTitle legend = new LegendTitle(cplot);
		chart.addSubtitle(legend);

		// Free memory renderer
		/*
		XYLineAndShapeRenderer memRenderer = new XYLineAndShapeRenderer();
		memRenderer.setSeriesPaint(0, Color.blue);
		memRenderer.setBaseShapesVisible(true);
		memRenderer.setBaseShapesFilled(true);
		plot.setRenderer(1, memRenderer);
		 */

		// Date axis (horizontal)

		return chart;

	}

	private static XYItemRenderer getPlotRenderer () {
		XYItemRenderer renderer = new StandardXYItemRenderer();
		renderer.setSeriesPaint(0, new Color(255,0,0));
		renderer.setSeriesPaint(1, new Color(0,0,255));
		renderer.setSeriesPaint(2, new Color(0,255,0));
		renderer.setSeriesPaint(3, new Color(0,255,255));
		return renderer;
	}
	
	public static TimeSeries getTomcatMemTimeSeries(Date from, Date to)
			throws IOException {

		TimeSeries timeSeries = new TimeSeries("tomcat free mem",
				Second.class);

		/*
		 * Look for memory log in list of files specified in 'tomcatMemLog'
		 * property in WEB-INF/config.properties
		 */
		File[] tomcatMemLogFiles = { new File("/home/tomcat/tomcat-mem.log"),
				new File("/home/joe/tomcat-mem.log"),
				new File("/var/tmp/tomcat-mem.log") };

		File logFile = null;
		for (int i = 0; i < tomcatMemLogFiles.length; i++) {
			if (tomcatMemLogFiles[i].exists()) {
				logFile = tomcatMemLogFiles[i];
				break;
			}
		}

		if (logFile == null) {
			throw new IOException("mem log file not found");
		} else {
			System.err.println("Found log file at " + logFile.getPath());
		}

		LineNumberReader lnr = new LineNumberReader(new FileReader(logFile));
		String line;
		int m;
		while ((line = lnr.readLine()) != null) {
			String[] p = line.split(",");
			try {
				Date d = new Date(Long.parseLong(p[0]) * 1000);
				if (d.after(from) && d.before(to)) {
					Second ts = new Second(d);
					m = Integer.parseInt(p[1]);
					m /= 1024 * 1024;
					// Ignore datapoints above sanity threshold
					if (m <= MAX_TOMCAT_MEM) {
						timeSeries.addOrUpdate(ts, new Integer(m));
					}
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
		return timeSeries;
	}

	public static TimeSeries getLoadAverageTimeSeries(Date from, Date to)
			throws IOException {

		TimeSeries timeSeries = new TimeSeries("Load Avg", Second.class);

		File[] loadAvgLogFiles = { new File("/home/tomcat/loadavg.log"),
				new File("/home/joe/loadavg.log"),
				new File("/var/tmp/loadavg.log") };

		File logFile = null;
		for (int i = 0; i < loadAvgLogFiles.length; i++) {
			if (loadAvgLogFiles[i].exists()) {
				logFile = loadAvgLogFiles[i];
				break;
			}
		}

		if (logFile == null) {
			throw new IOException("mem log file not found");
		} else {
			System.err.println("Found log file at " + logFile.getPath());
		}

		LineNumberReader lnr = new LineNumberReader(new FileReader(logFile));
		String line;
		while ((line = lnr.readLine()) != null) {
			String[] p = line.split(" ");
			try {
				Date d = new Date(Long.parseLong(p[0]) * 1000);
				if (d.after(from) && d.before(to)) {
					Second ts = new Second(d);
					Float la = new Float(p[3]);
					if (la.floatValue() > MAX_LOAD_AVG) {
						la = new Float(MAX_LOAD_AVG);
					}
					timeSeries.addOrUpdate(ts, la);
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
		return timeSeries;
	}

	/**
	 * Read time series from periodic dump of /proc/(pid)/stat file
	 * 
	 * @param from
	 * @param to
	 * @param dirs
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static TimeSeries[] getStatTimeSeries(Date from, Date to,
			File[] dirs, String filename) throws IOException {

		TimeSeries majfltSeries = new TimeSeries(filename + " majflt",
				Second.class);
		TimeSeries utimeSeries = new TimeSeries(filename + " utime",
				Second.class);
		TimeSeries stimeSeries = new TimeSeries(filename + " stime",
				Second.class);
		TimeSeries rssSeries = new TimeSeries(filename + " rss", Second.class);

		TimeSeries[] ret = new TimeSeries[4];
		ret[0] = majfltSeries;
		ret[1] = utimeSeries;
		ret[2] = stimeSeries;
		ret[3] = rssSeries;

		File logFile = null;
		for (int i = 0; i < dirs.length; i++) {
			File f = new File(dirs[i], filename + "_stat.log");
			if (f.exists()) {
				logFile = f;
				break;
			}
		}

		if (logFile == null) {
			throw new IOException("log file not found");
		} else {
			System.err.println("Found log file at " + logFile.getPath());
		}

		LineNumberReader lnr = new LineNumberReader(new FileReader(logFile));
		String line;

		int prevMajflt = 0;
		int prevUtime = 0;
		int prevStime = 0;

		int majflt, utime, stime, rss;
		int dmajflt, dutime, dstime;

		long prevTime = 0;
		int dT = 0;

		while ((line = lnr.readLine()) != null) {
			String[] p = line.split(" ");
			Date d;
			try {
				d = new Date(Long.parseLong(p[0]) * 1000);
			} catch (Exception e) {
				continue;
			}

			if (d.before(from) || d.after(to)) {
				continue;
			}
			
			dT = (int) ((d.getTime() - prevTime) / 1000);

			if (dT < 600) {
				continue;
			}
			
			if (dT > 900) {
				prevTime = d.getTime();
				continue;
			}

			try {

				Second ts = new Second(d);
				majflt = Integer.parseInt(p[13]);
				dmajflt = majflt - prevMajflt;
				if (prevMajflt > 0 && dmajflt >= 0) {
					majfltSeries.addOrUpdate(ts, new Float((float)dmajflt/dT));
				}
				prevMajflt = majflt;

				utime = Integer.parseInt(p[15]);
				dutime = utime - prevUtime;
				if (prevUtime > 0 && dutime >= 0) {
					utimeSeries.addOrUpdate(ts, new Float((float)dutime/dT));
				}
				prevUtime = utime;

				stime = Integer.parseInt(p[16]);
				dstime = stime - prevStime;
				if (prevStime > 0 && dstime >= 0) {
					stimeSeries.addOrUpdate(ts, new Float((float)dstime/dT));
				}
				prevStime = stime;

				rss = Integer.parseInt(p[25]);
				rssSeries.addOrUpdate(ts, new Float((float) rss / 1024));

			} catch (Exception e) {
				log.error(e);
			}

			prevTime = d.getTime();
		}
		return ret;
	}

	/**
	 * Get timeseries from periodic dump from /proc/net/dev
	 * 
	 * @param from
	 * @param to
	 * @param dirs
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static TimeSeries[] getNetworkTimeSeries(Date from, Date to,
			File[] dirs) throws IOException {

		TimeSeries[] ret = new TimeSeries[2];
		TimeSeries txSeries = new TimeSeries("Tx", Second.class);
		TimeSeries rxSeries = new TimeSeries("Rx", Second.class);
		ret[0] = txSeries;
		ret[1] = rxSeries;

		File logFile = null;
		for (int i = 0; i < dirs.length; i++) {
			File f = new File(dirs[i], "network_stat.log");
			if (f.exists()) {
				logFile = f;
				break;
			}
		}

		if (logFile == null) {
			throw new IOException("log file not found");
		} else {
			System.err.println("Found log file at " + logFile.getPath());
		}

		LineNumberReader lnr = new LineNumberReader(new FileReader(logFile));
		String line;

		long prevTime = 0;
		long txBytes = 0;
		long rxBytes = 0;
		long dTx = 0;
		long dRx = 0;
		long prevTxBytes = 0;
		long prevRxBytes = 0;
		long dT = 0;
		
		while ((line = lnr.readLine()) != null) {
			String[] p = line.split("\\s+");
			Date d;
			try {
				d = new Date(Long.parseLong(p[0]) * 1000);
			} catch (Exception e) {
				continue;
			}

			if (d.before(from) || d.after(to)) {
				continue;
			}

			dT = d.getTime() - prevTime;
			
			if (dT < 300000) {
				continue;
			}
			
			if (dT > 700000) {
				prevTime = d.getTime();
				continue;
			}

			try {

				Second ts = new Second(d);
				// remove "eth0:"
				System.err.println ("d=" + d + "p[2]=" + p[2]);
				txBytes = Long.parseLong(p[2].substring(5));
				rxBytes = Long.parseLong(p[10]);

				dTx = txBytes - prevTxBytes;
				if (prevTxBytes > 0 && dTx > 0) {
					txSeries.addOrUpdate(ts, new Integer((int) ((dTx*1000)/(dT*1024))));
				}
				//txSeries.addOrUpdate(ts,new Integer(100));

				dRx = rxBytes - prevRxBytes;
				if (prevRxBytes > 0 && dRx > 0) {
					rxSeries.addOrUpdate(ts, new Integer((int) ((dRx*1000)/(dT*1024))));
				}
				//rxSeries.addOrUpdate(ts, new Integer(50));

				prevTxBytes = txBytes;
				prevRxBytes = rxBytes;

			} catch (Exception e) {
				log.error(e);
			}

			prevTime = d.getTime();
		}
		return ret;
	}
}