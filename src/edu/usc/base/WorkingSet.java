package edu.usc.base;

import java.util.List;

public class WorkingSet {
	private final long size;
	private final List<Long> minKeys;
	private final List<Long> numKeys;

	private final long[] nums;

	public WorkingSet(long size, List<Long> minKeys, List<Long> numKeys) {
		super();
		this.size = size;
		this.minKeys = minKeys;
		this.numKeys = numKeys;
		nums = new long[this.numKeys.size() + 1];
		nums[0] = 0;
		for (int i = 1; i <= numKeys.size(); i++) {
			nums[i] = nums[i - 1] + numKeys.get(i);
		}
	}

	public long getSize() {
		return size;
	}

	public List<Long> getMinKeys() {
		return minKeys;
	}

	public List<Long> getNumKeys() {
		return numKeys;
	}

	public long mapKey(long key) {
		int i = 0;
		long flat = 0;
		for (; i < nums.length; i++) {
			if (key + 1 <= nums[i]) {
				break;
			}
			flat += nums[i];
		}
		long num = key - flat;
		return minKeys.get(i - 1) + num;
	}

}
