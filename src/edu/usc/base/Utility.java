package edu.usc.base;

import java.util.Arrays;

public class Utility {

	public static final long ONE_MB = 1024 * 1024;

	public static int select(double[] data, double cdf, int mean, int max) {
		int index = Arrays.binarySearch(data, cdf);
		if (index >= 0)
			return index;
		int insert = -(index + 1);
		if (insert > max) {
			return mean;
		}
		return insert;
	}

	public static int select(int[] data, int size) {
		int index = Arrays.binarySearch(data, size);
		if (index >= 0)
			return index;
		int insert = -(index + 1);
		if (insert == data.length) {
			throw new IllegalArgumentException();
		}
		return insert;
	}

	public static void CHECK(boolean condition, String message) {
		if (!condition) {
			System.out.println("Condition check failed: " + message);
			System.exit(0);
		}
	}
}
