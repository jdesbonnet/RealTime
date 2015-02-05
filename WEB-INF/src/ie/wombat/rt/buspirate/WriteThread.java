package ie.wombat.rt.buspirate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class WriteThread implements Runnable {

	private static final String I2C_SENTENCE = "dbacdcabacdc ababababababababababab acdcdcababacdb acdcabacdcabababab acdcabababababababababacdcabd";
	private PrintWriter w;

	public WriteThread(OutputStream out) {
		this.w = new PrintWriter(out);
	}

	public void run() {
		try {
			
			// ESC to reset
			w.write ("\033");
			Thread.sleep(500);
			
			// I2C snoop (asm) mode
			w.write ("D\r");
			Thread.sleep(500);
			
			int n = I2C_SENTENCE.length();
			for (int i = 0; i < n; i++) {
				char c = I2C_SENTENCE.charAt(i);
				if (c == ' ') {
					continue;
				}
				//System.err.print(c);
				//System.err.flush();
				w.write(c);
				w.flush();
				Thread.sleep(100);
				/*
				w.write("L1\r");
				w.flush();
				Thread.sleep(500);
				
				w.write("L0\r");
				w.flush();
				Thread.sleep(1000);
				*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
