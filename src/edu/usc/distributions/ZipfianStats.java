package edu.usc.distributions;

/**
 * https://en.wikipedia.org/wiki/Zipf%27s_law
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class ZipfianStats {
	private final int alpha;
	private final int items;

	public ZipfianStats(int alpha, int items) {
		super();
		this.alpha = alpha;
		this.items = items;
	}

	public double mean() {
		double sum2 = 0;
		double sum1 = 0;
		for (double i = 1; i <= items; i++) {
			sum2 += Math.pow(1 / i, alpha);
			sum1 += Math.pow(1 / i, alpha - 1);
		}
		return sum1 / sum2;
	}
}
