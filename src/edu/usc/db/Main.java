package edu.usc.db;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import edu.usc.base.Configuration;
import edu.usc.base.WorkingSet;
import edu.usc.facebook.WorkloadGenerator;

public class Main {

	public static void main(String[] args) throws Exception {

		// long workingSetSize = 19767631310l;
		// 18G.
		String dir = args[0];
		String configFile = args[1];
		String[] pars = args[2].split(",");
		List<Integer> partitions = new ArrayList<>();
		for (String par : pars) {
			partitions.add(Integer.parseInt(par));
		}

		for (Integer partition : partitions) {
			simulate(dir, configFile, partition);
		}
	}

	private static void simulate(String dir, String configFile, int partition) throws Exception {
		Map<String, List<CacheSizeStats>> statsOutput = new HashMap<>();
		List<Long> simulateCacheSizes = new ArrayList<>();
		for (long i = 16; i <= 16384;) {
			simulateCacheSizes.add(i);
			i *= 4;
		}

		for (long cs : simulateCacheSizes) {
			if (cs / partition == 0) {
				throw new IllegalArgumentException();
			}
		}

		System.out.println("Simulated cache sizes " + simulateCacheSizes);
		long db = 0;
		{
			Configuration config = new Configuration(configFile);
			List<CacheSizeStats> stats = new ArrayList<>();
			db = run(simulateCacheSizes, dir, "fb", config, stats, partition);
			statsOutput.put("fb", stats);
		}
		System.out.println("DB Size " + db);
		{
			Configuration config = new Configuration(configFile);
			config.setEnableSize(false);
			config.setEnableLoadChange(false);
			config.setEnableTemporal(false);
			config.setEnableSpatial(false);
			List<CacheSizeStats> stats = new ArrayList<>();
			run(simulateCacheSizes, dir, "ycsb", config, db, stats, partition);
			statsOutput.put("ycsb", stats);
		}
		{
			Configuration config = new Configuration(configFile);
			config.setEnableSize(true);
			config.setEnableLoadChange(false);
			config.setEnableTemporal(false);
			config.setEnableSpatial(false);
			List<CacheSizeStats> stats = new ArrayList<>();
			run(simulateCacheSizes, dir, "ycsb+size", config, db, stats, partition);
			statsOutput.put("ycsb+size", stats);
		}
		// {
		// Configuration config = new Configuration(configFile);
		// config.setEnableSize(true);
		// config.setEnableLoadChange(true);
		// config.setEnableTemporal(false);
		// config.setEnableSpatial(false);
		// List<CacheSizeStats> stats = new ArrayList<>();
		// run(simulateCacheSizes, dir, "ycsb+size+load", config, db, stats, partition);
		// statsOutput.put("ycsb+size+load", stats);
		// }

		output(statsOutput, simulateCacheSizes, partition);
	}

	private static void output(Map<String, List<CacheSizeStats>> statss, List<Long> simulateCacheSizes,
			int partitions) {
		StringBuilder out = new StringBuilder();
		for (long size : simulateCacheSizes) {
			out.append("," + size);
		}
		System.out.println(out.toString());

		BiFunction<CacheSizeStats, Integer, String> evictRatio = new BiFunction<CacheSizeStats, Integer, String>() {
			@Override
			public String apply(CacheSizeStats t, Integer u) {
				if (u == -1) {
					return String.valueOf(t.evictRatio);
				}
				return String.valueOf(t.evictMissRatios.get(u));
			}
		};

		BiFunction<CacheSizeStats, Integer, String> missRatio = new BiFunction<CacheSizeStats, Integer, String>() {
			@Override
			public String apply(CacheSizeStats t, Integer u) {
				if (u == -1) {
					return String.valueOf(t.missRatio);
				}
				return String.valueOf(t.missRatios.get(u));
			}
		};
		BiFunction<CacheSizeStats, Integer, String> reads = new BiFunction<CacheSizeStats, Integer, String>() {
			@Override
			public String apply(CacheSizeStats t, Integer u) {
				if (u == -1) {
					return String.valueOf(t.read);
				}
				return String.valueOf(t.reads.get(u));
			}
		};
		System.out.println("miss ratio curve");
		output(statss, partitions, missRatio);
		System.out.println("evict miss ratio curve");
		output(statss, partitions, evictRatio);
		System.out.println("reads");
		output(statss, partitions, reads);
	}

