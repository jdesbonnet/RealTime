package ie.wombat.rt.chart;

import ie.wombat.rt.eirgrid.EirgridChartMaker;
import ie.wombat.rt.tg.MakeTideChart;
import ie.wombat.rt.wx.MakeNRAStationChart;
import ie.wombat.rt.wx.MakeWUStationChart;
import ie.wombat.rt.wx.MakeWXChart;
import ie.wombat.rt.wx.MakeWindChart;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;


public class ChartServlet extends HttpServlet {

	private static final int DEFAULT_WIDTH=640;
	private static final int DEFAULT_HEIGHT=480;
	private static final int MAX_WIDTH=1400;
	private static final int MAX_HEIGHT=1000;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd");
	private static final SimpleDateFormat dtf = new SimpleDateFormat ("yyyyMMddHHmmss");
	
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		JFreeChart chart;
		String chartTitle = request.getParameter("title");
		
		
		Date from = null;
		Date to = null;
		
		// Try yyyyMMdd or yyyyMMddHHmmss format depending on length
		try {
			String fromStr = request.getParameter("from");
			String toStr = request.getParameter("to");
			if (fromStr.length() == 14) {
				from = dtf.parse(fromStr);
			} else {
				from = df.parse(fromStr);
			}
			if (toStr.length() == 14) {
				to = dtf.parse(toStr);
			} else {
				to = df.parse(toStr);
			}
		} catch (Exception e) {
			//
		}
		
		int nHour = 48;
		try {
			nHour = Integer.parseInt(request.getParameter("nhour"));
		} catch (Exception e) {
			// ignore
		}
		
		Calendar cal = Calendar.getInstance();
		if (to == null) {
			to = cal.getTime();
		}
		if (from == null) {
			cal.add(Calendar.HOUR_OF_DAY, nHour * -1);
			from = cal.getTime();
		}
		
		String stnListStr = request.getParameter("stations");
		
		String chartType = request.getParameter("chart");
		boolean includeDataCredit = true;
		
		if ("false".equals(request.getParameter("datacredit"))) {
			includeDataCredit = false;
		}
		
		if (chartType == null || "".equals(chartType) || "tidegauge".equals(chartType)) {
			chart = MakeTideChart.createTideChart(nHour,chartTitle, stnListStr);
		} else if (chartType.startsWith("wu")) {
			chart = MakeWUStationChart.createChart (nHour,chartType);
		} else if (chartType.startsWith("wx")) {
			chart = MakeWXChart.createChart(nHour, chartType, stnListStr);
		} else if (chartType.startsWith("nra")) {
            chart = MakeNRAStationChart.createChart (nHour,chartType);
        } else if ("histogram".equals(chartType)) {
			chart = MakeTideChart.createHistogramChart(nHour, chartTitle);
		} else if ("wind".equals(chartType)) {
			String stationId = request.getParameter("stn");
			if (stationId == null) {
				stationId="62095";
			}
			chart = MakeWindChart.createWindChart(nHour, stationId, chartTitle);
        } else if ("mem".equals(chartType)) {
        	ServerPerformanceChartMaker chartMaker = new ServerPerformanceChartMaker();
            chart = chartMaker.makeChart (from,to);
        } else if ("eirgrid".equals(chartType)) {
        	if (from != null && to != null) {
        		EirgridChartMaker chartMaker = new EirgridChartMaker();
        		chart = chartMaker.makeChart(from, to);
        	} else {
        		chart = EirgridChartMaker.createChart(nHour);
        	}
        } else if ("tgmeter".equals(chartType)) {
        	String stationId = request.getParameter("stn");
        	if (request.getParameter("d") != null) {
        		Date d;
        		try {
        			d = dtf.parse(request.getParameter("d"));
        		} catch (Exception e) {
        			d = Calendar.getInstance().getTime();
        		}
        		chart = MakeTideChart.createMeter(stationId, d);
        	}
        	else {
        		Long recordId = new Long(request.getParameter("rid"));
        		chart = MakeTideChart.createMeter(stationId, recordId);
        	}
		} else {
			chart = MakeTideChart.createTideChart(nHour,chartTitle, stnListStr);
		}
		
		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();
		
		int width = DEFAULT_WIDTH;
		try {
			width = Integer.parseInt(request.getParameter("width"));
		} catch (Exception e) {
			// ignore
		}
		
		int height = DEFAULT_HEIGHT;
		try {
			height = Integer.parseInt(request.getParameter("height"));
		} catch (Exception e) {
			// ignore
		}
		
		if (width > MAX_WIDTH) {
			width = MAX_WIDTH;
		}
		
		if (height > MAX_HEIGHT) {
			height = MAX_HEIGHT;
		}
		
		
		
		//ChartUtilities.writeChartAsPNG(out, chart, width, height);
		
		KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
		encoder.setEncodingAlpha(true);
		encoder.encode(chart.createBufferedImage(width, height, BufferedImage.BITMASK, null) , out);

		
	}

}
