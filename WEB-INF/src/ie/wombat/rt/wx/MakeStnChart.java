package ie.wombat.rt.wx;

import ie.wombat.rt.HibernateUtil;
import ie.wombat.rt.Station;
import ie.wombat.rt.tg.TideGaugeRecord;

import java.awt.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * Generate small station chart for inclusion in Google/Yahoo Maps
 * @author joe
 *
 */
public class MakeStnChart {

	private static SimpleDateFormat hqlf = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private static SimpleDateFormat dnyf = new SimpleDateFormat("dd MMM");

	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");

	public static JFreeChart createChart(int nHour, String stnId, String what)
			throws ServletException {

		Session hsession = HibernateUtil.currentSession();
		Transaction tx = hsession.beginTransaction();

		List stations = hsession.createQuery(
				"from Station where stationId='" + stnId + "'").list();

		if (stations.size() == 0) {
			throw new ServletException("station " + stnId + " not found");
		}

		Station stn = (Station) stations.get(0);

		TimeSeries series = new TimeSeries(stn.getStationId(), Minute.class);

		String query;
		List dbrecs;

		if ("we".equals(what)) {
			query = "from TideGaugeRecord " + " where gaugeId='" + stnId + "'"
					+ " and timestamp >= ? order by timestamp";

			Date sinceDate = new Date(System.currentTimeMillis() - (long) nHour
					* 3600000L);
			dbrecs = hsession.createQuery(query).setDate(0, sinceDate).list();
			for (int i = 1; i < dbrecs.size(); i++) {
				TideGaugeRecord r = (TideGaugeRecord) dbrecs.get(i);
				Minute ts = new Minute(r.getTimestamp());
				series.addOrUpdate(ts, r.getWaterElevation());
			}
		} else {

			String recordClass = null;
			if ("NRA".equals(stn.getStationType())) {
				recordClass = "NRAStationRecord";
			} else if ("WU".equals(stn.getStationType())) {
				recordClass = "WUStationRecord";
			} else if ("BUOY".equals(stn.getStationType())) {
				recordClass = "BuoyRecord";
			} else if ("APRSWX".equals(stn.getStationType())) {
				recordClass = "APRSWXStationRecord";
			} else if ("METAR".equals(stn.getStationType())) {
				recordClass = "METARRecord";
			}

			query = "from "
					+ recordClass
					+ " where stationId='"
					+ stnId
					+ "'"
					+ " and timestamp >= '"
					+ hqlf.format(new Date(System.currentTimeMillis()
							- (long) nHour * 3600000L))
					+ "' order by timestamp";

			dbrecs = hsession.createQuery(query).list();

			for (int i = 1; i < dbrecs.size(); i++) {
				WXRecord r = (WXRecord) dbrecs.get(i);
				Minute ts = new Minute(r.getTimestamp());

				if ("ap".equals(what)
						&& r.getAtmosphericPressureMillibars() != null) {

					if (r.getAtmosphericPressureMillibars().floatValue() > 100) {
						series.addOrUpdate(ts, r
								.getAtmosphericPressureMillibars());
					}
				} else if ("at".equals(what)
						&& r.getAirTemperatureCelsius() != null) {
					if (r.getAirTemperatureCelsius().floatValue() > -30) {
						series.addOrUpdate(ts, r.getAirTemperatureCelsius());
					}
				} else if ("st".equals(what)
						&& r.getSurfaceTemperatureCelsius() != null) {
					if (r.getSurfaceTemperatureCelsius().floatValue() > -30) {
						series
								.addOrUpdate(ts, r
										.getSurfaceTemperatureCelsius());
					}
				} else if ("ws".equals(what) && r.getWindSpeedKnots() != null) {
					float kmph = WXUtil.kn2kmph(r.getWindSpeedKnots()
							.floatValue());
					series.addOrUpdate(ts, new Float(kmph));
				} else if ("gs".equals(what)
						&& r.getWindMaxGustSpeedKnots() != null) {
					float kmph = WXUtil.kn2kmph(r.getWindMaxGustSpeedKnots()
							.floatValue());
					series.addOrUpdate(ts, new Float(kmph));
				} else if ("wd".equals(what) && r.getWindDirection() != null) {
					float d = r.getWindDirection().floatValue();
					if (d < 0) {
						continue;
					}
					series.addOrUpdate(ts, r.getWindDirection());
				} else if ("rh".equals(what) && r.getRelativeHumidity() != null) {
					series.addOrUpdate(ts, r.getRelativeHumidity());
				} else if ("pr".equals(what) && r.getRelativeHumidity() != null) {
					series.addOrUpdate(ts, r.getPrecipitationRate());
				}

			}
		}

		tx.commit();
		HibernateUtil.closeSession();

		String xLabel;
		if (nHour <= 24) {
			Date now = Calendar.getInstance().getTime();
			Date yesterday = new Date(now.getTime() - (long) 24 * 3600000L);
			xLabel = "Time (UTC) Date: " + dnyf.format(yesterday) + "/"
					+ df.format(now);
		} else {
			xLabel = "Time & Date (UTC)";
		}

		TimeSeriesCollection tsc = new TimeSeriesCollection();
		tsc.addSeries(series);

		String chartTitle = "";
		String yLabel = "";
		if ("ap".equals(what)) {
			chartTitle = "Atmospheric Pressure";
			yLabel = "hPa";
		} else if ("at".equals(what)) {
			chartTitle = "Air Temperature";
			yLabel = "Temperature (C)";
		} else if ("st".equals(what)) {
			chartTitle = "Surface/Sea Temperature";
			yLabel = "Temperature (C)";
		} else if ("ws".equals(what)) {
			chartTitle = "Wind Speed";
			yLabel = "km/h";
		} else if ("gs".equals(what)) {
			chartTitle = "Max Gust Speed";
			yLabel = "km/h";
		} else if ("wd".equals(what)) {
			chartTitle = "Wind Direction";
			yLabel = "degrees";
		} else if ("pr".equals(what)) {
			chartTitle = "Precipition Rate";
			yLabel = "mm/h";
		} else if ("rh".equals(what)) {
			chartTitle = "Relative Humidity";
			yLabel = "%";
		} else if ("we".equals(what)) {
			chartTitle = "Water Elevation";
			yLabel = "m";
		}

		JFreeChart chart;

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

		if ("wuprecip".equals(what)) {
			XYBarRenderer br = new XYBarRenderer();
			plot.setRenderer(br);
		} else {

		}

		// Date axis (horizontal)
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		if (nHour <= 24) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		} else if (nHour <= 48) {
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd-MMM"));
		} else {
			axis
					.setDateFormatOverride(new SimpleDateFormat(
							"dd-MMM"));
		}

		// JFreeChart chart = new JFreeChart(plot);
		// chart.setBackgroundPaint(Color.white);
		return chart;

	}

}