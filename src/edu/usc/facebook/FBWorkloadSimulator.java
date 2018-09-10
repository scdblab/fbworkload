package edu.usc.facebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.usc.db.LRUCache;
import edu.usc.distributions.ZipfianGenerator;

public class FBWorkloadSimulator {

	public static enum Config {
		GEMINI, STALE, DISCARD
	}

	public static class Stats {
		List<Integer> reads = new ArrayList<>();
		List<Integer> stales = new ArrayList<>();
		List<Double> staleReads = new ArrayList<>();
		List<Double> hits = new ArrayList<>();
	}

	public static Stats run(Config config, int failDuration) {
		long seed = 1000;
		Random rand = new Random(seed);
		System.out.println(config);

		Stats stats = new Stats();
		FBWorkloads fbworkload = new FBWorkloads(rand);

		double read = 0.95;
		int numRecords = 10000000;
		int totalReqs = 30000000;
		long failStart = 300;
		long failEnd = failStart + failDuration;
		int totalCaches = 100;
		int totalFrags = 5000;
		int totalFailCaches = 20;
		double cacheSize = 0.5;
		long outputInterval = 1000 * 1000;
		int warmup = (int) (numRecords * cacheSize * 0.5);

		System.out.println(totalReqs + " " + failStart + " " + failEnd);

		// Caches
		List<LRUCache> caches = new ArrayList<>();
		List<Integer> dirtyKeys = new ArrayList<>();
		Set<Integer> dirtyKeysSet = new HashSet<>();

		for (int i = 0; i < totalCaches; i++) {
			caches.add(new LRUCache(i, (int) (numRecords * cacheSize * fbworkload.getMeanRequestSize() / totalCaches)));
		}

		// Fragments
		List<Integer> fragments = new ArrayList<Integer>();
		for (int i = 0; i < totalFrags; i++) {
			fragments.add(i % totalCaches);
		}
		Collections.shuffle(fragments, rand);
		List<Integer> failFragments = new ArrayList<Integer>(fragments);
		int nextCache = totalFailCaches;
		for (int i = 0; i < totalFailCaches; i++) {
			for (int f = 0; f < totalFrags; f++) {
				if (failFragments.get(f) == i) {
					failFragments.set(f, nextCache);
					nextCache += 1;
					if (nextCache >= totalCaches) {
						nextCache = totalFailCaches;
					}
				}
			}
		}

		for (int f = 0; f < totalFrags; f++) {
			if (failFragments.get(f) < totalFailCaches) {
				System.exit(0);
			}
		}

		// Stats
		long staleReads = 0;
		long reads = 0;
		long hits = 0;

		// Database.
		int[] database = new int[numRecords];
		int nextVal = 1;

		// Experiments.
		ZipfianGenerator zip = new ZipfianGenerator(numRecords, rand);
		long now = 0;
		int interval = 1;

		boolean recoveryMode = false;
		boolean failMode = false;

		// Warm up
		for (int key = 0; key < warmup; key++) {
			int cache = fragments.get(key % fragments.size());
			int keySize = fbworkload.generateKeySize();
			int valueSize = fbworkload.generateValueSize();
			int size = keySize + valueSize;
			caches.get(cache).set(key, database[key], size);
		}
		System.out.println("warm up completes");

		for (int i = 0; i < totalReqs; i++) {
			int key = zip.nextValue().intValue();
			int keySize = fbworkload.generateKeySize();
			int valueSize = fbworkload.generateValueSize();
			int size = keySize + valueSize;

			int micro = fbworkload.generateDailyInterarrivalTime();
			now += micro;

			List<Integer> frags = fragments;

			if (interval > failEnd) {
				if (!recoveryMode) {
					// System.out.println("Recovered");
					System.out.println(String.format("dirtykeys,%d,%d", dirtyKeys.size(), dirtyKeysSet.size()));
				}
				recoveryMode = true;
			} else if (interval > failStart) {
				if (!failMode) {
					// System.out.println("Failed");
					if (Config.DISCARD.equals(config)) {
						for (int c = 0; c < totalFailCaches; c++) {
							caches.get(c).clear();
						}
					}
				}
				frags = failFragments;
				failMode = true;
			}

			boolean isRead = rand.nextDouble() <= read;
			int cache = frags.get(key % frags.size());

			if (isRead) {
				// Read
				reads++;
				int val = caches.get(cache).get(key);
				if (val == -1) {
					// miss
					if (recoveryMode && Config.GEMINI.equals(config)) {
						// Look up secondary cache.
						int secondaryCache = failFragments.get(key % failFragments.size());
						val = caches.get(secondaryCache).get(key);
						if (val != -1) {
							hits++;
						}
						caches.get(cache).set(key, val, size);
					}

					if (val == -1) {
						val = database[key];
						caches.get(cache).set(key, database[key], size);
					}
				} else {
					// hit
					hits++;
					if (recoveryMode && Config.GEMINI.equals(config) && dirtyKeysSet.contains(key)) {
						hits--;
						dirtyKeysSet.remove(key);
						caches.get(cache).delete(key);
						int secondaryCache = failFragments.get(key % failFragments.size());
						val = caches.get(secondaryCache).get(key);
						if (val != -1) {
							hits++;
							caches.get(cache).set(key, val, size);
						}
					}
					if (val == -1) {
						val = database[key];
						caches.get(cache).set(key, database[key], size);
					}
				}
				if (val != database[key]) {
					System.out.println("stale," + val + "," + database[key]);
					staleReads++;
				}
			} else {
				// Update
				caches.get(cache).delete(key);
				if (recoveryMode && Config.GEMINI.equals(config)) {
					// Delete in both caches.
					int secondaryCache = failFragments.get(key % failFragments.size());
					caches.get(secondaryCache).delete(key);
				}
				if (failMode && Config.GEMINI.equals(config)) {
					int primaryCache = fragments.get(key % fragments.size());
					if (primaryCache < totalFailCaches) {
						dirtyKeys.add(key);
						dirtyKeysSet.add(key);
					}
				}

				database[key] = nextVal;
				nextVal += 1;
			}

			if (now > outputInterval) {
				System.out.println(String.format("%d,%d,%d,%d,%.2f,%.2f", interval, reads, hits, staleReads,
						(hits * 100.0) / reads, (staleReads * 100.0) / reads));
				stats.reads.add((int) reads);
				stats.stales.add((int) staleReads);
				stats.hits.add((hits * 100.0) / reads);
				stats.staleReads.add((staleReads * 100.0) / reads);

				interval++;
				reads = 0;
				hits = 0;
				staleReads = 0;
				now = 0;
			}
		}
		// System.out.println(now);
		interval++;
		System.out.println(String.format("%d,%d,%d,%d,%.2f,%.2f", interval, reads, hits, staleReads,
				(hits * 100.0) / reads, (staleReads * 100.0) / reads));
		return stats;
	}

