package edu.usc.facebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.usc.distributions.GeneralizedExtremeValueDistribution;
import edu.usc.distributions.GeneralizedParetoDistribution;

public class FBWorkloads {

	private final GeneralizedExtremeValueDistribution keySizeDist;
	private final double[] keySizeCDF;

	private final GeneralizedParetoDistribution valueSizeDist;
	private final double[] valueSizeCDF;

	private final List<GeneralizedParetoDistribution> interarrivalDist = new ArrayList<>();
	private final List<double[]> interarrivalCDF = new ArrayList<>();
	private final List<InterArrivalDistConfig> interarrivalConfigs = new ArrayList<>();
	private final Random rand;

	private final GeneralizedParetoDistribution interarrivalDailyDist;
	private final double[] interarrivalDailyCDF;

	private static final class InterArrivalDistConfig {
		private final double loc;
		private final double scale;
		private final double shape;

		public InterArrivalDistConfig(double loc, double scale, double shape) {
			super();
			this.loc = loc;
			this.scale = scale;
			this.shape = shape;
		}

		public GeneralizedParetoDistribution getDist() {
			return new GeneralizedParetoDistribution(loc, scale, shape);
		}

	}

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

			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 16.2868, 0.155280));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.8937, 0.141368));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.6345, 0.137579));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.7003, 0.142382));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 16.3231, 0.160706));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 17.5157, 0.181278));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 18.6748, 0.196885));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 19.5114, 0.202396));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 20.2050, 0.201637));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 20.2915, 0.193764));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 19.5577, 0.178386));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 18.2294, 0.161636));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 17.1879, 0.140461));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 16.2159, 0.119242));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.6716, 0.104535));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.2904, 0.094286));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.2033, 0.096963));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 14.9533, 0.098510));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.1381, 0.096155));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.3210, 0.094156));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.3848, 0.100365));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 15.7502, 0.111921));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 16.0205, 0.131946));
			this.interarrivalConfigs.add(new InterArrivalDistConfig(0, 16.3238, 0.147258));

			// 1 millisecond
			int maxSize = 1001;
			for (int i = 0; i < this.interarrivalConfigs.size(); i++) {
				this.interarrivalDist.add(this.interarrivalConfigs.get(i).getDist());
				double[] cdf = new double[maxSize];
				for (int j = 1; j < maxSize; j++) {
					cdf[j] = this.interarrivalDist.get(i).getCDF(j);
				}
				this.interarrivalCDF.add(cdf);
			}

			double loc = 0;
			double scale = 16.0292;
			double shape = 0.154971;
			this.interarrivalDailyDist = new GeneralizedParetoDistribution(loc, scale, shape);
			this.interarrivalDailyCDF = new double[maxSize];
			for (int j = 1; j < maxSize; j++) {
				this.interarrivalDailyCDF[j] = this.interarrivalDailyDist.getCDF(j);
			}
		}

		System.out.println(String.format("Mean: key size: %.2f, value size: %.2f", keySizeDist.getMean(),
				valueSizeDist.getMean()));
		for (int i = 0; i < this.interarrivalDist.size(); i++) {
			System.out.println(String.format("Mean inter-arrival time at %d hour: %.2f, %d requests/s", i,
					this.interarrivalDist.get(i).getMean(),
					(int) (1000 * 1000 / this.interarrivalDist.get(i).getMean())));
		}

		// print(keySizeCDF);
		print(valueSizeCDF);
		// print(this.interarrivalDailyCDF);
	}

	public static void print(double[] dist) {
		for (int i = 0; i < Math.min(dist.length, 1000); i++) {
			System.out.println(i + " : " + dist[i] * 100);
		}
		for (int i = 1000; i < 10000; i += 1000) {
			System.out.println(i + " : " + dist[i] * 100);
		}
		for (int i = 10000; i < 100000; i += 10000) {
			System.out.println(i + " : " + dist[i] * 100);
		}
		for (int i = 100000; i <= 1000000; i += 100000) {
			System.out.println(i + " : " + dist[i] * 100);
		}
		System.out.println();
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

	public int generateDailyInterarrivalTime() {
		return value(this.interarrivalDailyCDF, rand.nextDouble(), (int) this.interarrivalDailyDist.getMean(),
				this.interarrivalDailyCDF.length - 1);
	}

	public int generateInterarrivalTime(int hour) {
		return value(this.interarrivalCDF.get(hour), rand.nextDouble(), (int) this.interarrivalDist.get(hour).getMean(),
				this.interarrivalCDF.get(hour).length - 1);
	}
}