	private static void output(Map<String, List<CacheSizeStats>> statss, int partitions,
			BiFunction<CacheSizeStats, Integer, String> metric) {
		statss.forEach((config, miss) -> {
			for (int p = -1; p < partitions; p++) {
				StringBuilder builder = new StringBuilder();
				builder.append(config);
				builder.append(",");
				builder.append("p" + p + ",");
				for (CacheSizeStats d : miss) {
					builder.append(metric.apply(d, p));
					builder.append(",");
				}
				System.out.println(builder.toString());
			}
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

	public static long run(List<Long> simulateCacheSizes, String dir, String fileName, Configuration config,
			List<CacheSizeStats> stats, int partitions) {

		System.out.println("Running " + fileName + " now");

		long db = 0;
		ExecutorService executor = Executors.newFixedThreadPool(simulateCacheSizes.size());
		List<Future<Long>> list = new ArrayList<>();
		final Map<Long, CacheSizeStats> cacheSizeStats = new ConcurrentHashMap<>();

		for (long cacheSizeMB : simulateCacheSizes) {
			final long cs = cacheSizeMB;

			list.add(executor.submit(new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					long cacheSize = cs * 1024 * 1024;
					PartitionedCache client = new PartitionedCache(partitions, config.getCacheType(),
							(cacheSize / partitions));
					System.out.println("Running with cache size MB " + cs);
					String filename = fileName + "-cacheMB-" + cs + "-partitions-" + partitions;
					File file = new File(dir + "/" + filename);
					file.createNewFile();

					WorkloadGenerator gen = new WorkloadGenerator(config, client, file);
					WorkingSet set = gen.run();
					gen.close();

					CacheSizeStats stats = new CacheSizeStats();
					cacheSizeStats.put(cs, stats);
					stats.missRatio = client.getTotalMisses().divide(client.getTotalReads(), 4, RoundingMode.HALF_UP)
							.doubleValue();
					stats.evictRatio = client.getTotalEvictions()
							.divide(client.getTotalMisses(), 4, RoundingMode.HALF_UP).doubleValue();
					stats.read = client.getTotalReads();

					for (int i = 0; i < partitions; i++) {
						stats.missRatios.add(client.getTotalMisses(i)
								.divide(client.getTotalReads(i), 4, RoundingMode.HALF_UP).doubleValue());
						stats.evictMissRatios.add(client.getTotalEvictions(i)
								.divide(client.getTotalMisses(i), 4, RoundingMode.HALF_UP).doubleValue());
						stats.reads.add(client.getTotalReads(i).divide(client.getTotalReads(), 4, RoundingMode.HALF_UP)
								.doubleValue());
					}
					System.out.println(cs + " MB," + stats);
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
		for (long cacheSizeMB : simulateCacheSizes) {
			CacheSizeStats csStats = cacheSizeStats.get(cacheSizeMB);
			stats.add(csStats);
		}
		output(stats.stream().map(i -> i.missRatio).collect(Collectors.toList()));
		output(stats.stream().map(i -> i.evictRatio).collect(Collectors.toList()));
		executor.shutdown();
		return db;
	}

	public static void run(List<Long> simulateCacheSizes, String dir, String fileName, Configuration config,
			long dbSize, List<CacheSizeStats> stats, int partitions) {

		System.out.println("Running " + fileName + " now");

		ExecutorService executor = Executors.newFixedThreadPool(simulateCacheSizes.size());
		List<Future<Void>> list = new ArrayList<>();
		final Map<Long, CacheSizeStats> cacheSizeStats = new ConcurrentHashMap<>();

		for (long cacheSizeMB : simulateCacheSizes) {
			final long cs = cacheSizeMB;
			list.add(executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					long cacheSize = cs * 1024 * 1024;
					PartitionedCache client = new PartitionedCache(partitions, config.getCacheType(),
							cacheSize / partitions);

					String filename = fileName + "-cacheMB-" + cs + "-partitions-" + partitions;
					File file = new File(dir + "/" + filename);
					file.createNewFile();

					WorkloadGenerator gen = new WorkloadGenerator(config, client, file);
					gen.runYCSB(dbSize);
					gen.close();
					CacheSizeStats stats = new CacheSizeStats();
					cacheSizeStats.put(cs, stats);
					stats.missRatio = client.getTotalMisses().divide(client.getTotalReads(), 4, RoundingMode.HALF_UP)
							.doubleValue();
					stats.evictRatio = client.getTotalEvictions()
							.divide(client.getTotalMisses(), 4, RoundingMode.HALF_UP).doubleValue();
					stats.read = client.getTotalReads();

					for (int i = 0; i < partitions; i++) {
						stats.missRatios.add(client.getTotalMisses(i)
								.divide(client.getTotalReads(i), 4, RoundingMode.HALF_UP).doubleValue());
						stats.evictMissRatios.add(client.getTotalEvictions(i)
								.divide(client.getTotalMisses(i), 4, RoundingMode.HALF_UP).doubleValue());
						stats.reads.add(client.getTotalReads(i).divide(client.getTotalReads(), 4, RoundingMode.HALF_UP)
								.doubleValue());
					}
					System.out.println(cs + " MB, " + stats);
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
		for (long cacheSizeMB : simulateCacheSizes) {
			CacheSizeStats csStats = cacheSizeStats.get(cacheSizeMB);
			stats.add(csStats);
		}
		output(stats.stream().map(i -> i.missRatio).collect(Collectors.toList()));
		output(stats.stream().map(i -> i.evictRatio).collect(Collectors.toList()));
		executor.shutdown();
	}

}
