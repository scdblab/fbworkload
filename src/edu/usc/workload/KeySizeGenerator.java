package edu.usc.workload;

import java.util.Random;

import edu.usc.distributions.GeneralizedExtremeValueDistribution;

/**
 * The generator models the key size distribution based on statistics published
 * by Facebook.
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
public class KeySizeGenerator {

	private final GeneralizedExtremeValueDistribution keySizeDist;
	private final double[] keySizeCDF;
	private final Random rand;

	public KeySizeGenerator(Random rand) {
		this.rand = rand;
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

	public int get() {
		return Utility.select(this.keySizeCDF, rand.nextDouble(), (int) this.keySizeDist.getMean(),
				this.keySizeCDF.length - 1);
	}

	public GeneralizedExtremeValueDistribution getKeySizeDist() {
		return keySizeDist;
	}

}
