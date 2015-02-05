
<%@page import="java.text.ParseException"%><%@page import="java.util.Calendar"%><%@include file="_header.jsp"%><%!
public  void writeImageJson (ImageStore store, ImageId imageId, JspWriter out)
	throws IOException, ParseException {
	out.write ("{id:\"");
	out.write (imageId.getId());
	out.write ("\", t:\"");
	out.write (tf.format(imageId.getTimestamp()));
	out.write ("\", url:\"");
	out.write (store.getImageURL(imageId));
	out.write ("\"}");
}
%><%

int defaultImageInterval = 600;
int defaultCarouselSize = 7;

Calendar cal = Calendar.getInstance();

String cameraId = request.getParameter("id");
ImageStore store = imageStores.get(cameraId);

// Center timestamp
Date timestamp;
if (request.getParameter("ts") != null) {
	timestamp = tsf.parse(request.getParameter("ts"));
} else {
	timestamp = cal.getTime();
}

int imageInterval;
if (request.getParameter("ii")!=null) {
	imageInterval = Integer.parseInt(request.getParameter("ii"));
} else {
	imageInterval = defaultImageInterval;
}

int carouselSize;
if (request.getParameter("cs") != null) {
	carouselSize = Integer.parseInt(request.getParameter("cs"));
} else {
	carouselSize = defaultCarouselSize;
}
int halfCarouselSize = carouselSize / 2;


cal.setTime(timestamp);
cal.add(Calendar.SECOND, - imageInterval * halfCarouselSize);
Date startTime = cal.getTime();
cal.setTime(timestamp);
cal.add(Calendar.SECOND,imageInterval * halfCarouselSize);
Date endTime = cal.getTime();


out.clear();
//response.setContentType("application/javascript");
response.setContentType("text/plain");

out.write ("{ urlPrefix:\"http://www.galway.net/galwayguide/webcam\",\n");
out.write ("startTime:\"" + tsf.format(startTime) + "\",\n");
out.write ("endTime:\"" + tsf.format(endTime) + "\",\n");

// Carousel image candidates
List<ImageId>images = store.query(startTime,endTime);

// No images, return empty image array
if (images.size() == 0) {
	out.write ("nImages: 0,\nimages: []\n}");
	return;
}


Date firstImageTime = images.get(0).getTimestamp();
Date lastImageTime = images.get(images.size()-1).getTimestamp();


out.write ("nImages: " + images.size() + ",\n");
out.write ("images: [\n");



if (images.size() > 0) {
	int i = 0;
	cal.setTime(startTime);
	while (cal.getTime().before(timestamp) && i < halfCarouselSize) {
		ImageId imageId = store.getImageByTimestamp(cal.getTime(),images);
		writeImageJson (store,imageId,out);
		out.write (",\n");
		i++;
		cal.add(Calendar.SECOND,imageInterval);
	}
}

ImageId currentImageId = store.getImageByTimestamp(timestamp);
writeImageJson(store,currentImageId, out);
out.write (",\n");

if (images.size() > 0) {
	int i = 0;
	cal.setTime(timestamp);
	cal.add(Calendar.SECOND,imageInterval);
	while (cal.getTime().before(lastImageTime) && i < halfCarouselSize) {
		ImageId imageId = store.getImageByTimestamp(cal.getTime(),images);
		writeImageJson (store,imageId,out);
		out.write (",\n");
		i++;
		cal.add(Calendar.SECOND,imageInterval);
	}
}

out.write ("] }");
%>