package edu.usc.workload;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.usc.distributions.GeneralizedParetoDistribution;

/**
 * The generator models the inter-arrival time distributions based on statistics
 * published by Facebook.
 * 
 * Berk Atikoglu, Yuehai Xu, Eitan Frachtenberg, Song Jiang, and Mike Paleczny.
 * 2012. Workload analysis of a large-scale key-value store. In Proceedings of
 * the 12th ACM SIGMETRICS/PERFORMANCE joint international conference on
 * Measurement and Modeling of Computer Systems (SIGMETRICS '12). ACM, New York,
 * NY, USA, 53-64. DOI=http://dx.doi.org/10.1145/2254756.2254766
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class InterarrivalGapGenerator {

	private final List<GeneralizedParetoDistribution> interarrivalDist = new ArrayList<>();
	private final List<double[]> interarrivalCDF = new ArrayList<>();
	private final List<InterArrivalDistConfig> interarrivalConfigs = new ArrayList<>();
	private final GeneralizedParetoDistribution interarrivalDailyDist;
	private final double[] interarrivalDailyCDF;
	private final Random rand;

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

	public InterarrivalGapGenerator(Random rand) {
		this.rand = rand;
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

	public int generateDailyInterarrivalTime() {
		return Utility.select(this.interarrivalDailyCDF, rand.nextDouble(), (int) this.interarrivalDailyDist.getMean(),
				this.interarrivalDailyCDF.length - 1);
	}

	public int mean(int hour) {
		return (int) this.interarrivalDist.get(hour).getMean();
	}

	public int mean() {
		return (int) this.interarrivalDailyDist.getMean();
	}

	public int generateInterarrivalTime(int hour) {
		return Utility.select(this.interarrivalCDF.get(hour), rand.nextDouble(),
				(int) this.interarrivalDist.get(hour).getMean(), this.interarrivalCDF.get(hour).length - 1);
	}

}
