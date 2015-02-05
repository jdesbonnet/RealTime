package ie.wombat.rt.chart;

import ie.wombat.rt.tg.MakeTideChart;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import org.jfree.chart.JFreeChart;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class MakeChartApp extends ApplicationFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MakeChartApp(String title) {
		super(title);

		ChartPanel chartPanel = (ChartPanel) createDemoPanel();
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);

	}


	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 * 
	 * @return A panel.
	 */
	public static JPanel createDemoPanel() {
		JFreeChart chart = MakeTideChart.createTideChart(48,null, null);
		return new ChartPanel(chart);
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {

		MakeChartApp demo = new MakeChartApp("Galway Tide Gauge");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}
