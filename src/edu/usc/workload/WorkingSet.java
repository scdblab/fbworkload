package edu.usc.workload;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a working set. A working set consists of a list of
 * ranges, minKeys and numKeys. A range i includes items from minKeys[i] to
 * minKeys[i]+numKeys[i].
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
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
			nums[i] = nums[i - 1] + numKeys.get(i - 1);
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
		for (; i < nums.length; i++) {
			if (key + 1 <= nums[i]) {
				break;
			}
		}
		if (key < nums[i - 1]) {
			System.out.println("BUG1: " + key + "," + nums[i - 1]);
		}
		long realKey = minKeys.get(i - 1) + key - nums[i - 1];
		if (realKey < 0) {
			System.out.println("BUG2: " + key + ", " + realKey);
		}
		return realKey;
	}

	@Override
	public String toString() {
		return "WorkingSet [size=" + size + ", minKeys=" + minKeys + "\n numKeys=" + numKeys + "\n nums="
				+ Arrays.toString(nums) + "]";
	}

}
