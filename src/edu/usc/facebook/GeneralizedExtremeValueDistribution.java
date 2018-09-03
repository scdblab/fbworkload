package edu.usc.facebook;

/**
 * http://en.wikipedia.org/wiki/Generalized_extreme_value_distribution
 */
public class GeneralizedExtremeValueDistribution {
	public static double EulerConstant = 0.5772156649015328606065120900824024310421;

	private final double loc;
	private final double scale;
	private final double shape;

	public GeneralizedExtremeValueDistribution(double loc, double scale, double shape) {
		this.loc = loc;
		this.scale = scale;
		this.shape = shape;
	}

	public double getCDF(double x) {
		double cdfValue;
		double z = (x - loc) / scale;
		if (shape != 0) {
			cdfValue = Math.exp(-Math.pow((1 + shape * z), -1.0 / shape));
		} else {
			cdfValue = Math.exp(-Math.exp(-z));
		}
		return cdfValue;
	}

	public static double logGamma(double x) {
		double coef[] = { 76.18009173, -86.50532033, 24.01409822, -1.231739516, 0.00120858003, -0.00000536382 };
		double step = 2.50662827465, fpf = 5.5, t, tmp, ser;
		t = x - 1;
		tmp = t + fpf;
		tmp = (t + 0.5) * Math.log(tmp) - tmp;
		ser = 1;
		for (int i = 1; i <= 6; i++) {
			t = t + 1;
			ser = ser + coef[i - 1] / t;
		}
		return tmp + Math.log(step * ser);
	}

	public static double gamma(double x) {
		return Math.exp(logGamma(x));
	}

	public double getMean() {
		if (shape > 0 && shape < 1)
			return (loc + scale * (gamma(1 - shape) - 1) / shape);
		else if (shape == 0)
			return (loc + scale * EulerConstant);
		else
			return Double.POSITIVE_INFINITY;
	}

	public double getMedian() {
		if (shape != 0)
			return (loc + scale * (Math.pow(Math.log(2), -shape) - 1) / shape);
		return (loc - scale * Math.log(Math.log(2)));
	}
}
