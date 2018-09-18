package edu.usc.base;

import java.util.Random;

import edu.usc.distributions.ZipfianGenerator;

public class KeyAccessGenerator {

	private final ZipfianGenerator zipfian;

	public KeyAccessGenerator(Random rand, Configuration config, long items) {
		zipfian = new ZipfianGenerator(items, config.getZipf(), rand);
	}

	public long get() {
		return zipfian.nextValue();
	}
}
