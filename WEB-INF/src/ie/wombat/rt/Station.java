package ie.wombat.rt;

public class Station {

	private Long id;
	private String stationId;
	private String name;
	private String stationType;
	
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private String timezone;
	private Integer timezoneOffset;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public Double getAltitude() {
		return altitude;
	}
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	public Double getLatitudeDeg () {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public Double getLongitudeDeg() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
	public Integer getTimezoneOffset() {
		return timezoneOffset;
	}
	public void setTimezoneOffset(Integer timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}
	public String getStationType() {
		return stationType;
	}
	public void setStationType(String stationType) {
		this.stationType = stationType;
	}
	
	
	
}
