package edu.usc.db;

import java.math.BigDecimal;
import java.math.RoundingMode;

import edu.usc.base.Access;
import edu.usc.facebook.TraceRequestProcessor;

public class Cache implements TraceRequestProcessor {

	private final LRUCache cache;
	private long misses;
	private long reads;
	private BigDecimal totalMisses = new BigDecimal("0");
	private BigDecimal totalReads = new BigDecimal("0");

	public Cache(LRUCache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public void access(Access access) {
		switch (access.getOp()) {
		case DELETE:
			cache.delete(access.getKey());
			break;
		case READ:
			reads++;
			int val = cache.get(access.getKey());
			if (val == -1) {
				misses++;
				cache.set(access.getKey(), 1, access.getKeySize() + access.getValueSize());
			}
			break;
		case REPLACE:
			cache.delete(access.getKey());
			cache.set(access.getKey(), 1, access.getKeySize() + access.getValueSize());
			break;
		default:
			break;
		}
	}

	public LRUCache getCache() {
		return cache;
	}

	public BigDecimal getTotalMisses() {
		return totalMisses;
	}

	public BigDecimal getTotalReads() {
		return totalReads;
	}

	@Override
	public String outputStats() {
		String message = String.format("%d,%d,%.2f", misses, reads, (double) misses / (double) reads);
		totalMisses = totalMisses.add(new BigDecimal("" + misses));
		totalReads = totalReads.add(new BigDecimal("" + reads));
		misses = 0;
		reads = 0;
		return message;
	}

	@Override
	public String finalStats() {
		return String.valueOf(totalMisses.divide(totalReads, 4, RoundingMode.HALF_UP).doubleValue());
	}
}
