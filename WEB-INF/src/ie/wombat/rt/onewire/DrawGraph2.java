package ie.wombat.rt.onewire;

import ie.wombat.rt.chart.ChartUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Read file in tab delimited format (timestamp, sensor number, temperature C) and 
 * create a chart as image file.
 * 
 * @author joe
 *
 */
public class DrawGraph2 {

	private static Logger log = Logger.getLogger(DrawGraph2.class);

	
	public static void main (String[] arg) throws IOException {
		makeChart(arg[0]);
	}
	public static void makeChart (String dataFileName) throws IOException  {
		
		Map<String,TimeSeries> sensorTimeseriesMap = new HashMap<String,TimeSeries>();
		Map<String,String>sensorNumberToNameMap = new HashMap<String,String>();
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(dataFileName));
		String line;
		float temperatureC;
		long t;
		Date d = null;
		String sensorNumber;
		Date from = null;
		Date to = null;
		
		while ((line = lnr.readLine()) != null) {
			String[] p = line.split("\t");
			
			if (p.length < 1) {
				continue;
			}
			
			if ("Sensor".equals(p[0])) {
				sensorNumberToNameMap.put(p[1], p[2]);
				continue;
			}
			
			try {
				t = Long.parseLong(p[0]) * 1000;
				d = new Date( t );
				temperatureC = Float.parseFloat(p[2]);
				sensorNumber = p[1];
			} catch (Exception e) {
				continue;
			}
			
			if (from == null) {
				from = d;
			}
			
			String sensorName = sensorNumberToNameMap.get(sensorNumber);
			if (sensorName == null) {
				sensorName = "Sensor" + sensorNumber;
			}
			
			TimeSeries timeSeries = sensorTimeseriesMap.get(sensorName);
			
			if (timeSeries == null) {
				timeSeries = new TimeSeries(sensorName);
				sensorTimeseriesMap.put(sensorName, timeSeries);
			}
			
			// Ignore anomalies
			if (temperatureC > 40 ) {
				continue;
			}
			
			RegularTimePeriod ts = new Minute(d);
			
			timeSeries.addOrUpdate(ts, new Float(temperatureC));
		}
		
		to = d;
		
		// X Axis definition
		
		DateAxis timeAxis = new DateAxis("Time");
		timeAxis.setDateFormatOverride(ChartUtil.getDateAxisFormatter(from, to));
		
		
		// Y Axis definitions
		NumberAxis temperatureAxis = new NumberAxis("Temperature C");
		temperatureAxis.setAutoRange(true);
		temperatureAxis.setAutoRangeIncludesZero(false);
		
		
		
		// Temperature plot of all sensors
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		for (TimeSeries timeSeries : sensorTimeseriesMap.values()) {
			tsc.addSeries(timeSeries);
		}
		
		
		XYPlot temperaturePlot = new XYPlot(tsc, timeAxis,
				temperatureAxis, getPlotRenderer());
		
		JFreeChart chart = new JFreeChart("106 Tur Uisce Temperature Sensors",
				JFreeChart.DEFAULT_TITLE_FONT, temperaturePlot, 
				true // legend
				);
		

		KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
		encoder.setEncodingAlpha(true);
		encoder.encode(chart.createBufferedImage(1024, 600, BufferedImage.BITMASK, null) , new FileOutputStream ("/var/tmp/chart.png"));
		
	}

	
	private static XYItemRenderer getPlotRenderer () {
		
		XYItemRenderer renderer = new StandardXYItemRenderer();
		BasicStroke stroke = new BasicStroke(2.0f);
		renderer.setStroke(stroke);
		
		/*
		renderer.setSeriesPaint(0, new Color(255,0,0));
		renderer.setSeriesPaint(1, new Color(0,0,255));
		renderer.setSeriesPaint(2, new Color(0,255,0));
		renderer.setSeriesPaint(3, new Color(0,255,255));
		*/
		return renderer;
	}
	
}
