package ie.wombat.rt.onewire;

import ie.wombat.rt.chart.ChartUtil;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.Date;

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
public class DrawGraph {

	private static Logger log = Logger.getLogger(DrawGraph.class);

	private static final int NSENSOR = 21;
	private static final float MAX_T = 1000;
	private static final float MIN_T = -273;
	
	public static void main (String[] arg) throws IOException {
		makeChart(arg[0]);
	}
	public static void makeChart (String dataFileName) throws IOException  {
		
		TimeSeries[] timeseries = new TimeSeries[NSENSOR];
		TimeSeries[] deviationFromMeanTimeSeries = new TimeSeries[NSENSOR];
		XYSeries[] deviationFromMeanVsTemperatureSeries = new XYSeries[NSENSOR];
	
		for (int i = 0; i < NSENSOR; i++) {
			timeseries[i] = new TimeSeries("Sensor#"+i, Minute.class);
			deviationFromMeanTimeSeries[i] = new TimeSeries("Deviation Sensor#"+i, Minute.class);
			deviationFromMeanVsTemperatureSeries[i] = new XYSeries("Sensor#"+i);
		}
		
		TimeSeries minTempTimeSeries = new TimeSeries("Min");
		TimeSeries maxTempTimeSeries = new TimeSeries("Max");
		TimeSeries meanTempTimeSeries = new TimeSeries("Mean");
		TimeSeries rangeTimeSeries = new TimeSeries("Range");
		
		Calendar cal = Calendar.getInstance();
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(dataFileName));
		String line;
		int i,j;
		int sensorNumber;
		int minute, currentMinute=0;
		float dT;
		float meanTemperature;
		float minTemperature=MAX_T, maxTemperature=MIN_T;
		float temperatureC;
		float[] sensorTemperatureC = new float[NSENSOR];
		long t;
		Date d;
		
		while ((line = lnr.readLine()) != null) {
			String[] p = line.split("\t");
			
			try {
				t = Long.parseLong(p[0]) * 1000;
				d = new Date( t );
				sensorNumber = Integer.parseInt(p[1]);
				temperatureC = Float.parseFloat(p[2]);
			} catch (Exception e) {
				continue;
			}

			sensorTemperatureC[sensorNumber] = temperatureC;
			
			RegularTimePeriod ts = new Minute(d);
			
			minute = (int)(t / 60000);
			if (minute != currentMinute) {
				if (minTemperature != MAX_T && maxTemperature != MIN_T) {
					minTempTimeSeries.addOrUpdate(ts, minTemperature);
					maxTempTimeSeries.addOrUpdate(ts, maxTemperature);
					rangeTimeSeries.addOrUpdate(ts, maxTemperature - minTemperature);
				}

				
				// Calculate mean temperature
				meanTemperature = 0;
				j = 0;
				for (i = 0; i < NSENSOR; i++) {
					if (sensorTemperatureC[i] > MIN_T) {
						meanTemperature += sensorTemperatureC[i];
						j++;
					}
				}
				meanTemperature /= (float)j;
				meanTempTimeSeries.addOrUpdate(ts,meanTemperature);
				
				// Calc deviation from mean for each sensor
				for (i = 0; i < NSENSOR; i++) {
					if (sensorTemperatureC[i] > MIN_T) {
						dT = sensorTemperatureC[i] - meanTemperature;
						// Ignore anomalous readings
						if (dT < -0.5 || dT > 0.5) {
							continue;
						}
						deviationFromMeanTimeSeries[i].addOrUpdate(ts, dT);
						
						// Only interested in times where there is no human activity	
						cal.setTime(d);
						if (cal.get(Calendar.HOUR_OF_DAY) > 3 && cal.get(Calendar.HOUR_OF_DAY) < 11) {
							deviationFromMeanVsTemperatureSeries[i].add(temperatureC, dT);
						}
						
					}
				}
				
				// Reset
				currentMinute = minute;
				minTemperature = MAX_T;
				maxTemperature = MIN_T;
				for (i = 0; i < NSENSOR; i++) {
					sensorTemperatureC[i] = MIN_T;
				}
			} else {
				if (temperatureC > maxTemperature) {
					maxTemperature = temperatureC;
				}
				if (temperatureC < minTemperature) {
					minTemperature = temperatureC;
				}
				
			}
			
			timeseries[sensorNumber].addOrUpdate(ts, new Float(temperatureC));
		}
		
		
		// X Axis definition
		
