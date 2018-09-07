package edu.usc.distributions;

/**
 * https://en.wikipedia.org/wiki/Generalized_Pareto_distribution
 * 
 * @author haoyuh
 */
public class GeneralizedParetoDistribution extends Distribution {
	private final double loc;
	private final double scale;
	private final double shape;

	public GeneralizedParetoDistribution(double loc, double scale, double shape) {
		super();
		this.loc = loc;
		this.scale = scale;
		this.shape = shape;
	}

	public double getCDF(double x) {
		double z = (x - loc) / scale;
		return 1 - Math.pow(1 + shape * z, -(1 / shape));
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
		return loc + (scale / (1 - shape));
	}

}
