package ie.wombat.rt.wx;


import java.util.Date;

public class WUStationRecord implements WXRecord {
	
	
	private Long id;
	private String stationId;
	
	private Date timestamp;
	
	private Float temperature;
	private Float dewPointTemperature;
	private Float atmosphericPressure;
	private Float windDirection;
	private Float windSpeed;
	private Float windMaxGustSpeed;
	private Float relativeHumidity;
	private Float precipitation;
	
	private String csvRecord;

	public Float getAtmosphericPressure() {
		return atmosphericPressure;
	}

	public void setAtmosphericPressure(Float atmosphericPressure) {
		this.atmosphericPressure = atmosphericPressure;
	}

	public String getCsvRecord() {
		return csvRecord;
	}

	public void setCsvRecord(String csvRecord) {
		this.csvRecord = csvRecord;
	}

	public Float getDewPointTemperature() {
		return dewPointTemperature;
	}

	public void setDewPointTemperature(Float dewPointTemperature) {
		this.dewPointTemperature = dewPointTemperature;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Float getPrecipitation() {
		return precipitation;
	}
	
	/**
	 * Precipitation in mm/h
	 */
	public Float getPrecipitationRate() {
		return new Float( precipitation.floatValue() * 25.4);
	}

	public void setPrecipitation(Float precipitation) {
		this.precipitation = precipitation;
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

	public Float getTemperature() {
		return temperature;
	}

	public void setTemperature(Float temperature) {
		this.temperature = temperature;
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

	public Float getAirTemperatureCelsius() {
		if (getTemperature() == null) {
			return null;
		}
		return new Float (WXUtil.f2c(getTemperature().floatValue())); 
	}

	public Float getAtmosphericPressureMillibars() {
		if (getAtmosphericPressure() == null) {
			return null;
		}
		return new Float (WXUtil.inHg2mb(getAtmosphericPressure().floatValue()));
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
	
	
	
}
