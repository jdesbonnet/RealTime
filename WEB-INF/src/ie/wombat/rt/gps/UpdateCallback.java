package ie.wombat.rt.gps;

public interface UpdateCallback {
	public void gpsUpdate (double lat, double lon, double alt, double speed);
}
