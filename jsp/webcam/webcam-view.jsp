<%@page import="java.util.Calendar"%><%@include file="_header.jsp"%><%

int tnCarouselInterval = 600;

String cameraId = request.getParameter("id");

ImageStore store = imageStores.get(cameraId);
context.put ("imageStore",store);
context.put ("cameraId",cameraId);

ImageId imageId = null;
if (request.getParameter("image_id") != null) {
	imageId = new ImageId(request.getParameter("image_id"));
	context.put ("mainImageURL", store.getImageURL(imageId));
} else {
	// bad API
	Date lastTimestamp = store.getLastImageTimestamp();
	if (lastTimestamp != null) {
		imageId = store.timestampToImageId(lastTimestamp);
		context.put ("mainImageURL", store.getCurrentImageURL());
	}
}



if (imageId != null) {
	context.put ("tf",tf);
	context.put ("ts",tsf.format(imageId.getTimestamp()));
}
  
out.clear();
TemplateRegistry.getInstance().merge("/webcam/webcam-view.vm",context,out);

%>