		DateAxis axis = new DateAxis("Time");
		//axis.setDateFormatOverride(ChartUtil.getDateAxisFormatter(from, to));
		
		
		// Y Axis definitions
		
		NumberAxis temperatureAxis = new NumberAxis("Temperature C");
		temperatureAxis.setAutoRange(true);
		temperatureAxis.setAutoRangeIncludesZero(false);
		
		
		NumberAxis rangeAxis = new NumberAxis("Range C");
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoRangeIncludesZero(false);
		
	
		NumberAxis dTAxis = new NumberAxis("Deviation from Mean C");
		dTAxis.setAutoRange(true);
		dTAxis.setAutoRangeIncludesZero(false);
	
		
		
		// Temperature plot of all sensors
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		for (i = 0; i < NSENSOR; i++) {
			tsc.addSeries(timeseries[i]);
		}
		XYPlot temperaturePlot = new XYPlot(tsc, null,
				temperatureAxis, getPlotRenderer());
		
		
		// Temperature range (max-min) plot of all sensors 
		TimeSeriesCollection spreadCollection = new TimeSeriesCollection();
		spreadCollection.addSeries(rangeTimeSeries);
		XYPlot rangePlot = new XYPlot (spreadCollection, null, rangeAxis, getPlotRenderer());
		
		
		// Deviation from mean of all sensors
		TimeSeriesCollection deviationCollection = new TimeSeriesCollection();
		for (i = 0; i < NSENSOR; i++) {
			deviationCollection.addSeries(deviationFromMeanTimeSeries[i]);
		}
		XYPlot deviationVsTimePlot = new XYPlot(deviationCollection, null,
				dTAxis, getPlotRenderer());
		
		
		CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(axis);
		cplot.setGap(10.0);
		cplot.setDomainGridlinePaint(Color.lightGray);
		cplot.setDomainGridlinesVisible(true);
		cplot.setOrientation(PlotOrientation.VERTICAL);
		
		
		cplot.add(temperaturePlot);	
		
		cplot.add(rangePlot);
		
		cplot.add(deviationVsTimePlot);

		JFreeChart chart = new JFreeChart("Temperature Sensors",
				JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
		

		KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
		encoder.setEncodingAlpha(true);
		encoder.encode(chart.createBufferedImage(1200, 600, BufferedImage.BITMASK, null) , new FileOutputStream ("/var/tmp/chart.png"));

		
		
		// Deviation vs Temperature scatter plot

		XYSeriesCollection deviationFromMeanVsTemperatureCollection = new XYSeriesCollection();
		for (i = 0; i < NSENSOR; i++) {
			deviationFromMeanVsTemperatureCollection.addSeries(deviationFromMeanVsTemperatureSeries[i]);
		}
		
		XYDotRenderer dotRenderer = new XYDotRenderer();
        dotRenderer.setDotWidth(2);
        dotRenderer.setDotHeight(2);
        
        NumberAxis temperatureAxis2 = new NumberAxis("Temperature C");
		temperatureAxis2.setAutoRange(true);
		temperatureAxis2.setAutoRangeIncludesZero(false);
		
		XYPlot deviationFromMeanVsTemperaturePlot = new XYPlot(deviationFromMeanVsTemperatureCollection, 
				null,
				null, 
				dotRenderer);
		
		 JFreeChart chart2 = ChartFactory.createScatterPlot("Sensor Deviation From Mean Temperature vs Temperature",
	                "Temperature C", "Deviation From Mean Temperature C", deviationFromMeanVsTemperatureCollection, PlotOrientation.VERTICAL, true, true, false);
		 
		//JFreeChart chart2 = new JFreeChart("Deviation From Mean vs Temperature",
			//	JFreeChart.DEFAULT_TITLE_FONT, deviationFromMeanVsTemperaturePlot, false);
		
		
		encoder.encode(chart2.createBufferedImage(800, 800, BufferedImage.BITMASK, null) , new FileOutputStream ("/var/tmp/chart2.png"));

		
	}

	
	private static XYItemRenderer getPlotRenderer () {
		XYItemRenderer renderer = new StandardXYItemRenderer();
		renderer.setSeriesPaint(0, new Color(255,0,0));
		renderer.setSeriesPaint(1, new Color(0,0,255));
		renderer.setSeriesPaint(2, new Color(0,255,0));
		renderer.setSeriesPaint(3, new Color(0,255,255));
		return renderer;
	}
	
}
