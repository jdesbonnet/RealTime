package ie.wombat.rt.pothole;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class Charts {

	public static JFreeChart chartLine (File dataDir, double x0, double y0, double x1, double y1) {
		// Eqn of line a * x + b * y = c
		double b = x1 - x0;
		double a = y1 - y0;
		
		//double a2 = a*a;
		//double b2 = b*b;
		double l = Math.sqrt(a*a + b*b);

		// Line segment bounding box (bx0, by0) -> (bx1, by1) where first coord is always bottom left
		double bx0, by0, bx1, by1;
		if (x0 < x1) {
			bx0 = x0;
			bx1 = x1;
		} else {
			bx0 = x1;
			bx1 = x0;
		}
		if (y0 < y1) {
			by0 = y0;
			by1 = y1;
		} else {
			by0 = y1;
			by1 = y0;
		}
		
		// v is unit vector perpendicular to AB. Divide by |AB| to get unit vector.
		double vi = a / l;
		double vj = - b / l;

		
		double d,r;
		double xpp, ypp;

		XYSeriesCollection xysc = new XYSeriesCollection();
		
		File[] files = dataDir.listFiles();
		for (File f : files) {
			if (!f.getName().startsWith("trip-")) {
				continue;
			}
			System.err.println(f.getPath());

			try {
				
				XYSeries series = new XYSeries(f.getName());
				xysc.addSeries(series);
				
				List<TimeAccelerationPosition> tapRecords = Utils.getTimeAccelerationPositionRecords(f);
				for (TimeAccelerationPosition tap : tapRecords) {
					
					// d = perpendicular distance to line
					//d = ((y0 - tap.latitude) * b  -  (x0 - tap.longitude) * a) / l;
					d = ( (tap.longitude - x0) * a - (tap.latitude - y0) * b) / l;
						
					if (d < 0.0005 && d > -0.0005) {
						
						// Intersection point is found my moving along the perpendicular unit vector v by d units.
						xpp = tap.longitude - vi*d;
						ypp = tap.latitude - vj*d;
						
						// Ok: close to infinite line, but is it in the line segment?
						// This will fail if x0 == x1
						if ( (bx0 <= xpp) && (xpp <= bx1)  && (by0 <= ypp) && (ypp <= by1) ) {
							
							// How far along the line segment?
							r = Math.sqrt((xpp-x0)*(xpp-x0) + (ypp-y0)*(ypp-y0));
							
							series.add(r, tap.g[2]);
						} 
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		
		JFreeChart chart;
	
		chart = ChartFactory.createXYLineChart(
				"Vehicle Z-Axis (vertical) vibration + gravity (m/s²)", // title
				"longitude", // x-axis label
				"acceleration", // y-axis label
				xysc, // data
				PlotOrientation.VERTICAL,
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);
		//chart.setBackgroundPaint(Color.white);
		// chart.setPadding(new RectangleInsets(0,0,0,0));
	
		return chart;
		

	}
	
	
	public static JFreeChart chartRoute (File dataDir, List<double[]> route) {
		
		int i;
		
		LineSegment[] lineSegments = new LineSegment[route.size() - 1];
		
		for (i = 0; i < route.size() - 1; i++) {
			LineSegment seg = new LineSegment();
			lineSegments[i] = seg;
			seg.x0 = route.get(i)[1];
			seg.y0 = route.get(i)[0];
			seg.x1 = route.get(i+1)[1];
			seg.y1 = route.get(i+1)[0];
			seg.a = seg.y1 - seg.y0;
			seg.b = seg.x1 - seg.x0;
			seg.l = Math.sqrt (seg.a*seg.a + seg.b*seg.b);
			
			// Line segment bounding box (bx0, by0) -> (bx1, by1) where first coord is always bottom left
			if (seg.x0 < seg.x1) {
				seg.bx0 = seg.x0; seg.bx1 = seg.x1;
			} else {
				seg.bx0 = seg.x1; seg.bx1 = seg.x0;
			}
			if (seg.y0 < seg.y1) {
				seg.by0 = seg.y0; seg.by1 = seg.y1;
			} else {
				seg.by0 = seg.y1; seg.by1 = seg.y0;
			}
			
			// v is unit vector perpendicular to AB. Divide by |AB| to get unit vector.
			seg.vi = seg.a / seg.l;
			seg.vj = - seg.b / seg.l;			
		}
		
	
		
		
		
		
		double d,r;
		double xpp, ypp;
		double y = -9000;
		float alpha = 0.2f;
		double e3 = 0;

		XYSeriesCollection xysc = new XYSeriesCollection();
		
		File[] files = dataDir.listFiles();
		for (File f : files) {
			if (!f.getName().startsWith("trip-")) {
				continue;
			}
			System.err.println(f.getPath());

			try {
				
				XYSeries series = new XYSeries(f.getName());
				xysc.addSeries(series);
				
				List<TimeAccelerationPosition> tapRecords = Utils.getTimeAccelerationPositionRecords(f);
				for (TimeAccelerationPosition tap : tapRecords) {
					
					r = 0;
					for (i = 0; i < lineSegments.length; i++) {
						LineSegment seg = lineSegments[i];
					
						// d = perpendicular distance to line
						d = ( (tap.longitude - seg.x0) * seg.a - (tap.latitude - seg.y0) * seg.b) / seg.l;
						
						if (d < 0.0005 && d > -0.0005) {
						
							// Intersection point is found my moving along the perpendicular unit vector v by d units.
							xpp = tap.longitude - seg.vi*d;
							ypp = tap.latitude - seg.vj*d;
						
							// Ok: close to infinite line, but is it in the line segment?
							// This will fail if x0 == x1
							if ( (seg.bx0 <= xpp) && (xpp <= seg.bx1)  && (seg.by0 <= ypp) && (ypp <= seg.by1) ) {
							
								// set first lpf output = first input value
								if (y < -1000) {
									y = tap.g[2];
								}
								
								y +=  alpha * (tap.g[2]-y);
								//lpfSeries.addOrUpdate(ts, y);
								
								// High frequency signal
								//e1Series.addOrUpdate(ts, (g[2]-y));
								
								// e1 ^ 2 (high frequency energy)
								//e2Series.addOrUpdate(ts, (g[2]-y) * (g[2]-y)* 20);	
								
								// e2 through low pass filter
								e3 += 0.1 * ( (tap.g[2]-y) * (tap.g[2]-y)* 10 - e3);
								//e3Series.addOrUpdate(ts, e3);	
								
								
								// How far along the line segment?
								series.add(r + Math.sqrt((xpp-seg.x0)*(xpp-seg.x0) + (ypp-seg.y0)*(ypp-seg.y0)), 
										//tap.g[2]
										e3
										      );
							} 
						}
						r += seg.l;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	
		
		JFreeChart chart;
	
		chart = ChartFactory.createXYLineChart(
				"Vehicle Z-Axis (vertical) vibration + gravity (m/s²)", // title
				"distance along route (undefined units)", // x-axis label
				"acceleration", // y-axis label
				xysc, // data
				PlotOrientation.VERTICAL,
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);
		//chart.setBackgroundPaint(Color.white);
		// chart.setPadding(new RectangleInsets(0,0,0,0));
	
		return chart;
		

	}
	public static JFreeChart makeChart (BufferedReader lnr, long tStart, long tEnd) throws IOException {
		
		
		TimeSeries series = new TimeSeries("a", Millisecond.class);
		TimeSeries lpfSeries = new TimeSeries("lpf", Millisecond.class);
		
		TimeSeries e1Series = new TimeSeries("e1", Millisecond.class);
		TimeSeries e2Series = new TimeSeries("e2", Millisecond.class);
		TimeSeries e3Series = new TimeSeries("e3", Millisecond.class);
		
		long t;
		float[] g = new float[3];
		
		// LPF filter
		float y = -9999;
		float dt = 1/50;
		//float alpha = dt/(0.05f+dt);
		float alpha = 0.2f;
		
		float e1=0, e2=0, e3=0;
		
		// Read and discard first line to ensure in sync
		String line = lnr.readLine();
		
		int n = 0;
		while (  (line = lnr.readLine()) != null ) {
			String[] p = line.split(" ");
			t = Long.parseLong(p[1]);

			
			if ( t < tStart) {
				continue;
			}
			
			
			if ( t > tEnd ) {
				break;
			}
			
			
			
			if ("G".equals(p[0])) {
				g[0] = Float.parseFloat (p[2]);
				g[1] = Float.parseFloat (p[3]);
				g[2] = Float.parseFloat (p[4]);
				
				 
				 
				//Second ts = new Second(new Date(t));
				Millisecond ts = new Millisecond(new Date(t));
				
				
				series.addOrUpdate(ts,g[2]);
				
				// set first lpf output = first input value
				if (y < - 1000) {
					y = g[2];
				}
				
				y +=  alpha * (g[2]-y);
				lpfSeries.addOrUpdate(ts, y);
				
				// High frequency signal
				e1Series.addOrUpdate(ts, (g[2]-y));
				
				// e1 ^ 2 (high frequency energy)
				e2Series.addOrUpdate(ts, (g[2]-y) * (g[2]-y)* 20);	
				
				// e2 through low pass filter
				e3 += 0.1 * ( (g[2]-y) * (g[2]-y)* 10 - e3);
				e3Series.addOrUpdate(ts, e3);	
				n++;
			}
		}
		
		System.err.println (" ** N=" + n);
		
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		tsc.addSeries(series);
		tsc.addSeries(lpfSeries);
		tsc.addSeries(e1Series);
		tsc.addSeries(e2Series);
		tsc.addSeries(e3Series);

		
		JFreeChart chart;

		String xLabel = "time";
		String yLabel = "a";
		
		chart = ChartFactory.createTimeSeriesChart(null, // title
				xLabel, // x-axis label
				yLabel, // y-axis label
				tsc, // data
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);
		chart.setBackgroundPaint(Color.white);
		// chart.setPadding(new RectangleInsets(0,0,0,0));

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
		//DateAxis axis = (DateAxis) plot.getDomainAxis();
		/*
		if (nHour <= 24) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		} else if (nHour <= 48) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd-MMM"));
		} else {
			axis
					.setDateFormatOverride(new SimpleDateFormat(
							"dd-MMM"));
		}
		*/

		// JFreeChart chart = new JFreeChart(plot);
		// chart.setBackgroundPaint(Color.white);
		return chart;

	}
}
