package ie.wombat.rt.wx;

import java.util.Date;

public interface WXRecord {

	public String getStationId();
	
	/**
	 * Time of observation in UTC
	 * @return
	 */
	public Date getTimestamp();
	
	/**
	 * Air temperature (from venelated enclosure) in degrees celsius.
	 * @return
	 */
	public Float getAirTemperatureCelsius();
	
	/**
	 * Surface (or sea) temperature in degrees celsius.
	 * @return
	 */
	public Float getSurfaceTemperatureCelsius();
	
	/**
	 * Atmospheric pressure (sea level) in mb (or hPa)
	 * @return
	 */
	public Float getAtmosphericPressureMillibars();
	
	/**
	 * Relative humidity in %
	 * @return
	 */
	public Float getRelativeHumidity();
	
	/**
	 * Wind speed in m/s
	 * @return
	 */
	public Float getWindSpeedKnots();
	
	/**
	 * Wind direction in degrees (0 north, 90 east, 180 south, 270 west). This is the
	 * direction from which the wind comes from -- wind arrows need to point in the
	 * opposite direction.
	 * @return
	 */
	public Float getWindDirection();
	
	/**
	 * Maximum gust speed in m/s
	 */
	public Float getWindMaxGustSpeedKnots ();
	
	/**
	 * Precipitation rate in mm/h
	 * @return
	 */
	public Float getPrecipitationRate();
}
