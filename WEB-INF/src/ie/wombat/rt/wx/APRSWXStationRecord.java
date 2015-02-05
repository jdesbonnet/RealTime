package ie.wombat.rt.wx;


import java.util.Date;

public class APRSWXStationRecord implements WXRecord {

	private Long id;
	private Date timestamp;
	private String stationId;
	
	private Float airTemperature;
	private Float windSpeed;
	private Float windDirection;
	private Float windMaxGustSpeed;
	private Float relativeHumidity;
	private Float precipitationLast60m;
	private Float precipitationLast24h;
	private Float precipitationLastDay;
	private Float atmosphericPressure;
	
	private String rawData;
	
	

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
	public Float getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(Float windSpeed) {
		this.windSpeed = windSpeed;
	}
	public Float getAtmosphericPressure() {
		return atmosphericPressure;
	}
	public void setAtmosphericPressure(Float atmosphericPressure) {
		this.atmosphericPressure = atmosphericPressure;
	}

	public Float getPrecipitationLast24h() {
		return precipitationLast24h;
	}
	public void setPrecipitationLast24h(Float precipitationLast24h) {
		this.precipitationLast24h = precipitationLast24h;
	}
	public Float getPrecipitationLast60m() {
		return precipitationLast60m;
	}
	public void setPrecipitationLast60m(Float precipitationLast60m) {
		this.precipitationLast60m = precipitationLast60m;
	}
	public Float getPrecipitationLastDay() {
		return precipitationLastDay;
	}
	public void setPrecipitationLastDay(Float precipitationLastDay) {
		this.precipitationLastDay = precipitationLastDay;
	}
	public Float getWindMaxGustSpeed() {
		return windMaxGustSpeed;
	}
	public void setWindMaxGustSpeed(Float windMaxGustSpeed) {
		this.windMaxGustSpeed = windMaxGustSpeed;
	}
	public String getRawData() {
		return rawData;
	}
	public void setRawData(String rawData) {
		this.rawData = rawData;
	}
	public Float getAirTemperatureCelsius() {
		if (getAirTemperature() == null) {
			return null;
		}
		return new Float (WXUtil.f2c(getAirTemperature().floatValue())); 
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
	public Float getPrecipitationRate () {
		return precipitationLast60m;
	}
}
