package ie.wombat.rt.wx;


import java.util.Date;

/**
 * @author joe
 *
 */
public class METARRecord implements WXRecord {

	private static final Float FLOAT_ZERO = new Float (0);
	
	private Long id;
	private String stationId;
	private Date timestamp;
	
	private Float airTemperature;
	private Float atmosphericPressure;
	private Float relativeHumidity;
	private Float windDirection;
	private Float windSpeed;
	private Float windMaxGustSpeed;
	
	private String rawData;
	
	
	public Float getAirTemperature() {
		return airTemperature;
	}
	public void setAirTemperature(Float airTemperature) {
		this.airTemperature = airTemperature;
	}
	public Float getAtmosphericPressure() {
		return atmosphericPressure;
	}
	public void setAtmosphericPressure(Float atmosphericPressure) {
		this.atmosphericPressure = atmosphericPressure;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Float getRelativeHumidity() {
		return relativeHumidity;
	}
	public void setRelativeHumidity(Float relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public Float getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(Float windDirection) {
		this.windDirection = windDirection;
	}
	public Float getWindMaxGustSpeed() {
		return windMaxGustSpeed;
	}
	public void setWindMaxGustSpeed(Float windMaxGustSpeed) {
		this.windMaxGustSpeed = windMaxGustSpeed;
	}
	public Float getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(Float windSpeed) {
		this.windSpeed = windSpeed;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
	public Float getAirTemperatureCelsius() {
		return getAirTemperature();
	}
	public Float getAtmosphericPressureMillibars() {
		return getAtmosphericPressure();
	}
	public Float getSurfaceTemperatureCelsius() {
		return null;
	}
	public Float getWindMaxGustSpeedKnots() {
		return getWindMaxGustSpeed();
	}
	public Float getWindSpeedKnots() {
		return getWindSpeed();
	}
	public String getRawData() {
		return rawData;
	}
	public void setRawData(String rawData) {
		this.rawData = rawData;
	}
	
	public Float getPrecipitationRate () {
		return FLOAT_ZERO;
	}
	

}
