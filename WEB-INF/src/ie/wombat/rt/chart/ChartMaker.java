package ie.wombat.rt.chart;

import java.util.Date;

import org.jfree.chart.JFreeChart;

public interface ChartMaker {
	public JFreeChart makeChart (Date from, Date to);
}
