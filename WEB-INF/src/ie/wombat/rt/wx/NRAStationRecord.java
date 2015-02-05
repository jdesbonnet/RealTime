package ie.wombat.rt.wx;


import java.util.Date;

public class NRAStationRecord implements WXRecord {

	private static final Float FLOAT_ZERO = new Float (0);
	
	private Long id;
	private Date timestamp;
	private String stationId;
	
	private Float airTemperature;
	private Float roadTemperature;
	private Float windSpeed;
	private Float windDirection;
	private Float relativeHumidity;
	private Integer precipitationStatus;
	private String roadCondition;
	public Float getAirTemperature() {
		return airTemperature;
	}
	public void setAirTemperature(Float airTemperature) {
		this.airTemperature = airTemperature;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * @return Precipitation status:
	 * 0: No recent rainfall;
	 * 1: Light rainfall;
	 * 2: Medium rainfall;
	 * 3: Heavy rainfall;
	 */
	public Integer getPrecipitationStatus() {
		return precipitationStatus;
	}
	public void setPrecipitationStatus(Integer precipitationStatus) {
		this.precipitationStatus = precipitationStatus;
	}
	
	/**
	 * Guessing status -> rate
	 */
	public Float getPrecipitationRate() {
		switch (precipitationStatus.intValue()) {
		case 0: return FLOAT_ZERO;
		case 1: return new Float(1);
		case 2: return new Float(4);
		case 3: return new Float(8);
		}
		return FLOAT_ZERO;
	}
	public Float getRelativeHumidity() {
		return relativeHumidity;
	}
	public void setRelativeHumidity(Float relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}
	public String getRoadCondition() {
		return roadCondition;
	}
	public void setRoadCondition(String roadCondition) {
		this.roadCondition = roadCondition;
	}
	public Float getRoadTemperature() {
		return roadTemperature;
	}
	public void setRoadTemperature(Float roadTemperature) {
		this.roadTemperature = roadTemperature;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Float getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(Float windDirection) {
		this.windDirection = windDirection;
	}
	
	/**
	 * Wind speed in km/h
	 * @return
	 */
	public Float getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(Float windSpeed) {
		this.windSpeed = windSpeed;
	}
	public Float getAirTemperatureCelsius() {
		return getAirTemperature();
	}
	public Float getAtmosphericPressureMillibars() {
		return null;
	}
	
	public Float getSurfaceTemperatureCelsius() {
		return getRoadTemperature();
	}
	public Float getWindMaxGustSpeedMetersPerSecond() {
		return null;
	}
	public Float getWindSpeedKnots() {
		// TODO: replace unit conversions with method calls
		return new Float (WXUtil.kmph2kn(getWindSpeed().floatValue()));
	}
	public Float getWindMaxGustSpeedKnots () {
		return null;
	}
	
}
