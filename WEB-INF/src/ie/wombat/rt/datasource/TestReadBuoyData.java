package ie.wombat.rt.datasource;

public class TestReadBuoyData {

	public void testAll () throws Exception {
		ReadBuoyData.pollServer("http://localhost/joe/Observations.html");
	}
}
