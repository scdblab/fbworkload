package edu.usc.db;

import java.io.File;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.usc.base.Configuration;
import edu.usc.base.WorkingSet;
import edu.usc.facebook.WorkloadGenerator;

public class Main {

	static Map<String, List<Double>> output = new HashMap<>();

	public static void main(String[] args) throws Exception {

		// long workingSetSize = 19767631310l;
		// 18G.
		String dir = args[0];

		List<Long> simulateCacheSizes = new ArrayList<>();
		for (long i = 1; i <= 1024;) {
			simulateCacheSizes.add(i);
			i *= 2;
		}
		System.out.println("Simulated cache sizes " + simulateCacheSizes);
		long db = 0;
		{
			Configuration config = new Configuration("trace_specification.properties");
			List<Double> missRate = new ArrayList<>();
			db = run(simulateCacheSizes, dir, "fb", config, missRate);
			output.put("fb", missRate);
		}
		System.out.println("DB Size " + db);
		{
			Configuration config = new Configuration("trace_specification.properties");
			output.put("ycsb", new ArrayList<>());
			config.setEnableSize(false);
			config.setEnableLoadChange(false);
			config.setEnableTemporal(false);
			config.setEnableSpatial(false);
			List<Double> missRate = new ArrayList<>();
			run(simulateCacheSizes, dir, "ycsb", config, db, missRate);
			output.put("ycsb", missRate);
		}
		{
			Configuration config = new Configuration("trace_specification.properties");
			config.setEnableSize(true);
			config.setEnableLoadChange(false);
			config.setEnableTemporal(false);
			config.setEnableSpatial(false);
			List<Double> missRate = new ArrayList<>();
			run(simulateCacheSizes, dir, "ycsb+size", config, db, missRate);
			output.put("ycsb+size", missRate);
		}
		{
			Configuration config = new Configuration("trace_specification.properties");
			config.setEnableSize(true);
			config.setEnableLoadChange(true);
			config.setEnableTemporal(false);
			config.setEnableSpatial(false);
			List<Double> missRate = new ArrayList<>();
			run(simulateCacheSizes, dir, "ycsb+size+load", config, db, missRate);
			output.put("ycsb+size+load", missRate);
		}

		StringBuilder out = new StringBuilder();
		for (long size : simulateCacheSizes) {
			out.append("," + size);
		}
		System.out.println(out.toString());
		output.forEach((config, miss) -> {
			StringBuilder builder = new StringBuilder();
			builder.append(config);
			builder.append(",");
			for (double d : miss) {
				builder.append(d);
				builder.append(",");
			}
			System.out.println(builder.toString());
		});

	}

	public static long run(List<Long> simulateCacheSizes, String dir, String fileName, Configuration config,
			List<Double> missRatio) {
		long db = 0;
		ExecutorService executor = Executors.newFixedThreadPool(simulateCacheSizes.size());
		List<Future<Long>> list = new ArrayList<>();
		for (long cacheSizeMB : simulateCacheSizes) {
			final long cs = cacheSizeMB;
			list.add(executor.submit(new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					long cacheSize = cs * 1024 * 1024;
					Cache cache = new Cache(new LRUCache(0, cacheSize));

					String filename = fileName + "-cacheMB-" + cs;
					File file = new File(dir + "/" + filename);
					file.createNewFile();

					WorkloadGenerator gen = new WorkloadGenerator(config, cache, file);
					WorkingSet set = gen.run();
					gen.close();
					synchronized (missRatio) {
						missRatio.add(cache.getTotalMisses().divide(cache.getTotalReads(), 4, RoundingMode.HALF_UP)
								.doubleValue());
					}
					return set.getMinKeys().get(0) + set.getNumKeys().get(0);
				}
			}));
		}

		for (Future<Long> em : list) {
			try {
				db = em.get().longValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
		return db;
	}

	public static void run(List<Long> simulateCacheSizes, String dir, String fileName, Configuration config,
			long dbSize, List<Double> missRatio) {
		ExecutorService executor = Executors.newFixedThreadPool(simulateCacheSizes.size());
		List<Future<Void>> list = new ArrayList<>();
		for (long cacheSizeMB : simulateCacheSizes) {
			final long cs = cacheSizeMB;
			list.add(executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					long cacheSize = cs * 1024 * 1024;
					Cache cache = new Cache(new LRUCache(0, cacheSize));

					String filename = fileName + "-cacheMB-" + cs;
					File file = new File(dir + "/" + filename);
					file.createNewFile();

					WorkloadGenerator gen = new WorkloadGenerator(config, cache, file);
					gen.runYCSB(dbSize);
					gen.close();
					synchronized (missRatio) {
						missRatio.add(cache.getTotalMisses().divide(cache.getTotalReads(), 4, RoundingMode.HALF_UP)
								.doubleValue());
					}
					return null;
				}
			}));
		}

		for (Future<Void> em : list) {
			try {
				em.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}

}
