package ie.wombat.rt.witilt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConvertWiTilt {

	public static void main (String[] arg) throws IOException {
	
		InputStreamReader r = new InputStreamReader(new FileInputStream(arg[0]));
		WiTiltInputStream in = new WiTiltInputStream(r);
		
		int i = 0;
		double x,y,z;
		double sx=0,sy=0,sz=0,srz=0;
		
		WiTiltRecord record;
		while ( (record = in.getReading()) != null) {
			
			
			x = (double)record.x;
			y = (double)record.y;
			z = (double)record.z;
			
			sx += x;
			sy += y;
			sz += z;
			srz += record.rz;
			
			if (i%5 ==  4) {
				sx /= 5;
				sy /= 5;
				sz /= 5;
				
				System.out.print (i+"\t");
				
				System.out.print (sx + "\t");
				System.out.print (sy + "\t");
				System.out.print (sz + "\t");
			
				System.out.print (Math.sqrt(sx*sx + sy*sy + sz*sz) + "\t");
			
				System.out.print (record.rz + "\n");
				
				sx = sy = sz = srz = 0;
			}
			
			i++;
		}
	}
}
