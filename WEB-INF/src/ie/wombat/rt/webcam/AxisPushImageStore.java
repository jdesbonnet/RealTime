package ie.wombat.rt.webcam;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author joe
 *
 */
public class AxisPushImageStore implements ImageStore {

	private static final long DEFAULT_MAX_INTERVAL = 3600000; // 60 minutes

	private static final List<ImageId>EMPTY_IMAGEID_LIST = new ArrayList<ImageId>(0);
	
	private static String lastImageFilename = "current.jpg";
	
	private String cameraId;
	private File rootDir;
	private String rootURL;
	
	private static SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd");
	private static SimpleDateFormat tsf = new SimpleDateFormat ("yyyyMMdd-HHmmss");
	private long maxInterval = DEFAULT_MAX_INTERVAL;
	
	public AxisPushImageStore (String cameraId, File rootDir, String rootURL) {
		this.cameraId = cameraId;
		this.rootDir = rootDir;
		this.rootURL = rootURL;
	}
	
	public String getId() {
		return cameraId;
	}
	
	public Date getLastImageTimestamp() {
		
		File lastImageFile = new File(rootDir,lastImageFilename);
		
		if (lastImageFile.exists()) {
			return new Date(lastImageFile.lastModified());
		}
		
		System.err.println (lastImageFile.getPath() + " does not exist");
		
		// Get most recent directory (last in listFiles() which is a directory)
		File lastDir = null;
		File[] dirs = rootDir.listFiles();
		if (dirs == null) {
			return null;
		}
		for (int i = dirs.length-1; i >= 0; i++) {
			if (dirs[i].isDirectory()) {
				lastDir = dirs[i];
				break;
			}
		}
		
		// Make sure we have lastDir
		if (lastDir == null) {
			// TODO: deal with this
			return null;
		}
		
		File[] images = lastDir.listFiles();
		if (images.length == 0) {
			// TODO: deal with this
			return null;
		}
		File lastImage = images[images.length-1];
		
		try {
			return new ImageId(lastImage.getName()).getTimestamp();
		} catch (ParseException e) {
			// TODO: deal with this
			return null;
		}
	}

	public boolean isAlive() {
		Date now = new Date(System.currentTimeMillis());
		Date lastImageTimestamp = getLastImageTimestamp();
		long interval = now.getTime() - lastImageTimestamp.getTime();
		return (interval < maxInterval);
	}

	public long getMaxInterval() {
		return maxInterval;
	}

	public void setMaxInterval(long maxInterval) {
		this.maxInterval = maxInterval;
	}

	public List<ImageId> query(Date startTime, Date endTime) throws IOException {
		
		if (endTime.before(startTime)) {
			return new ArrayList<ImageId>(0);
		}
		
		String startDay = df.format(startTime);
		String endDay = df.format(endTime);
		
		List<ImageId> ret = new ArrayList<ImageId>();
		
		ImageId key = timestampToImageId(startTime);
		List<ImageId> list = query(startTime);
		int i = Collections.binarySearch(list, key);
		if (i < 0) {
			i = -i - 1;
		}
		
		// Start and end times are on same day, so uses same list to find end
		if (endDay.equals(startDay)) {
			key = timestampToImageId(endTime);
			int j = Collections.binarySearch(list, key);
			if (j < 0) {
				j = -j -1;
			}
			ret.addAll (list.subList(i,j));
			return ret;
		}
		
		
		
		
		
		// Add from i to end of list to ret
		ret.addAll(list.subList(i, list.size()));
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTime);
		
		// Add complete days inbetween startTime and endTime
		while (true) {
			cal.add(Calendar.HOUR_OF_DAY,24);
			String day = df.format(cal.getTime());
			if (day.equals(endDay)) {
				break;
			}
			ret.addAll(query(cal.getTime()));
		}
		
		// Last day
		list = query(cal.getTime());
		key = timestampToImageId(endTime);
		int j = Collections.binarySearch(list, key);
		if (j < 0) {
			j = -j -1;
		}
		ret.addAll (list.subList(0,j));
		
		
		return ret;
	}
	
	
	public List<ImageId> query (Date date) throws IOException {
		File imageDir = new File (rootDir,df.format(date));
		if (imageDir.exists() && !imageDir.isDirectory()) {
			throw new java.io.IOException (imageDir.getPath() + " is not a directory");
		}
		String[] images = imageDir.list();
		if (images == null) {
			images = new String[0];
		}
		Arrays.sort(images);
		return filenameArrayToImageIdList(images);
	}
	
	public ImageId getImageByTimestamp (Date timestamp) {
		File dateDir = getDateDir (timestamp);
		String[] images = dateDir.list();
		
		// If no images, and early in day, try previous day
		if (images == null || images.length == 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(timestamp);
			if (cal.get(Calendar.HOUR_OF_DAY) < 1) {
				cal.add(Calendar.HOUR_OF_DAY, -2);
				dateDir = getDateDir (timestamp);
				images = dateDir.list();
			}
		}
		
		// No images available
		if (images == null || images.length == 0) {
			return null;
		}
		
		Arrays.sort(images);
		List<ImageId> imageIdList = filenameArrayToImageIdList(images);
		return getImageByTimestamp(timestamp, imageIdList);
	}
	
	public ImageId getImageByTimestamp (Date timestamp, List<ImageId> images) {
		
		ImageId key = timestampToImageId (timestamp);
		int j = Collections.binarySearch(images, key);
		
		// if j >= 0 then exact match found
		if (j >= 0) {
			System.err.println ("Returning " + images.get(j) + " for " + key);
			return images.get(j);
		}
		
		// If j negative then -j gives nearest timestamp
		j = -j - 1;
		if (j >= images.size()) {
			j = images.size()-1;
		}
		System.err.println ("Returning " + images.get(j) + " for " + key );
		return images.get(j);
	}
	
	public int getImageCount (Date date) throws IOException {
		File imageDir = new File (rootDir,df.format(date));
		if (imageDir.exists() && !imageDir.isDirectory()) {
			throw new java.io.IOException (imageDir.getPath() + " is not a directory");
		}
		String[] images = imageDir.list();
		if (images == null) {
			return 0;
		}
		return images.length;
	}
	
	/**
	 * Image ids in format cam??-yyyyMMdd-HHmmss.jpg
	 */
	public File getImageFile (ImageId imageId) {
		String dateStr = imageId.getId().substring(6,14);
		File imageDir = new File (rootDir, dateStr);
		File imageFile = new File (imageDir, imageId.getId());
		return imageFile;
	}
	public String getImageURL (ImageId imageId) {
		String dateStr = imageId.getId().substring(6,14);
		return rootURL + "/" + dateStr + "/" + imageId.getId();
	}
	public String getCurrentImageURL () {
		return rootURL + "/current.jpg";
	}
	
	public ImageId timestampToImageId (Date timestamp) {
		return new ImageId(this.cameraId + "-" + tsf.format(timestamp) + ".jpg");
	}
	
	private File getDateDir (Date date) {
		return new File (rootDir, df.format(date));
	}
	
	private List<ImageId> filenameArrayToImageIdList (String[] filenames) {
		List<ImageId> ret = new ArrayList<ImageId>(filenames.length);
		for (int i = 0; i < filenames.length; i++) {
			ret.add(new ImageId(filenames[i]));
		}
		return ret;
	}

	public List<Date> getImageAvailability() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getRootURL () {
		return this.rootURL;
	}

}