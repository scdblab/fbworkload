package edu.usc.base;

import java.util.Random;

import edu.usc.distributions.GeneralizedExtremeValueDistribution;

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
