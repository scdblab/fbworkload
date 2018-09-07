package edu.usc.base;

import java.util.Arrays;

public class Utility {
	public static int select(double[] data, double cdf, int mean, int max) {
		int index = Arrays.binarySearch(data, cdf);
		if (index >= 0)
			return index;
		int insert = -(index + 1);
		if (insert > max) {
			return mean;
		}
		return -(index + 1);
	}
}
