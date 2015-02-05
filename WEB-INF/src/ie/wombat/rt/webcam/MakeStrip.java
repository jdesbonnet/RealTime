package ie.wombat.rt.webcam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;



public class MakeStrip {

	public static void main (String[] arg) throws IOException {
		File dir = new File(arg[0]);
		File[] files = dir.listFiles();
		
		BufferedImage image = makeStrip(files);
		
		ImageIO.write(image, "jpeg", new File ("/var/tmp/t.jpg"));
		
	}
	
	public static BufferedImage makeStrip (File[] files) throws IOException {
	
		//List<ImageId> images = store.query(date);
		
		Arrays.sort(files);
		
		File firstImageFile = files[0];
		
		BufferedImage firstImage = ImageIO.read(firstImageFile);
		int width = firstImage.getWidth();
		int height = firstImage.getHeight();
		
		
		
		BufferedImage strip = new BufferedImage (files.length,height, BufferedImage.TYPE_INT_RGB);
		
		int i,j,x,y,c;
		for (i = 0; i < files.length; i++) {
			System.err.println (files[i].getPath());
			BufferedImage image = ImageIO.read(files[i]);
			//x = (image.getWidth() * i)/ files.length;
			x = width/2;
			for (j = 0; j < height; j++) {
				c = image.getRGB(x,j);
				strip.setRGB(i, j, c);
			}
		}
		return strip;
	}
}
