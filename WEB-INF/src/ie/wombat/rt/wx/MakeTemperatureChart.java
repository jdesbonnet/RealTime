package ie.wombat.rt.wx;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.general.DefaultValueDataset;

public class MakeTemperatureChart {

	public static JFreeChart createThermometerChart() {

		ThermometerPlot plot = new ThermometerPlot();
		plot.setUnits(ThermometerPlot.UNITS_CELCIUS);

		plot.setRange(-10, 40);
		plot.setSubrange(ThermometerPlot.CRITICAL, -10, 5);
		plot.setSubrange(ThermometerPlot.WARNING, 5, 14);
		plot.setSubrange(ThermometerPlot.NORMAL, 14, 18);
		plot.setSubrange(ThermometerPlot.WARNING, 18, 24);
		plot.setSubrange(ThermometerPlot.CRITICAL, 25, 40);

		// plot.setRangeInfo(ThermometerPlot.NORMAL, 0.0, 55.0, 0.0, 100.0);
		// plot.setRangeInfo(ThermometerPlot.WARNING, 55.0, 75.0, 0.0, 100.0);
		// plot.setRangeInfo(ThermometerPlot.CRITICAL, 75.0, 100.0, 0.0, 100.0);

		DefaultValueDataset vd = new DefaultValueDataset();
		vd.setValue(new Double(31.2));
		plot.setDataset(vd);

		JFreeChart chart = new JFreeChart(plot);
		return chart;

	}


}
