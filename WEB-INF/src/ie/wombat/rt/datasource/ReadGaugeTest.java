package ie.wombat.rt.datasource;

import java.util.Calendar;

import org.apache.log4j.BasicConfigurator;

import junit.framework.TestCase;

public class ReadGaugeTest extends TestCase {

	public void setUp () {
		BasicConfigurator.configure();
	}

	public void testReadGauge() {
		ReadGauge g = new ReadGauge();
		g.readGauge("gal1", Calendar.getInstance().getTime());
	}

}
