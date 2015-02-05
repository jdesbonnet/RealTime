package ie.wombat.rt.webcam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageId implements Comparable<ImageId> {

	private static SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd");
	private static SimpleDateFormat tsf = new SimpleDateFormat ("yyyyMMdd-HHmmss");
	
	private String imageId;

	public ImageId (String imageId) {
		this.imageId = imageId;
	}
	
	public String getId() {
		return imageId;
	}

	public void setId(String imageId) {
		this.imageId = imageId;
	}
	
	public Date getTimestamp () throws ParseException {
		String ts = imageId.substring(6, 21);
		Date d = tsf.parse (ts);
		return d;
	}

	public int compareTo(ImageId o) {
		return this.imageId.compareTo(o.imageId);
	}
	
}
