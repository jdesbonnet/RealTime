<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="ie.wombat.rt.tg.TideGaugeRecord"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="ie.wombat.rt.HibernateUtil"%>
<%@page import="org.hibernate.Session"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.io.FileNotFoundException"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.File"%>
<% 
	/*
	 * Parameters:
	 * date = yyyyMMdd (mandatory)
	 * dir = (cam image dir) (optional)
	 */
	 
	Calendar cal = Calendar.getInstance();

	String cam = "cam05";
	
	//File camDir = new File ("/home/joe/GalwayHarbourWebcam", cam);
	File camDir = new File ("/home/tomcat/opt/gg/htdocs/webcam", cam);
	
	String WGET="wget";
	String CHART_URL="http://localhost:8080/RealTime/chart/chart.png?chart=tgmeter\\&stn=GALWAY___1\\&width=200\\&height=200";
	String OVERLAY_URL="http://localhost:8080/RealTime/jsp/webcam/overlay-svg.jsp";
	String SKYPATH_URL="http://localhost:8080/RealTime/jsp/webcam/skypath-svg.jsp";
	File outDir = new File ("/var/tmp/tltest");
	
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	//SimpleDateFormat cidf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	SimpleDateFormat cidf = new SimpleDateFormat("yyyyMMddHHmmss");
	SimpleDateFormat tgdf = new SimpleDateFormat("yyyyMMddHHmmss");

	// Retrieve tide gauge records
	String date = request.getParameter("date");
	Date d0 = df.parse(date);
	Date d1 = new Date (d0.getTime() + 24*3600000);
	
	String stationId = "GALWAY___1";
	Session hsession = HibernateUtil.currentSession();
	Transaction tx = hsession.beginTransaction();
	List <TideGaugeRecord> list = hsession
	.createQuery("from TideGaugeRecord "
			+ " where gaugeId=? and timestamp >=? and timestamp<? "
			+ " order by timestamp")
			.setString(0,stationId)
			.setTimestamp(1,d0)
			.setTimestamp(2,d1)
			.list();
	int tgrListIndex = 0;
	
	// Where are webcam images located?
	File dayDir;
	if (request.getParameter("dir") != null) {
		dayDir = new File (request.getParameter("dir"));
	} else {
		dayDir = new File (camDir,date);
	}
	
	System.err.println ("dayDir=" + dayDir.getPath());
	
	if (! dayDir.exists()) {
		throw new ServletException (dayDir.getPath() + " does not exist");
	}
	
	out.clear();
	response.setContentType("text/plain");
	
	File skyPathSvgFile = new File (outDir, "skypath.svg");
	File skyPathPngFile = new File (outDir, "skypath.png");
	
	out.write (WGET + " -O " + skyPathSvgFile.getPath() + " "
			+ SKYPATH_URL + "?ts=" + date
			//+ "&azc=-53&altc=8&rot=2&fov=48"
			//+ "\\&azc=-51\\&altc=7\\&rot=-1\\&fov=54\\&mapping=0"
			
			// Harbour Cam
			//+ "\\&azc=-51.34\\&altc=6.75\\&rot=-1.113\\&fov=54.79\\&mapping=0"
			
			// Roscam
			+ "\\&azc=-99.63\\&altc=-0.672\\&rot=1.874\\&fov=54.79\\&mapping=0"
			+ "\n"
			);
	out.write ("convert -background none "
			+ skyPathSvgFile.getPath() 
			+ " "
			+ skyPathPngFile.getPath()
			+ "\n"
			);
	
	File[] imageFiles = dayDir.listFiles();
	
	File meterImageFile = null;
	
	Arrays.sort(imageFiles);
	for (int i = 0; i < imageFiles.length; i+=1) {
		
		File tmpFile = new File (outDir, "tmp" + (i+10000) + ".jpg");
		File outFile = new File (outDir, "frame" + (i+10000) + ".jpg");
		
		// TODO: remove need to change this code
		//String tss = imageFiles[i].getName().substring(6,21);
		String tss = imageFiles[i].getName().substring(7,21);
		
		Date d = cidf.parse(tss);
		
		// Hack
		cal.setTime(d);
		
		/*
		if (cal.get(Calendar.HOUR_OF_DAY) < 18) {
			out.write ("# Skipping frame number " + i + " ts=" + tss + "\n");
			continue;
		}
		*/
		
		
		
		
		File overlaySvgFile = new File (outDir, "overlay" + (i+10000) + ".svg");
		File overlayPngFile = new File (outDir, "overlay" + (i+10000) + ".png");
		
		out.write ("\n\n# Frame number " + i + " ts=" + tss + "\n");
		
		// Make planet overlay layer 
		/*
		out.write (WGET + " -O " + overlaySvgFile.getPath() + " "
				+ OVERLAY_URL + "?ts=" + tss + "\n"
				);
		out.write ("convert -background none "
				+ overlaySvgFile.getPath() 
				+ " "
				+ overlayPngFile.getPath()
				+ "\n"
				);
		*/
		
		// Combine skypath + image -> tmp
		out.write (" composite  " 
				+ skyPathPngFile.getPath()
				+ " " + imageFiles[i].getPath() 
				+ " " + tmpFile.getPath()
				+ "\n"
				);
		
		// Combine planet overlay + tmp -> tmp
		/*
		out.write (" composite  " 
				+ overlayPngFile.getPath()
				+ " " + tmpFile.getPath() 
				+ " " + tmpFile.getPath()
				+ "\n"
				);
		*/
		
		/*
		while (list.get(tgrListIndex).getTimestamp().before(d) ) {
			tgrListIndex++;
			if (tgrListIndex >= list.size()) {
				meterImageFile = null;
				break;
			}
			meterImageFile = new File (outDir,"meter"+tgrListIndex+".png");
			out.write (WGET + " -O " + meterImageFile.getPath() + " "
					+ CHART_URL + "\\&rid=" + list.get(tgrListIndex).getId()
					+"\n");
		}
		
		if (meterImageFile != null) {
			// tidegauge + tmp -> tmp
			out.write (" composite -gravity SouthEast " 
				+ meterImageFile.getPath()
				+ " " + tmpFile.getPath() 
				+ " " + tmpFile.getPath()
				+ "\n"
				);
		}
		*/
		
		out.write ("mv " + tmpFile.getPath() 
				+ " " + outFile.getPath() + "\n");
		
		out.flush();
	}
	
%>