package ie.wombat.rt.webcam;

import java.io.IOException;

/**
 * Abstract HTTP fetch implementation. Can be Apache Commons HTTP Client or
 * Google App Engine URL fetcher. (The latter is mandatory on Google App Engine).
 * @author joe
 *
 */
public interface WebcamURLFetcher {

	public WebcamURLResponse head (String url) throws IOException;
	public WebcamURLResponse get (String url) throws IOException;
}