	public static void main(String[] args) {
		 StaleRateExp();
//		 HitRateExp();
//		run(Config.GEMINI, 100);
	}

	private static void StaleRateExp() {
		Stats stale10Stats = run(Config.STALE, 10);
		Stats stale100Stats = run(Config.STALE, 100);

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		for (int i = 0; i < stale10Stats.reads.size(); i++) {
			if (i < stale100Stats.hits.size()) {
				System.out.println(String.format("%d,%d,%d,%.2f,%.2f", i, stale10Stats.stales.get(i),
						stale100Stats.stales.get(i), stale10Stats.staleReads.get(i), stale100Stats.staleReads.get(i)));
			}
		}
	}

	private static void HitRateExp() {
		Stats discardStats = run(Config.DISCARD, 100);
		Stats staleStats = run(Config.STALE, 100);
		Stats geminiStats = run(Config.GEMINI, 100);

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		for (int i = 0; i < discardStats.reads.size(); i++) {
			if (i < staleStats.hits.size() && i < geminiStats.hits.size()) {
				System.out.println(String.format("%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f", i, discardStats.hits.get(i),
						staleStats.hits.get(i), geminiStats.hits.get(i), discardStats.staleReads.get(i),
						staleStats.staleReads.get(i), geminiStats.staleReads.get(i)));
			}
		}
	}

}
