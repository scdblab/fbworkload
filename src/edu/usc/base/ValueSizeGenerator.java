package edu.usc.base;

import java.util.Random;

import edu.usc.distributions.GeneralizedParetoDistribution;

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
		return Utility.select(this.valueSizeCDF, rand.nextDouble(), (int) this.valueSizeDist.getMean(),
				this.valueSizeCDF.length - 1);
	}

	public GeneralizedParetoDistribution getValueSizeDist() {
		return valueSizeDist;
	}

}
