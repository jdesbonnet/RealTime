package ie.wombat.rt.webcam;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ImageStoreRegistry {
	
	private static Logger log = Logger.getLogger(ImageStoreRegistry.class.getName());

	private static ImageStoreRegistry instance = new ImageStoreRegistry();
	private static HashMap<String,ImageStore> stores = new HashMap<String,ImageStore>();

	private WebcamURLFetcher urlFetcher;
	
	public static ImageStoreRegistry getInstance () {
		return instance;
	}
	
	private ImageStoreRegistry () {
		log.info ("ImageStoreRegistry singleton created");
		//BasicConfigurator.configure();
	}
	
	
	public void init (File xmlFile, WebcamURLFetcher urlFetcher) throws IOException {
		this.urlFetcher = urlFetcher;
		readStoreConfiguration(xmlFile);
		
	}
	public ImageStore getStore (String id) {
		return stores.get(id);
	}
	public Set<ImageStore> getStores () {
		return new HashSet<ImageStore>(stores.values());
	}
	
	private void readStoreConfiguration (File xmlFile) throws IOException {
	
		SAXReader xmlReader = new SAXReader();
	    Document doc;
	    try {
	    	doc = xmlReader.read(xmlFile);
	    } catch (DocumentException e) {
	    	throw new IOException (e.getMessage());
	    }

	    List<Element>webcamEls = doc.getRootElement().selectNodes("//webcam");
	    
	    log.info ("Found " + webcamEls.size() + " webcams");
	    
	    for (Element webcamEl : webcamEls) {
	    	String camId = webcamEl.valueOf("@id");
	    	String driverClass = webcamEl.valueOf("@driver");
	    	String url = webcamEl.valueOf("@url");
	    	
	    	log.info (" cam " + camId + " at " + url + " driver=" + driverClass);
	    	if ("AxisPushRemoteImageStore".equals(driverClass)) {
	    		ImageStore store = new AxisPushRemoteImageStore(camId, url, urlFetcher);
	    		stores.put(camId,store);
	    	}
	    }
	    
	}
}
