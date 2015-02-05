package ie.wombat.rt.usb;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.StringTokenizer;

/**
 * From usbmon.txt in Linux Kernel doc.
 * 
 *
 */
public class USBMonParser {
	
		//static int data_len;		/* Available length of data */
		//static byte data[];

		static void parseData(StringTokenizer st) {
			int availwords = st.countTokens();
			
			byte[] data = new byte[availwords * 4];
			int data_len = 0;
			
			while (st.hasMoreTokens()) {
				String data_str = st.nextToken();
				int len = data_str.length() / 2;
				int i;
				int b;	// byte is signed, apparently?! XXX
				for (i = 0; i < len; i++) {
					// data[data_len] = Byte.parseByte(
					//     data_str.substring(i*2, i*2 + 2),
					//     16);
					b = Integer.parseInt(
					     data_str.substring(i*2, i*2 + 2),
					     16);
					if (b >= 128)
						b *= -1;
					data[data_len] = (byte) b;
					System.err.println ("data[" + data_len + "]="
							+ Integer.toHexString((byte)b)
							+ " "
							+ (byte)b 
							+ " "
							+ (char)b
							);
					data_len++;
				}
			}
		}
		
		public static void main (String[] arg) throws Exception {
			File monFile = new File(arg[0]);
			Reader r = new FileReader(monFile);
			LineNumberReader lnr = new LineNumberReader(r);
			String line;
			int i,j,b;
			while ( ( line = lnr.readLine()) != null) {
				//StringTokenizer st = new StringTokenizer(line);
				//parseData(st);
				j = line.indexOf('=');
				if ( j < 0) {
					continue;
				}
				
				String dataHex = line.substring(j+2);
				dataHex = dataHex.replaceAll(" ", "");
				j = (dataHex.length()/2);
				//System.err.print(dataHex + " " + j + " bytes : ");
				for (i = 0; i < j; i++) {
					//if (dataHex.charAt(i) == ' ') {
						//continue;
					//}
					String hexByte = dataHex.substring(i*2,i*2+2);
					b = Integer.parseInt(hexByte, 16);
					printByte(b);
				}
				
				System.out.println ("");
			}
		}
		
		static void printByte (int b) {
			System.out.print (
					//( (b >=0 && b < 16) ? "0x0" : "0x")
					( (b >=0 && b < 16) ? "0" : "")
					+ Integer.toHexString(b&0xff)
					//+ " "
					//+ (byte)b 
					+ " ["
					+  ( (b > 32 && b < 128 ) ? (char)b : '?' )
					+ "], "
					);
		}
	}
	