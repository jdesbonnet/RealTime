package ie.wombat.rt.tg;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TideGaugeRecord {

	private static SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	private Long id;
	private String gaugeId;
	private Date timestamp;
	private Float waterElevation;
	private Integer atmosphericPressure;
	public Integer getAtmosphericPressure() {
		return atmosphericPressure;
	}
	public void setAtmosphericPressure(Integer atmosphericPressure) {
		this.atmosphericPressure = atmosphericPressure;
	}
	public String getGaugeId() {
		return gaugeId;
	}
	public void setGaugeId(String gaugeId) {
		this.gaugeId = gaugeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Float getWaterElevation() {
		return waterElevation;
	}
	public void setWaterElevation(Float waterElevation) {
		this.waterElevation = waterElevation;
	}
	
	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append("TideGaugeRecord: gaugeId=");
		buf.append (getGaugeId());
		buf.append (" timestamp=");
		buf.append (df.format(getTimestamp()));
		buf.append (" we=");
		buf.append (getWaterElevation());
		buf.append (" ap=");
		buf.append (getAtmosphericPressure());
		return buf.toString();
	}
	
}
