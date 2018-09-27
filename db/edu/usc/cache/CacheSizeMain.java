package edu.usc.cache;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.usc.hoagie.WorkloadGenerator;
import edu.usc.workload.Configuration;

/**
 * The program used to generate cache miss ratio curves of many cache sizes.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class CacheSizeMain {

	public static void main(String[] args) throws Exception {
		String configFile = args[0];
		String dir = args[1];
		simulate(dir, configFile);
	}

	private static void simulate(String dir, String configFile) throws Exception {
		Map<String, List<CacheSizeStats>> statsOutput = new HashMap<>();
		List<Long> simulateCacheSizes = new ArrayList<>();
		for (long i = 16; i <= 4096;) {
			simulateCacheSizes.add(i);
			i *= 2;
		}

		System.out.println("Simulated cache sizes " + simulateCacheSizes);
		{
			Configuration config = new Configuration(configFile);
			List<CacheSizeStats> stats = new ArrayList<>();
			run(simulateCacheSizes, dir, "fb", config, stats);
			statsOutput.put("fb", stats);
		}
		output(statsOutput, simulateCacheSizes);
	}

	private static void output(Map<String, List<CacheSizeStats>> statss, List<Long> simulateCacheSizes) {
		StringBuilder out = new StringBuilder();
		for (long size : simulateCacheSizes) {
			out.append("," + size);
		}
		System.out.println(out.toString());

		Function<CacheSizeStats, String> evictRatio = new Function<CacheSizeStats, String>() {
			@Override
			public String apply(CacheSizeStats t) {
				return String.valueOf(t.evictRatio);
			}
		};

		Function<CacheSizeStats, String> missRatio = new Function<CacheSizeStats, String>() {
			@Override
			public String apply(CacheSizeStats t) {
				return String.valueOf(t.missRatio);
			}
		};
		Function<CacheSizeStats, String> reads = new Function<CacheSizeStats, String>() {
			@Override
			public String apply(CacheSizeStats t) {
				return String.valueOf(t.read);
			}
		};
		System.out.println("miss ratio curve");
		output(statss, missRatio);
		System.out.println("evict miss ratio curve");
		output(statss, evictRatio);
		System.out.println("reads");
		output(statss, reads);
	}

	private static void output(Map<String, List<CacheSizeStats>> statss, Function<CacheSizeStats, String> metric) {
		statss.forEach((config, miss) -> {
			StringBuilder builder = new StringBuilder();
			builder.append(config);
			builder.append(",");
			for (CacheSizeStats d : miss) {
				builder.append(metric.apply(d));
				builder.append(",");
			}
			System.out.println(builder.toString());
		});
	}

	private static void output(List<Double> list) {
		StringBuilder builder = new StringBuilder();
		for (Double d : list) {
			builder.append(d);
			builder.append(",");
		}
		System.out.println(builder.toString());
	}

	public static class CacheSizeStats {
		double missRatio;
		double evictRatio;
		BigDecimal read = new BigDecimal("0");
		List<Double> reads = new ArrayList<>();
		List<Double> missRatios = new ArrayList<>();
		List<Double> evictMissRatios = new ArrayList<>();

		@Override
		public String toString() {
			return "CacheSizeStats [missRatio=" + missRatio + ", evictRatio=" + evictRatio + ", read=" + read
					+ ", reads=" + reads + ", missRatios=" + missRatios + ", evictMissRatios=" + evictMissRatios + "]";
		}

	}

	public static void run(List<Long> simulateCacheSizes, String dir, String fileName, Configuration config,
			List<CacheSizeStats> stats) {

		System.out.println("Running " + fileName + " now");

		final Map<Long, CacheSizeStats> cacheSizeStats = new ConcurrentHashMap<>();

		for (long cacheSizeMB : simulateCacheSizes) {
			long cacheSize = cacheSizeMB * 1024 * 1024;
			PartitionedCache client = new PartitionedCache(1, config.getCacheType(), cacheSize);
			System.out.println("Running with cache size MB " + cacheSizeMB);
			String filename = fileName + "-cacheMB-" + cacheSizeMB;
			File file = new File(dir + "/" + filename);
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}

			WorkloadGenerator gen = new WorkloadGenerator(config, client, Optional.of(file));
			gen.run();
			gen.close();

			CacheSizeStats cacheStats = new CacheSizeStats();
			cacheSizeStats.put(cacheSizeMB, cacheStats);
			cacheStats.missRatio = client.getTotalMisses().divide(client.getTotalReads(), 4, RoundingMode.HALF_UP)
					.doubleValue();
			cacheStats.evictRatio = client.getTotalEvictions().divide(client.getTotalMisses(), 4, RoundingMode.HALF_UP)
					.doubleValue();
			cacheStats.read = client.getTotalReads();

			for (int i = 0; i < 1; i++) {
				cacheStats.missRatios.add(client.getTotalMisses(i)
						.divide(client.getTotalReads(i), 4, RoundingMode.HALF_UP).doubleValue());
				cacheStats.evictMissRatios.add(client.getTotalEvictions(i)
						.divide(client.getTotalMisses(i), 4, RoundingMode.HALF_UP).doubleValue());
				cacheStats.reads.add(
						client.getTotalReads(i).divide(client.getTotalReads(), 4, RoundingMode.HALF_UP).doubleValue());
			}
			System.out.println(cacheSizeMB + " MB," + stats);
		}

		for (long cacheSizeMB : simulateCacheSizes) {
			CacheSizeStats csStats = cacheSizeStats.get(cacheSizeMB);
			stats.add(csStats);
		}
		output(stats.stream().map(i -> i.missRatio).collect(Collectors.toList()));
		output(stats.stream().map(i -> i.evictRatio).collect(Collectors.toList()));
	}

}
