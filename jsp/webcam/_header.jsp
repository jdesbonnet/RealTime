<%@page import="java.util.Collections"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="ie.wombat.rt.webcam.ImageId"%>
<%@page import="ie.wombat.rt.webcam.Webcam"%>
<%@page import="java.util.ArrayList"%><%@page import="java.util.Date"%>
<%@page import="java.io.File"%>
<%@page import="java.io.IOException"%>
<%@page import="ie.wombat.rt.webcam.AxisPushImageStore"%>
<%@page import="java.util.HashMap"%>
<%@page import="ie.wombat.rt.webcam.ImageStore"%>
<%@page import="java.util.Map"%>
<%@page import="ie.wombat.template.TemplateRegistry"%>
<%@page import="ie.wombat.template.Context"%>
<%@page import="ie.wombat.rt.Station"%>
<%@page import="java.util.List"%>
<%@page import="org.hibernate.Session"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.rt.HibernateUtil"%><%!

	private static String WEBCAM_IMAGE_STORE_ROOT = "/media/Elements/Webcams";
	//private static String WEBCAM_IMAGE_STORE_ROOT = "/home/tomcat/opt/gg/htdocs/webcam";
	
	private static String WEBCAM_IMAGE_URL_ROOT = "http://www.galway.net/galwayguide/webcam";
	
	public static List<String> cameraIds = new ArrayList();
	public static Map<String, Webcam> cameraMetadata = new HashMap<String,Webcam>();
	
	static Map<String,ImageStore> imageStores = new HashMap<String,ImageStore>();
	public static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	public static SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
	public static SimpleDateFormat tsf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	// Hardcoded init of cameras
	static {
		
		File camRootDir = new File(WEBCAM_IMAGE_STORE_ROOT + "/cam05");
		String camRootURL = WEBCAM_IMAGE_URL_ROOT + "/cam05";
		ImageStore is = new AxisPushImageStore ("cam05",camRootDir,camRootURL);
		is.setMaxInterval(300000); // 5 min
		imageStores.put("cam05",is);
		
		camRootDir = new File(WEBCAM_IMAGE_STORE_ROOT + "/cam06");
		camRootURL = WEBCAM_IMAGE_URL_ROOT + "/cam06";
		is = new AxisPushImageStore ("cam06",camRootDir,camRootURL);
		is.setMaxInterval(300000); // 5 min
		imageStores.put("cam06",is);
		
		camRootDir = new File(WEBCAM_IMAGE_STORE_ROOT + "/cam07");
		camRootURL = WEBCAM_IMAGE_URL_ROOT + "/cam07";
		is = new AxisPushImageStore ("cam07",camRootDir,camRootURL);
		is.setMaxInterval(300000); // 5 min
		imageStores.put("cam07",is);
		
		
		camRootDir = new File(WEBCAM_IMAGE_STORE_ROOT + "/cam09");
		camRootURL = WEBCAM_IMAGE_URL_ROOT + "/cam09";
		is = new AxisPushImageStore ("cam09",camRootDir,camRootURL);
		is.setMaxInterval(300000); // 5 min
		imageStores.put("cam09",is);
		
		
		
		
		// cameraIds are the keys of imageStores
		cameraIds.addAll(imageStores.keySet());
		Collections.sort(cameraIds);
		
		
		double latitudeDeg,longitudeDeg, azc,altc,rot,width,height,fov;
		

		azc = 0; altc=0; rot=0;
		width=640; height=480;
		fov=55;
		
		latitudeDeg = 53.269066;
		longitudeDeg = -9.0472358;
		Webcam cam05 = new Webcam (
				latitudeDeg, longitudeDeg,
				azc, altc, rot, // azc altc and rotation (degrees)
				width,height, // webcam image dimenaions (pixels)
				fov // fov (degrees)
				);
		cameraMetadata.put ("cam05",cam05);
		
		
		latitudeDeg = 53.2677;
		longitudeDeg =  -9.0427;
		Webcam cam06 = new Webcam (
				latitudeDeg, longitudeDeg,
				azc, altc, rot, // azc altc and rotation (degrees)
				width,height, // webcam image dimenaions (pixels)
				fov // fov (degrees)
				);
		cameraMetadata.put ("cam06",cam06);
		
		
		latitudeDeg = 53.02908;
		longitudeDeg =  -9.2934;
		Webcam cam07 = new Webcam (
				latitudeDeg, longitudeDeg,
				azc, altc, rot, // azc altc and rotation (degrees)
				width,height, // webcam image dimenaions (pixels)
				fov // fov (degrees)
				);
		cameraMetadata.put ("cam07",cam07);
		
		
		latitudeDeg = 53.24742707064503;
		longitudeDeg =  -8.97881609183969;
		Webcam cam09 = new Webcam (
				latitudeDeg, longitudeDeg,
				azc, altc, rot, // azc altc and rotation (degrees)
				width,height, // webcam image dimenaions (pixels)
				fov // fov (degrees)
				);
		cameraMetadata.put ("cam09",cam09);
		
		// Pad to demo more cameras
		//imageStores.put ("cam90",imageStores.get("cam05"));
		//imageStores.put ("cam91",imageStores.get("cam05"));
		//imageStores.put ("cam92",imageStores.get("cam05"));
		//imageStores.put ("cam93",imageStores.get("cam05"));
		
		
	}
	
	public static long getLastImageTimestampAsLong(String cameraId) {
		ImageStore store = imageStores.get(cameraId);
		if (store == null) {
			return 0;
		}
		return store.getLastImageTimestamp().getTime();
	}
%><%
	Session hsession = HibernateUtil.currentSession();
	Transaction tx = hsession.beginTransaction();
	

	Context context = new Context(request,response);
	context.put ("YUI","http://yui.yahooapis.com/2.5.2/build");
	//context.put ("locations",locations);
	context.put ("cameraIds",cameraIds);
	context.put ("cameraMetadata",cameraMetadata);
	context.put ("imageStores",imageStores);
	context.put ("contextPath",request.getContextPath());
	context.put ("jsp",this);
	
	context.put ("imageStoreRoot","http://www.galway.net/galwayguide/webcam");
	
	
%>