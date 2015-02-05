package ie.wombat.rt.bp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;


public class Charts {

	private static final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");

	
	public static JFreeChart makeChart (BufferedReader lnr) throws IOException {
		
		
		TimeSeries sysSeries = new TimeSeries("Systolic", Minute.class);
		TimeSeries diaSeries = new TimeSeries("Diastolic", Minute.class);
		TimeSeries hrSeries = new TimeSeries("Heart rate", Minute.class);
		
		long t;
		
		// Read and discard first line to ensure in sync
		String line = lnr.readLine();
		
		int n = 0;
		while (  (line = lnr.readLine()) != null ) {
			String[] p = line.split(",");
			
			if (p.length < 3) {
				continue;
			}
			
			try {
				Date d = df.parse(p[0]);
				t = d.getTime();
			} catch (ParseException e) {
				continue;
			} 
			
			Float sys = new Float (p[1]);
			Float dia = new Float (p[2]);
			Float hr = new Float (p[3]);
			
			Minute ts = new Minute(new Date(t));
			
			sysSeries.addOrUpdate(ts,sys);
			diaSeries.addOrUpdate(ts,dia);
			hrSeries.addOrUpdate(ts,hr);

		}
		
		
		
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		tsc.addSeries(sysSeries);
		tsc.addSeries(diaSeries);
		tsc.addSeries(hrSeries);
		
		
		TimeSeries sysMaSeries = MovingAverage.createMovingAverage(sysSeries, "MAv", 90, 60);
		TimeSeries diaMaSeries = MovingAverage.createMovingAverage(diaSeries, "MAv", 90, 60);
		//TimeSeries hrMaSeries = MovingAverage.createMovingAverage(hrSeries, "MAv", 90, 60);
	
		tsc.addSeries(sysMaSeries);
		tsc.addSeries(diaMaSeries);
		//tsc.addSeries(hrMaSeries);
		
		JFreeChart chart;

		String xLabel = "Time";
		String yLabel = "mmHg / bpm";
		
		chart = ChartFactory.createTimeSeriesChart(null, // title
				xLabel, // x-axis label
				yLabel, // y-axis label
				tsc, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);
		chart.setBackgroundPaint(Color.white);
		// chart.setPadding(new RectangleInsets(0,0,0,0));

		
		

		
		XYPlot plot = (XYPlot) chart.getPlot();

		
		
		//XYDifferenceRenderer renderer = new XYDifferenceRenderer(Color.red, Color.red, false);
		BPChartRenderer renderer = new BPChartRenderer ();
		renderer.setBasePaint(Color.black);
		renderer.setBaseOutlinePaint(Color.black);
		//renderer.setShapesVisible(true);
		//renderer.setBaseShapesVisible(true);
		//renderer.setBaseShapesFilled(true);
		
		Stroke dashedLine = new BasicStroke(
		        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
		        1.0f, new float[] {6.0f, 6.0f}, 0.0f
		    );
		
		// Use circle shapes
		renderer.setSeriesShape(0, renderer.getSeriesShape(1));
		
		// Set moving average to have 50% alpha and broken line, no shapes
		for (int i = 3; i < 5; i++) {
			renderer.setSeriesShapesVisible(i, false);
			float[] c = new float[4];
			float[] ca = ((Color)renderer.getSeriesPaint(i)).getColorComponents(null);
			Color cc = new Color (c[0],c[1],c[2],0.5f);
			for (int j = 0; j < 3; j++) {
				System.err.println ("ca[j]=" + ca[j]);
			}
			renderer.setSeriesPaint(i, cc);
			renderer.setSeriesStroke(i, dashedLine);
		}
		
		
		plot.setRenderer(renderer);
		
		
		
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

	
		

		return chart;

	}
}
