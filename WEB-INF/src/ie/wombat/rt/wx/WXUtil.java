package ie.wombat.rt.wx;

public class WXUtil {

	/**
	 * Fahrenheit to Celcius
	 * @param f
	 * @return
	 */
	public static final float f2c (float f) {
		return (f - 32.0f) * 5.0f/9.0f;
	}
	public final static Float f2c (Float f) {
		return new Float(f2c(f.floatValue()));
	}
	
	/**
	 * Pressure in inches of mercury to millibars
	 * @param inHg
	 * @return
	 */
	public static final float inHg2mb (float inHg) {
		return  inHg * 25.4f * 1.33322f;
	}
	
	public static final Float inHg2mb (Float inHg) {
		return new Float(inHg2mb(inHg.floatValue()));
	}
	
	/**
	 * Knots to km/h
	 * @param kn
	 * @return
	 */
	public static final float kn2kmph (float kn) {
		//return kn * 1.609344f;
		return kn * 1.852f;
	}
	public static final Float kn2kmph (Float kn) {
		return new Float (kn2kmph(kn.floatValue()));
	}
	
	public static final float kmph2kn (float kmph) {
		return kmph / 1.852f;
	}
}
