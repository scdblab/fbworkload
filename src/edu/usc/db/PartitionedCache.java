package edu.usc.db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import edu.usc.base.Access.Operation;
import edu.usc.facebook.Client;

public class PartitionedCache implements Client {

	private List<StatsCache> caches = new ArrayList<>();
	private long cacheSize;

	public PartitionedCache(int partitions, String cacheType, long cacheSize) {
		this.cacheSize = cacheSize;
		for (int i = 0; i < partitions; i++) {
			if ("slab-lru".equals(cacheType)) {
				caches.add(new StatsCache(new SlabLRUCache(cacheSize)));
			} else if ("lru".equals(cacheType)) {
				caches.add(new StatsCache(new LRUCache(i, cacheSize)));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	private StatsCache hash(long key) {
		if (caches.size() == 1) {
			return caches.get(0);
		}
		long hash = key % caches.size();
		return caches.get((int) (hash));
	}

	@Override
	public void access(Operation op, long key, int keySize, int valueSize, long timestamp) {
		hash(key).access(op, key, keySize, valueSize, timestamp);
	}

	public BigDecimal getTotalMisses(int partition) {
		return caches.get(partition).getTotalMisses();
	}

	public BigDecimal getTotalReads(int partition) {
		return caches.get(partition).getTotalReads();
	}

	public BigDecimal getTotalEvictions(int partition) {
		return caches.get(partition).getTotalEvictions();
	}

	public BigDecimal getTotalMisses() {
		BigDecimal totalMisses = new BigDecimal("0");
		for (int i = 0; i < caches.size(); i++) {
			totalMisses = totalMisses.add(caches.get(i).getTotalMisses());
		}
		return totalMisses;
	}

	public BigDecimal getTotalReads() {
		BigDecimal totalReads = new BigDecimal("0");
		for (int i = 0; i < caches.size(); i++) {
			totalReads = totalReads.add(caches.get(i).getTotalReads());
		}
		return totalReads;
	}

	public BigDecimal getTotalEvictions() {
		BigDecimal totalEvictions = new BigDecimal("0");
		for (int i = 0; i < caches.size(); i++) {
			totalEvictions = totalEvictions.add(caches.get(i).getTotalEvictions());
		}
		return totalEvictions;
	}

	private long priorNow = System.currentTimeMillis();

	@Override
	public String outputStats(long time) {
		if (time % 60 == 0) {
			System.gc();

			long now = System.currentTimeMillis();
			long diff = (now - priorNow) / 1000;
			priorNow = now;

			System.out.println("cache size " + cacheSize + " making progress " + time + " took " + diff + " seconds.");
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < caches.size(); i++) {
			builder.append("p" + i + ",");
			builder.append(caches.get(i).outputStats(time));
			if (i < caches.size() - 1) {
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	@Override
	public String finalStats() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < caches.size(); i++) {
			builder.append(i + ",");
			builder.append(caches.get(i).finalStats());
			builder.append("\n");
		}
		return builder.toString();
	}

}
