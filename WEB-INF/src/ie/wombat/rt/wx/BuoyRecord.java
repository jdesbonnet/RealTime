package ie.wombat.rt.wx;


import java.text.SimpleDateFormat;
import java.util.Date;

public class BuoyRecord implements WXRecord {
	
	private static final Float FLOAT_ZERO = new Float (0);
	private SimpleDateFormat df = new SimpleDateFormat("HH:mm dd-MMM-yyyy");

	private Long id;
	private String stationId;
	private Date timestamp;
	private Float atmosphericPressure;
	private Float pressureTendency;
	private Float charPressureTendency;
	private Float windDirection;
	private Float windSpeed;
	private Float windMaxGustSpeed;
	private Float wavePeriod;
	private Float waveHeight;
	private Float waveDirection;
	private Float waveSpread;
	private Float seaTemperature;
	private Float dryBulbTemperature;
	private Float dewPointTemperature;
	private Float relativeHumidity;
	private Float salinity;
	private Float conductivity;
	
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String buoyId) {
		this.stationId = buoyId;
	}

	/**
	 * Unit: mb.
	 * @return
	 */
	public Float getAtmosphericPressure() {
		return atmosphericPressure;
	}
	
	/**
	 * Unit: mb.
	 * @param atmosphericPressure
	 */
	public void setAtmosphericPressure(Float atmosphericPressure) {
		this.atmosphericPressure = atmosphericPressure;
	}
	
	public Float getCharPressureTendency() {
		return charPressureTendency;
	}
	public void setCharPressureTendency(Float charPressureTendency) {
		this.charPressureTendency = charPressureTendency;
	}
	public Float getDewPointTemperature() {
		return dewPointTemperature;
	}
	public void setDewPointTemperature(Float dewPointTemperature) {
		this.dewPointTemperature = dewPointTemperature;
	}
	public Float getDryBulbTemperature() {
		return dryBulbTemperature;
	}
	public void setDryBulbTemperature(Float dryBulbTemperature) {
		this.dryBulbTemperature = dryBulbTemperature;
	}
	public Float getPressureTendency() {
		return pressureTendency;
	}
	public void setPressureTendency(Float pressureTendency) {
		this.pressureTendency = pressureTendency;
	}
	public Float getRelativeHumidity() {
		return relativeHumidity;
	}
	public void setRelativeHumidity(Float relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}
	
	/**
	 * Unit: Degrees C.
	 * @return
	 */
	public Float getSeaTemperature() {
		return seaTemperature;
	}
	
	/**
	 * Unit: Degrees C.
	 * @param seaTemperature
	 */
	public void setSeaTemperature(Float seaTemperature) {
		this.seaTemperature = seaTemperature;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Float getWaveHeight() {
		return waveHeight;
	}
	public void setWaveHeight(Float waveHeight) {
		this.waveHeight = waveHeight;
	}
	
	/**
	 * Unit: second
	 * @return
	 */
	public Float getWavePeriod() {
		return wavePeriod;
	}
	
	/**
	 * Unit: second 
	 * @param wavePeriod
	 */
	public void setWavePeriod(Float wavePeriod) {
		this.wavePeriod = wavePeriod;
	}
	
	/**
	 * Unit: degrees. (N = ?)
	 * @return
	 */
	public Float getWindDirection() {
		return windDirection;
	}
	
	/**
	 * Unit: degrees. (N = ?)
	 * @param windDirection
	 */
	public void setWindDirection(Float windDirection) {
		this.windDirection = windDirection;
	}
	
	/**
	 * Unit: knots
	 * @return
	 */
	public Float getWindMaxGustSpeed() {
		return windMaxGustSpeed;
	}
	
	/**
	 * Unit: knots
	 * @param windMaxGustSpeed
	 */
	public void setWindMaxGustSpeed(Float windMaxGustSpeed) {
		this.windMaxGustSpeed = windMaxGustSpeed;
	}
	
	/** 
	 * Unit: knots 
	 * **/
	public Float getWindSpeed() {
		return windSpeed;
	}
	
	/**
	 * Unit: knots
	 * @param windSpeed
	 */
	public void setWindSpeed(Float windSpeed) {
		this.windSpeed = windSpeed;
	}
	
	
	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append (df.format(getTimestamp()));
		buf.append (" ap=" + getAtmosphericPressure() +"mb");
		buf.append (" wind=" + 
				getWindDirection()
				+ "deg/"
				+ getWindSpeed() + "kn/"
				+ getWindMaxGustSpeed() + "kn"
				);
		buf.append (" waves=" + getWaveHeight() + "m/"
				+ getWavePeriod() +"s");
		buf.append (" temp="
				+ getSeaTemperature()
				+ "C/"
				+ getDryBulbTemperature()
				+ "C/"
				+ getDewPointTemperature()
				+ "C/"
				+ getRelativeHumidity()
				+ "%"
				);
		
		return buf.toString();
	}

	public Float getAirTemperatureCelsius() {
		return getDryBulbTemperature();
	}

	public Float getAtmosphericPressureMillibars() {
		return getAtmosphericPressure();
	}

	public Float getSurfaceTemperatureCelsius() {
		return getSeaTemperature();
	}

	public Float getWindMaxGustSpeedKnots() {
		return getWindMaxGustSpeed();
	}

	public Float getWindSpeedKnots() {
		return getWindSpeed();
	}
	
	public Float getPrecipitationRate () {
		return FLOAT_ZERO;
	}

	public Float getWaveDirection() {
		return waveDirection;
	}

	public void setWaveDirection(Float waveDirection) {
		this.waveDirection = waveDirection;
	}

	/**
	 * Salinity in psu (practical salinity units).
	 * See http://en.wikipedia.org/wiki/Salinity
	 * @return
	 */
	public Float getSalinity() {
		return salinity;
	}

	public void setSalinity(Float salinity) {
		this.salinity = salinity;
	}

	/**
	 * Conductivity in Siemens / meter.
	 * 
	 * @return
	 */
	public Float getConductivity() {
		return conductivity;
	}

	public void setConductivity(Float conductivity) {
		this.conductivity = conductivity;
	}

	/**
	 * Mean wave spread (degrees)
	 * @return
	 */
	public Float getWaveSpread() {
		return waveSpread;
	}

	public void setWaveSpread(Float waveSpread) {
		this.waveSpread = waveSpread;
	}
	
	
}
