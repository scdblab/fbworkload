package edu.usc.facebook;

import java.util.Random;

import edu.usc.distributions.ZipfianGenerator;

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

	public static void main(String[] args) {
		// int items = 1000000;
		// for (int alpha = 1; alpha <= 10; alpha++) {
		// System.out.println(
		// String.format("%d:%.2f", alpha, (double) new ZipfianStats(alpha,
		// items).mean() / (double) items));
		//
		// }
		int items = 100000;
		for (double constant = 0.9; constant <= 0.99; constant += 0.01) {
			System.out.println("####" + constant);
			int[] reqs = new int[10];
			ZipfianGenerator gen = new ZipfianGenerator(items, constant, new Random());
			int total = 10000000;
			for (int i = 0; i < total; i++) {
				long val = gen.nextValue().longValue();
				reqs[(int) Math.log10(val + 1)]++;
			}
			double sum = 0;
			for (int i = 0; i < reqs.length; i++) {
				sum += (double) reqs[i] / (double) total;
				System.out.println((int) Math.pow(10, i + 1) + "," + sum * 100);
			}

		}
	}

}
