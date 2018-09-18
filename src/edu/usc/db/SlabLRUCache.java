package edu.usc.db;

import java.util.HashMap;
import java.util.Map;

import edu.usc.base.Utility;

public class SlabLRUCache implements Cache {
	private int[] slabClasses;
	private Map<Integer, LRUCache> slabClassLRU = new HashMap<>();
	private Map<Long, Integer> keySlabClass = new HashMap<>();

	private long availableCapacity;

	public SlabLRUCache(long capacity) {
		slabClasses = new int[] { 96, 120, 152, 192, 240, 304, 384, 480, 600, 752, 944, 1184, 1480, 1856, 2320, 2904,
				3632, 4544, 5680, 7104, 8880, 11104, 13880, 17352, 21696, 27120, 33904, 42384, 52984, 66232, 82792,
				103496, 129376, 161720, 202152, 252696, 315872, 394840, 524288, 1024 * 1024 };
		this.availableCapacity = capacity;
	}

	private int computeSlabClass(int size) {
		return Utility.select(slabClasses, size);
	}

	private int roundsize(int size) {
		int sc = computeSlabClass(size);
		return slabClasses[sc];
	}

	public int get(long key) {
		Integer slabclass = keySlabClass.get(key);
		if (slabclass == null) {
			return -1;
		}
		return slabClassLRU.get(slabclass).get(key);
	}

	public void delete(long key) {
		Integer slabclass = keySlabClass.get(key);
		if (slabclass == null) {
			return;
		}
		slabClassLRU.get(slabclass).delete(key);
	}

	public int set(long key, int value, int size) {
		int roundedSize = roundsize(size);
		if (roundedSize < size) {
			throw new IllegalArgumentException();
		}

		Integer slabclass = keySlabClass.get(key);
		if (slabclass != null) {
			slabClassLRU.get(slabclass).delete(key);
		}

		int sclass = computeSlabClass(roundedSize);
		LRUCache cache = slabClassLRU.get(sclass);

		if (cache == null || cache.availableCapacity < roundedSize) {
			if (availableCapacity >= 1024 * 1024) {
				int chunks = 1024 * 1024 / roundedSize;
				long cap = chunks * roundedSize;

				if (cache == null) {
					slabClassLRU.put(sclass, new LRUCache(0, cap));
				} else {
					slabClassLRU.get(sclass).capacity += cap;
					cache.availableCapacity += cap;
				}
				availableCapacity -= 1024 * 1024;
			} else {
				return -1;
			}
		}
		int evicted = slabClassLRU.get(sclass).set(key, value, roundedSize);
		if (evicted != -1) {
			keySlabClass.put(key, sclass);
		}
		return evicted;
	}

	public static void main(String[] args) {
		new SlabLRUCache(1000);
	}

	@Override
	public int size() {
		return keySlabClass.size();
	}
}
