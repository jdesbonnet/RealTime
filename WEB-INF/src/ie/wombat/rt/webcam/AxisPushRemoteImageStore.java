package ie.wombat.rt.webcam;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//import org.apache.commons.httpclient.Header;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
//import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Access Axis Push store using metadata in Apache mod_autoindex and HTTP GET.
 * 
 * @author joe
 *
 */
public class AxisPushRemoteImageStore implements ImageStore {

	private static Logger log = Logger.getLogger(AxisPushRemoteImageStore.class.getName());
	
	private static final long DEFAULT_MAX_INTERVAL = 3600000; // 60 minutes

	private static final List<ImageId>EMPTY_IMAGEID_LIST = new ArrayList<ImageId>(0);
	
	private static String lastImageFilename = "current.jpg";
	
	private String cameraId;
	private String rootURL;
	private WebcamURLFetcher urlFetcher;
	
	
	private static SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd");
	private static SimpleDateFormat tsf = new SimpleDateFormat ("yyyyMMdd-HHmmss");
	private long maxInterval = DEFAULT_MAX_INTERVAL;
	
	/** Keep server responses cached here. Key is date string yyyyMMdd */
	private Map<String,List<ImageId>> cachedResponses = new HashMap<String,List<ImageId>>();
	private Map<String,Date> cacheEntryTimestamp = new HashMap<String,Date>();
	
	public AxisPushRemoteImageStore (String cameraId, String rootURL, WebcamURLFetcher urlFetcher) {
		
		log.info("AxisPushRemoteImageStore initialized for " + cameraId);
		
		this.cameraId = cameraId;
		this.rootURL = rootURL;
		this.urlFetcher = urlFetcher;
	}
	
	public String getId() {
		return cameraId;
	}
	
	
	public Date getLastImageTimestamp() throws IOException {
		String url = rootURL + "/current.jpg";
		log.info(url);
		
		/*
		HttpClient client = new HttpClient();
		HeadMethod headMethod = new HeadMethod(url);
		int status = client.executeMethod(headMethod);
		
		// TODO: fix
		if (status == 404) {
			log.warn("No current image found for camera " + getId() + ". Returning t=0");
			return new Date(0);
		}
		
		if (status != 200) {
			log.warn ("Received unexpected status code from server. Expecting 200, got " + status);
		}
		
		
		// Got valid response to HEAD request. Extract date from response.
		Header header = headMethod.getResponseHeader("Last-Modified");
		*/
		
		WebcamURLResponse response = urlFetcher.head(url);
		//String dateStr = header.getValue();
		String dateStr = response.headers.get("last-modified");
		try {
			//return DateUtil.parseDate();
			return DateUtil.parseDate(dateStr);
		} catch (DateParseException e) {
			log.severe ("Error parsing Last-Modified date in headers of HEAD response. Received " 
					+ dateStr);
			return null;
		}
	}

	public boolean isAlive() {
		Date now = new Date(System.currentTimeMillis());
		
		try {
			Date lastImageTimestamp = getLastImageTimestamp();
			long interval = now.getTime() - lastImageTimestamp.getTime();
			return (interval < maxInterval);
		} catch (IOException e) {
			return false;
		}
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
		String dateStr = df.format(date);
		if (cachedResponses.containsKey(dateStr)) {
			return cachedResponses.get(dateStr);
		} else {
			return fetchFromServer (dateStr);
		}
	}
	
