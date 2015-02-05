package ie.wombat.rt.eirgrid;

import java.util.Date;

public interface PowerRecord {

	public Date getTimestamp();
	public void setTimestamp(Date ts);
	public Float getPower();
	public void setPower(Float p);
}
