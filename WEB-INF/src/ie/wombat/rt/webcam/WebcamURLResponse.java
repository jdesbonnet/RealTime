package ie.wombat.rt.webcam;

import java.util.Map;

public class WebcamURLResponse {

	public int status;
	public String content;
	
	/**
	 * Response headers. Header names are converted to lower case.
	 */
	public Map<String,String> headers;
}
