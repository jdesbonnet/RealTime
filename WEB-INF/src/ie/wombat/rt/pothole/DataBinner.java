package ie.wombat.rt.pothole;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class DataBinner {

	public static void main (String[] arg) throws IOException {
		FileReader r = new FileReader(arg[0]);
		long binSize = Long.parseLong(arg[1]);
		List<float[]> ret = binner(r,binSize);
		System.err.println ("Have " + ret.size() + " binned records");
		for (float[] f : ret) {
			//System.out.println ("" + f[0] + " " + f[1] + " " + f[2]);
			System.out.println ("" + f[2] + " 0");
		}
	}
	
	public static List<float[]> binner(Reader r, long binSize) throws IOException {
		LineNumberReader lnr = new LineNumberReader(r);

		ArrayList<float[]> binnedRecords = new ArrayList<float[]>();
		String line;
		int binRecordCount = 0;
		int lineNumber = 0;
		long t, bin, prevBin=0;
		float[] sg = new float[3];

		while ((line = lnr.readLine()) != null) {

			lineNumber++;
			
			if (line.length() == 0) {
				continue;
			}

			String[] p = line.split(" ");

			String recType = p[0];
			t = Long.parseLong(p[1]);
			
			bin = t/binSize;
			
			if (bin != prevBin) {
				//System.err.print ("B");
				if (binRecordCount > 0) {
					sg[0] /= binRecordCount;
					sg[1] /= binRecordCount;
					sg[2] /= binRecordCount;
					binnedRecords.add(sg);
					sg = new float[3];
					binRecordCount = 0;
				}
				prevBin = bin;
			}

			if ("G".equals(recType)) {
				if (lineNumber%1000==0) {
					System.err.print(".");
				}
				if (p.length < 5) {
					continue;
				}
				sg[0] += Float.parseFloat(p[2]);
				sg[1] += Float.parseFloat(p[3]);
				sg[2] += Float.parseFloat(p[4]);
				binRecordCount++;
			}

		}
		return binnedRecords;

	}
}
