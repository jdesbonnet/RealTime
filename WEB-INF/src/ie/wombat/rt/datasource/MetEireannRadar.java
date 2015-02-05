package ie.wombat.rt.datasource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.ImageIO;

/**
 * Class to analyse precipitation data from Met Éireann radar chart 
 * currently located at http://www.met.ie/weathermaps/latest_radar.gif
 * 
 * @author joe
 *
 */
public class MetEireannRadar {
	
	
	// The top left corner of Met Éireann radar chart
	private static final float LAT_TL = 55.51f;
	private static final float LON_TL = -12.46f;
	
	// Approx scale of Met Éireann radar chart
	private static final float LAT_PX_PER_DEG = 74.7f;
	private static final float LON_PX_PER_DEG = 42.9f;
	
	// Pixels per grid element
	private static final int SQ_SIZE = 12;
	
	
	// Define colors for various intensity of precipitation
	private static final int MASK=0xff808080;
	private static final int LIGHT_RAIN = 0xff0000c8 & MASK;
	private static final int MODERATE_RAIN= 0xffc8fe00 & MASK;
	private static final int HEAVY_RAIN=0xffe07bc8 & MASK;
	private static final int VHEAVY_RAIN=0xfffe0000 & MASK;
	
	private static final int LIGHT_RAIN_NEW = 0x808080ff;
	private static final int MODERATE_RAIN_NEW = 0x806060ff;
	private static final int HEAVY_RAIN_NEW = 0x804040ff;
	private static final int VHEAVY_RAIN_NEW = 0x802020ff;
	private static final int GLASS = 0x00000000;
	
	public static void main (String[] arg) throws Exception {
		File imageFile = new File(arg[0]);
		File imageOutFile = new File(arg[1]);
		
		
		
		
		//getIconMatrix(img, new PrintWriter(System.out));
		
		
		//BufferedImage imageOut = extractRainFromRadarMap(img);
		
		//ImageIO.write(imageOut, "PNG", imageOutFile);
		
		
		
	}
	
	/**
	 * <p>
	 * Analyses a Met Éireann radar chart and produces a radar element.
	 * </p>
	 * 
	 * <p>
	 * The radar element comprises the following attributes: lat0: the latitude of the
	 * top left of the grid; lon0: the longitude of the top left of the data area;
	 * dlat: the 'width' in degrees of one grid element; dlon: the 'height' in degrees of one
	 * grid element; rows: the number of rows in the data; cols: the number of columns in the data;
	 * data: the radar data from left to right, top to bottom, one digit per grid element (0: no rainfall, 
	 * 8: very, very heavy rainfall).
	 * </p>
	 * 
	 * @param radarImageFileName
	 * @param out
	 * @throws IOException
	 */
	public static void writeRadarElement (String radarImageFileName, Writer out)
		throws IOException  {
		
		File radarImageFile = new File(radarImageFileName);
		
		if (! radarImageFile.exists() ) {
			return;
		}
		
		BufferedImage imageIn = ImageIO.read(radarImageFile);
		
		int w = imageIn.getWidth();
		int h = imageIn.getHeight();
		
		int maxj = h / SQ_SIZE;
		int maxi = w / SQ_SIZE;
		int[][] ra = new int[maxi+1][maxj+1];
		
		int i,j,x,y,c,r;
		for (y = 0; y < h; y+=1) {
			j = y / SQ_SIZE;
			for (x = 0; x < w; x+=1) {
				
				// Ignore legend
				if (x < 20 && y < 80) {
					continue;
				}
				
				c = imageIn.getRGB(x,y) & MASK;
				i = x / SQ_SIZE;
				r = 0;
				switch (c) {
				case LIGHT_RAIN:
					r = 1;
					break;	
				case MODERATE_RAIN:
					r = 2;
					break;
					
				case HEAVY_RAIN:
					r = 3;
					break;
				case VHEAVY_RAIN:
					r = 4;
					break;
				}
				
				ra[i][j] += r;
			}
		
		}
		
		float s;
		int si;
		float lat = LAT_TL; 
		float dlat = SQ_SIZE / LAT_PX_PER_DEG;
		float lon = LON_TL; 
		float dlon = SQ_SIZE / LON_PX_PER_DEG;
		
		// Area in pixel of square
		float sqa = SQ_SIZE * SQ_SIZE;
		
		out.write ("<radar lat0=\"" 
				+ LAT_TL + "\" lon0=\"" 
				+ LON_TL
				+ "\" rows=\""
				+ (maxj + 1)
				+ "\" cols=\""
				+ (maxi + 1)
				+ "\" dlat=\""
				+ dlat
				+ "\" dlon=\""
				+ dlon
				+ "\" data=\"\n"
				);
		
		for (j = 0; j<maxj; j++) {
			lon = LON_TL;
			for (i = 0; i < maxi; i++) {
				
				// Mean value of square (0.0 - 4.0)
				s = (float)ra[i][j] / sqa;
				
				// Scale up from 0.0 - 8.0 and round to nearest integer
				si = (int)(s * 2 + 0.5f);
				out.write(""+si);
				lon += dlon;                                            
			}
			lat -= dlat;
			out.write ("\n");
		}
		out.write ("\" />");
	}
	
	/**
	 * Extracts precipitation data from Met Éireann radar chart
	 * @param imageIn
	 * @return
	 */
	private static BufferedImage extractRainFromRadarMap (BufferedImage imageIn) {
		
		int w = imageIn.getWidth();
		int h = imageIn.getHeight();
		
		//System.err.println ("w=" + w + " h=" + h);
		int x,y,c;
		for (y = 0; y < h; y+=1) {
			for (x = 0; x < w; x+=1) {
				
				// Ignore legend
				if (x < 20 && y < 80) {
					continue;
				}
				
				c = imageIn.getRGB(x,y) & MASK;
				if (c == LIGHT_RAIN) {
					imageIn.setRGB(x, y, LIGHT_RAIN_NEW);
					continue;
				}
				if (c == MODERATE_RAIN) {
					//System.err.print("M");
					imageIn.setRGB(x,y,MODERATE_RAIN_NEW);
					continue;
				}
				if (c == HEAVY_RAIN) {
					//System.err.print("H");
					imageIn.setRGB(x,y,HEAVY_RAIN_NEW);
					continue;
				}
				if (c == VHEAVY_RAIN) {
					//System.err.print("V");
					imageIn.setRGB(x,y,VHEAVY_RAIN_NEW);
					continue;
				}
				imageIn.setRGB (x,y,GLASS);
			
			}
		}
		return imageIn;
	}
}
