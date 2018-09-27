package edu.usc.cache;

import java.util.Optional;

import edu.usc.hoagie.WorkloadGenerator;
import edu.usc.workload.Configuration;

/**
 * An example main to evaluate an LRU cache with 1G cache size.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class LRUCacheMain {

	public static void main(String[] args) throws Exception {
		String configFile = args[0];
		long cacheSizeMB = 1024;
		long cacheSize = cacheSizeMB * 1024;

		Configuration config = new Configuration(configFile);
		System.out.println("Start Hoagie with configuration " + config);

		System.out.println("seconds,misses,reads,num-entries-in-cache,evictions,miss-ratio,evict-miss-ratio");
		StatsCache client = new StatsCache(new LRUCache(0, cacheSize));

		WorkloadGenerator gen = new WorkloadGenerator(config, client, Optional.empty());
		gen.run();
		gen.close();
	}
}
