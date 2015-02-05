package ie.wombat.rt.webcam;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;



public interface ImageStore {
	
	public String getId();

	/**
	 * Get List of ImageId there startTime <= image exposure time <= endTime
	 *  
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IOException
	 */
	public List<ImageId> query (Date startTime, Date endTime) throws IOException;
	public List<ImageId> query (Date date) throws IOException;
	
	/**
	 * Return imageId corresponding to timestamp, or nearest image if exact
	 * match not found.
	 * @param date
	 * @return
	 * @throws IOException
	 */
	public ImageId getImageByTimestamp (Date date) throws IOException;
	
	/**
	 *  Return imageId corresponding to timestamp, or nearest image if exact
	 *  match not found. Limit search to ImageId objects in 'images'.
	 *  
	 * @param date
	 * @param images
	 * @return
	 * @throws IOException
	 */
	public ImageId getImageByTimestamp (Date date, List<ImageId> images) throws IOException;
	/**
	 * Get image count for day of date
	 * @param date
	 * @return
	 * @throws IOException
	 */
	
	
	/**
	 * Get dates for which images are available. TODO: need better name.
	 */
	public List<Date> getImageAvailability () throws IOException;
	
	public int getImageCount (Date date) throws IOException;
	
	/**
	 * Indicates data is being received (determined by maxInterval)
	 */
	public boolean isAlive ();
	
	public Date getLastImageTimestamp() throws IOException;
	
	// Used to determine if camera is off-line
	public void setMaxInterval (long t);
	
	public File getImageFile (ImageId imageId);
	public String getRootURL ();
	public String getImageURL (ImageId imageId);
	public String getCurrentImageURL ();
	
	//public ImageId timestampToImageId (Date timestamp);
}
