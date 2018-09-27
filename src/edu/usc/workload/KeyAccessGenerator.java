package edu.usc.workload;

import java.util.Random;

import edu.usc.distributions.ZipfianGenerator;

/**
 * The key access generator is based on the Zipfian distribution. 
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class KeyAccessGenerator {

	private final ZipfianGenerator zipfian;

	public KeyAccessGenerator(Random rand, Configuration config, long items) {
		zipfian = new ZipfianGenerator(items, config.getZipf(), rand);
	}

	public long get() {
		return zipfian.nextValue();
	}
}
