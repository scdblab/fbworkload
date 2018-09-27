package edu.usc.cache;

import java.math.BigDecimal;
import java.math.RoundingMode;

import edu.usc.hoagie.Client;

/**
 * A cache that keeps track of cache stats.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class StatsCache implements Client {

	private final Cache cache;
	private long misses;
	private long reads;
	private long evictions;
	private BigDecimal totalEvictions = new BigDecimal("0");
	private BigDecimal totalMisses = new BigDecimal("0");
	private BigDecimal totalReads = new BigDecimal("0");

	public StatsCache(Cache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public void access(Operation op, long key, int keySize, int valueSize, long timestamp) {
		switch (op) {
		case DELETE:
			cache.delete(key);
			break;
		case READ:
			reads++;
			int val = cache.get(key);
			if (val == -1) {
				misses++;
				int evicted = cache.set(key, 1, keySize + valueSize);
				if (evicted != -1) {
					evictions += evicted;
				}
			}
			break;
		case REPLACE:
			cache.delete(key);
			int evicted = cache.set(key, 1, keySize + valueSize);
			if (evicted != -1) {
				evictions += evicted;
			}
			break;
		default:
			break;
		}
	}

	public Cache getCache() {
		return cache;
	}

	public BigDecimal getTotalMisses() {
		return totalMisses;
	}

	public BigDecimal getTotalReads() {
		return totalReads;
	}

	public BigDecimal getTotalEvictions() {
		return totalEvictions;
	}

	@Override
	public String outputStats(long time) {
		String message = String.format("%d,%d,%d,%d,%.4f,%.4f", misses, reads, cache.size(), evictions,
				(double) misses / (double) reads, (double) evictions / (double) misses);
		totalMisses = totalMisses.add(new BigDecimal("" + misses));
		totalReads = totalReads.add(new BigDecimal("" + reads));
		totalEvictions = totalEvictions.add(new BigDecimal(String.valueOf(evictions)));
		misses = 0;
		reads = 0;
		evictions = 0;
		return time + "," + message;
	}

	@Override
	public String finalStats() {
		return String.format("%s,%s,%s,%s,%s", totalMisses.toString(), totalReads.toString(), totalEvictions.toString(),
				String.valueOf(totalMisses.divide(totalReads, 4, RoundingMode.HALF_UP).doubleValue()),
				String.valueOf(totalEvictions.divide(totalMisses, 4, RoundingMode.HALF_UP).doubleValue()));
	}

}