	private List<ImageId> fetchFromServer (String dateStr) throws IOException {
		String url = rootURL + "/" + dateStr + "?F=0";
		
		/*
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url);
		int status = client.executeMethod(getMethod);
		*/
		WebcamURLResponse response = urlFetcher.get(url);
		int status = response.status;
		
		List<ImageId> ret = new ArrayList<ImageId>();
		
		// Day directory not found. Return empty list.
		if (status == 404) {
			return ret;
		}

		// Response other than 200 or 404 is unexpected
		if (status != 200) {
			throw new IOException ("Expected HTTP status 200 from server but got " + status);
		}
		
		try {
			//String content = getMethod.getResponseBodyAsString();
			String content = response.content;
			Document doc = parseHTML(content);
			List<Element> liEls = doc.getRootElement().selectNodes("//li");
			for (Element liEl : liEls) {
				String imageId = liEl.valueOf("a/@href");
				if ( ! imageId.endsWith(".jpg")) {
					continue;
				}
				//log.info("adding imageId=" + imageId);
				ImageId id = new ImageId(imageId);
				ret.add(id);
			}
		} catch (DocumentException e) {
			throw new IOException (e);
		}
		
		// Cache response
		addToCache (dateStr, ret);
		return ret;
	}
	
	public ImageId getImageByTimestamp (Date timestamp) {
		return null;
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
		/*
		String url = rootURL + "/" + df.format(date) + "?F=2";
		
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url);
		
		int status = client.executeMethod(getMethod);
		SAXReader xmlReader = new SAXReader();
		String html = getMethod.getResponseBodyAsString();
		String xml = "<?xml version=\"1.0\" ?>" + html;
		try {
			Document doc = DocumentHelper.parseText(xml);
			List<Element> liEls = doc.getRootElement().selectNodes("li");
			return liEls.size()-1;
		} catch (DocumentException e) {
			throw new IOException (e);
		}
		*/
		return query(date).size();
	}
	
	public List<Date> getImageAvailability () throws IOException {
		
		List<Date> ret = new ArrayList<Date>();
		String url = rootURL + "/?F=0";
		
		/*
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url);
		int status = client.executeMethod(getMethod);
		*/
		
		WebcamURLResponse response = urlFetcher.get(url);
		int status = response.status;
		
		if (status != 200) {
			throw new IOException ("Expected HTTP status 200 from server but got " + status);
		}
		
		try {
			//String content = getMethod.getResponseBodyAsString();
			String content = response.content;
			//System.err.println ("returnedContent=" + content);
			Document doc = parseHTML(content);
			List<Element> liEls = doc.getRootElement().selectNodes("//li");
			for (Element liEl : liEls) {
				String href = liEl.valueOf("a/@href");
				if (href==null  || href.length() != 9) {
					log.info("skipping " + href);
					continue;
				}
				String dateStr = href.substring(0,8);
				try {
					ret.add(df.parse(dateStr));
				} catch (ParseException e) {
					log.warning("could not parse " + dateStr + " as date");
				}
			}

			return ret;
		} catch (DocumentException e) {
			log.severe("Error parsing HTML/XML: " + e);
			throw new IOException (e);
		}
	}
	/**
	 * Image ids in format cam??-yyyyMMdd-HHmmss.jpg
	 */
	public File getImageFile (ImageId imageId) {
		return null;
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
	public String getRootURL () {
		return this.rootURL;
	}
	
	
	private List<ImageId> filenameArrayToImageIdList (String[] filenames) {
		List<ImageId> ret = new ArrayList<ImageId>(filenames.length);
		for (int i = 0; i < filenames.length; i++) {
			ret.add(new ImageId(filenames[i]));
		}
		return ret;
	}

	private void addToCache (String dateStr, List<ImageId> imageList) {
		cachedResponses.put(dateStr, imageList);
		cacheEntryTimestamp.put(dateStr, new Date());
	}
	
	/**
	 * Convert HTML returned by Apache mod_autoindex module into Document.
	 * @param html
	 * @return
	 * @throws DocumentException
	 */
	private static Document parseHTML (String html) throws DocumentException {
		// Skip DOCTYPE directive and start parsing XML at html element
		int startOfHtmlIndex = html.indexOf("<html>");
		String xml = "<?xml version=\"1.0\" ?>" + html.substring(startOfHtmlIndex);
		Document doc = DocumentHelper.parseText(xml);
		return doc;
	}
}