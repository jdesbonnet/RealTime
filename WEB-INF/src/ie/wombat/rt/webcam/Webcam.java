package ie.wombat.rt.webcam;

public class Webcam {

	private String cameraId;
	
	private static double DEG2RAD = Math.PI / 180;
	private static double RAD2DEG = 180 / Math.PI;
	
	private int mappingModel=0;
	
	
	/** Lat & lon in radians */
	private double lat,lon;
	
	private double sinLat, cosLat;
	
	/** Altitude above sea level in meters */
	private double alt;
	
	/** Angle of view (x axis) in radians */
	private double fov;

	/** Azimuth (radians) of center position */
	private double azc;
	
	/** Anglular altitude above horizon (radians) of image center position */
	private double altc;
	
	/** Rotation of camera from verticle **/
	private double rotation;
	
	private double sinRotAngle, cosRotAngle;
	
	/** Left & Right azimuth (radians) */
	private double azmin,azmax;
	
	/** Lower & upper angular altitude (radians) */
	private double altmin,altmax;
	
	/** Width of view in pixels */
	private static double widthPx;
	
	/** Height of view in pixels */
	private static double heightPx = 480;
	
	/** */
	private double r;
	
	public Webcam (double latitudeDeg, double longitudeDeg,
				double azcDeg, double altcDeg, double rotationDeg,
				double widthPx, double heightPx,
				double fovDeg) {
		
		this.widthPx = widthPx;
		this.heightPx = heightPx;
		
		this.lat = latitudeDeg * DEG2RAD;
		this.lon = longitudeDeg * DEG2RAD;
		
		this.cosLat = Math.cos(lat);
		this.sinLat = Math.sin(lat);
		
		this.fov = fovDeg * DEG2RAD;
		this.azc = azcDeg * DEG2RAD;
		this.altc = altcDeg * DEG2RAD;
		
		// Change sign of  so as the rotation op compensates for rotation
		this.rotation = - rotationDeg * DEG2RAD;
		this.sinRotAngle = Math.sin(rotation);
		this.cosRotAngle = Math.cos(rotation);
		

		this.r = (widthPx / 2) / Math.tan(fov / 2);
		this.azmin = azc - fov/2;
		this.azmax = azc + fov/2;
		
		
	}

	public String getCameraId() {
		return cameraId;
	}

	public void setCameraId(String cameraId) {
		this.cameraId = cameraId;
	}
	
	public void setMappingModel (int m) {
		this.mappingModel = m;
	}

	public double getLongitudeDeg () {
		return this.lon * RAD2DEG;
	}
	public double getLatitudeDeg () {
		return this.lat * RAD2DEG;
	}

	/** 
	 * Return X,Y pixel coordinates in webcam image 
	 * corresponding to specified angular
	 * altitude and azimuth 
	 * @param alt Angular altitude in radians
	 * @param az Azimuth in radians (0 = North)
	 * @return
	 */
	public double[] toXY(double alt, double az) {
		double[] ret = new double[2];
		double x,y;
		
		switch (mappingModel) {
		
		case 0:
			double scale = widthPx / fov;
			System.err.println ("Webcam: scale=" + scale);
			ret[0] = scale * (az - azc);
			ret[1] = scale * (alt - altc);
			break;
		case 1:
			// Pixel angle depends on distance from center
			ret[0] = r * Math.tan(az - azc) ;
			ret[1] = r * Math.tan(alt - altc);
			break;
		}
		
		// Each pixel subtends same angle regardless of position
	
		// Apply rotation matrix
		ret[0] = ret[0] * cosRotAngle - ret[1] * sinRotAngle;
		ret[1] = ret[0] * sinRotAngle + ret[1] * cosRotAngle;

		ret[0] += widthPx/2;
		ret[1] = heightPx/2 - ret[1];
		
		return ret;
	}
	
}
