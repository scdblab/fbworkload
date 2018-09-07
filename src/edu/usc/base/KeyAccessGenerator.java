package edu.usc.base;

import java.util.Random;

import edu.usc.distributions.ZipfianGenerator;

public class KeyAccessGenerator {

	private final ZipfianGenerator zipfian;

	public KeyAccessGenerator(Random rand, long items) {
		zipfian = new ZipfianGenerator(items, rand);
	}

	public long get() {
		return zipfian.nextValue();
	}
}
