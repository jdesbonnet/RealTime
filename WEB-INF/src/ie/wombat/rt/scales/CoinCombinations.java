package ie.wombat.rt.scales;

public class CoinCombinations {
	private static final int N = 32768;
	
	public static void main (String[] arg) {
		
		int i;
		int[] w = new int[N];
		int[] v = new int[N];
		for (i = 0; i < N; i++) {
			w[i] = (i&0x1f) * 410
				+ ( (i>>5)&0x1f ) * 574
				+ ( (i>>10)&0x1f ) * 780
			;
			v[i] = (i&0x1f) * 10
				+ ( (i>>5)&0x1f ) * 20
				+ ( (i>>10)&0x1f ) * 50
			;
			//System.out.println (i + " " + w + " " + v);
		}
		
		int W = Integer.parseInt(arg[0]);
		
		for (i = 0; i < N; i++) {
			if (w[i] > W-5 && w[i] < W+5) {
				System.out.println (i + " " + w[i] + " " + v[i]);
			}
		}
	}
}
