package ie.wombat.rt.eirgrid;

import java.util.Date;

public class ForecastDemandRecord implements PowerRecord {
	public Long id;
	public Date timestamp;
	public Float power;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Float getPower() {
		return power;
	}
	public void setPower(Float power) {
		this.power = power;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
}
