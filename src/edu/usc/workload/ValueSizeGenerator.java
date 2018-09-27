package edu.usc.workload;

import java.util.Random;

import edu.usc.distributions.GeneralizedParetoDistribution;

/**
 * The generator models the value size distribution based on statistics
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
public class ValueSizeGenerator {
	private final GeneralizedParetoDistribution valueSizeDist;
	private final double[] valueSizeCDF;
	private final Random rand;

	public ValueSizeGenerator(Random rand) {
		this.rand = rand;
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
	}

	public int get() {
		int val = Utility.select(this.valueSizeCDF, rand.nextDouble(), (int) this.valueSizeDist.getMean(),
				this.valueSizeCDF.length - 1);
		return val;
	}

	public GeneralizedParetoDistribution getValueSizeDist() {
		return valueSizeDist;
	}

}
