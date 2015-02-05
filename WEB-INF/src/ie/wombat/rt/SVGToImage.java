package ie.wombat.rt;


import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;


import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

public class SVGToImage {

	public static void renderSVG (String svg, String mimetype, OutputStream out) throws IOException, TranscoderException {
	
	/*
	 * Transcode SVG file into a graphic
	 */
	Transcoder transcoder;
	
	if ("image/png".equals(mimetype)) {
		transcoder = new PNGTranscoder();
	} else {
		transcoder = new PDFTranscoder ();
	}
	
	/*
	HashMap hints = new HashMap();
	hints.put (PNGTranscoder.KEY_INDEXED, new Integer(8));
	transcoder.setTranscodingHints(hints);
	*/
	
	StringReader r = new StringReader(svg);
	TranscoderInput input = new TranscoderInput (r);
	TranscoderOutput output = new TranscoderOutput(out);
	transcoder.transcode(input, output);
	
	}
	
	
}
