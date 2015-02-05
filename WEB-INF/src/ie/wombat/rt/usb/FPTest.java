package ie.wombat.rt.usb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FPTest {

	public static void main (String[] arg) throws IOException {
		Float f = new Float(arg[0]);
		
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		
		ObjectOutputStream oos = new ObjectOutputStream (baout);
		oos.writeFloat (0f);
		oos.writeFloat (f.floatValue());
		oos.flush();
		
		byte[] ba = baout.toByteArray();
		for (int i = 0; i < ba.length; i++) {
			USBMonParser.printByte((int)ba[i]);
			System.out.print(" ");
		}
		System.out.println("");
		
	}
}
