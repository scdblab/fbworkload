package edu.usc.facebook;

import java.util.Arrays;
import java.util.Random;

public class FBWorkloads {

	private final GeneralizedExtremeValueDistribution keySizeDist;
	private final double[] keySizeCDF;

	private final GeneralizedParetoDistribution valueSizeDist;
	private final double[] valueSizeCDF;

	private final GeneralizedParetoDistribution interarrivalDist;
	private final double[] interarrivalCDF;
	private final Random rand;

	public FBWorkloads(Random rand) {
		this.rand = rand;
		{
			double loc = 30.7984;
			double scale = 8.20449;
			double shape = 0.078688;
			this.keySizeDist = new GeneralizedExtremeValueDistribution(loc, scale, shape);
			int maxKeySize = 251;
			this.keySizeCDF = new double[maxKeySize];
			for (int i = 1; i < maxKeySize; i++) {
				this.keySizeCDF[i] = this.keySizeDist.getCDF(i);
			}
		}

		{
			double loc = 0;
			double scale = 214.476;
			double shape = 0.348238;
			this.valueSizeDist = new GeneralizedParetoDistribution(loc, scale, shape);
			// 1 MB.
			int maxSize = 1000001;
			this.valueSizeCDF = new double[maxSize];
			for (int i = 1; i < maxSize; i++) {
				this.valueSizeCDF[i] = this.valueSizeDist.getCDF(i);
			}
			// sizeCDF[0] = 0.00536;
			// sizeCDF[1] = 0.00047;
			// sizeCDF[2] = 0.17820;
			// sizeCDF[3] = 0.09239;
			// sizeCDF[4] = 0.00018;
			// sizeCDF[5] = 0.02740;
			// sizeCDF[6] = 0.00065;
			// sizeCDF[7] = 0.00606;
			// sizeCDF[8] = 0.00023;
			// sizeCDF[9] = 0.00837;
			// sizeCDF[10] = 0.00837;
			// sizeCDF[11] = 0.08989;
			// sizeCDF[12] = 0.00092;
			// sizeCDF[13] = 0.00326;
			// sizeCDF[14] = 0.01980;
		}

		{
			double loc = 0;
			double scale = 16.0292;
			double shape = 0.154971;

			this.interarrivalDist = new GeneralizedParetoDistribution(loc, scale, shape);
			// 1 millisecond
			int maxSize = 1001;
			this.interarrivalCDF = new double[maxSize];
			for (int i = 1; i < maxSize; i++) {
				this.interarrivalCDF[i] = this.interarrivalDist.getCDF(i);
			}
		}

		System.out.println(String.format("Mean: key size: %.2f, value size: %.2f, inter-arrival time: %.2f",
				keySizeDist.getMean(), valueSizeDist.getMean(), interarrivalDist.getMean()));
	}

	public static void print(double[] dist) {
		for (int i = 0; i < Math.min(dist.length, 1000); i++) {
			System.out.println(i + " : " + dist[i]);
		}
	}

	public int value(double[] data, double cdf, int mean, int max) {
		int index = Arrays.binarySearch(data, cdf);
		if (index >= 0)
			return index;
		int insert = -(index + 1);
		if (insert > max) {
			return mean;
		}
		return -(index + 1);
	}

	public int getMeanRequestSize() {
		return (int) (this.keySizeDist.getMean() + this.valueSizeDist.getMean());
	}

	public int generateKeySize() {
		return value(this.keySizeCDF, rand.nextDouble(), (int) this.keySizeDist.getMean(), this.keySizeCDF.length - 1);
	}

	public int generateValueSize() {
		return value(this.valueSizeCDF, rand.nextDouble(), (int) this.valueSizeDist.getMean(),
				this.valueSizeCDF.length - 1);
	}

	public int generateInterarrivalTime() {
		return value(this.interarrivalCDF, rand.nextDouble(), (int) this.interarrivalDist.getMean(),
				this.interarrivalCDF.length - 1);
	}
}
