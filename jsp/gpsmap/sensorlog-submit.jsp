
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.FileReader"%><%@page import="org.jfree.io.IOUtils"%><%@include file="_header.jsp"%><%
File sensorLogFile = new File ("/var/tmp/sensorlog.dat");
java.io.OutputStream fout = new java.io.FileOutputStream(sensorLogFile);
java.io.InputStream in = request.getInputStream();
IOUtils.getInstance().copyStreams(in,fout);
fout.close();

// Process sensor log to get change in orientation (movement) vs time
FileReader r = new FileReader(sensorLogFile);
LineNumberReader lnr = new LineNumberReader(r);

int i = 0;
float[] po = new float[3];
float[] o = new float[3];
float[] deltao = new float[3];

double d;
float d2;
long t=0;

String line = lnr.readLine();
String[] p = line.split(" ");


// Get first record
for (i = 0; i < 3; i++) {
	po[i] = Float.parseFloat(p[i+1]);
}
long t0 = Long.parseLong(p[0]);

File movementFile = new File ("/var/tmp/movement.dat");
FileWriter w = new FileWriter(movementFile);
while (  (line=lnr.readLine()) != null) {
	p = line.split(" ");
	t = Long.parseLong(p[0]);
	d2 = 0;
	for (i = 0; i < 3; i++) {
		o[i] = Float.parseFloat(p[i+1]);
		d2 += (o[i] - po[i]) * (o[i] - po[i]);
		po[i] = o[i];
	}
	d = Math.sqrt((double)d2);
	//if ( d > 3.5) {
		//w.write ("" +  ((double)(t - t0) / 3600000)  );
		w.write ("" +  t   );
		w.write (" " +  d);
		w.write ("\n");
	//}
}
w.close();

long binSize = 600000;
long timeSpan = t - t0;
int nBin = (int)(timeSpan/binSize) + 2;
float[] binSum = new float[nBin];
int[] binCount = new int[nBin];

r = new FileReader (new File("/var/tmp/movement.dat"));
lnr = new LineNumberReader(r);

float m;
while (  (line=lnr.readLine()) != null) {
	p = line.split(" ");
	t = Long.parseLong(p[0]);
	m = Float.parseFloat(p[1]);
	i = (int) (t-t0) / (int)binSize ;
	System.err.println ("bin " + i + " += " + m);
	binSum[i] += m;
	binCount[i]++;
}

w = new FileWriter(new File("/var/tmp/movementav.dat"));
for (i = 0; i < nBin; i++) {
	if (binCount[i] > 0) {
		w.write ("" +  (t0 + i*binSize)  );
		w.write (" " +  (binSum[i]/ (float)binCount[i])  );
		w.write ("\n");
	}
}
w.close();
%>
