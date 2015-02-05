package ie.wombat.rt.scales;

import java.awt.Canvas;


public class ScaleBar extends Canvas {

	public ScaleBar () {
		//super (new FlowLayout());
	
		setSize(600, 50);
		
		
		getGraphics().drawRect(0,0,10,10);
	}
}
