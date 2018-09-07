package edu.usc.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemporalLocalityGenerator {

	private double newKeys = 88.5;
	private double lastHour = 4;
	private long currentMinKey;

	private List<Double> locality = new ArrayList<Double>(Arrays.<Double>asList(88.5, 4.0, 2.5, 0.8, 0.4, 0.3));

	public TemporalLocalityGenerator() {
		super();
		double remaining = 100.0 - locality.stream().mapToDouble(i -> {
			return i;
		}).sum();
		while (remaining > 0) {
			remaining -= 0.1;
			locality.add(0.1);
		}
	}

	public int size() {
		return locality.size();
	}

	public WorkingSet prepare(long workingSetSize) {
		currentMinKey += workingSetSize;
		return new WorkingSet(workingSetSize, new ArrayList<Long>(Arrays.asList(currentMinKey - workingSetSize)),
				new ArrayList<Long>(Arrays.asList(workingSetSize)));
	}

	public WorkingSet computeWorkingSet(List<WorkingSet> pastWorkingSet, long workingSetSize) {
		List<Long> minKeys = new ArrayList<>();
		List<Long> numKeys = new ArrayList<>();
		long remainingSize = workingSetSize;

		for (int i = 1; i < locality.size(); i++) {
			long reusedKeys = (long) (workingSetSize * locality.get(i));
			WorkingSet workingSet = pastWorkingSet.get(i - 1);

			long minKey = workingSet.getMinKeys().get(0);
			long numKey = workingSet.getNumKeys().get(0);

			minKeys.add(minKey);
			numKeys.add(Math.min(reusedKeys, numKey));
			remainingSize -= Math.min(reusedKeys, numKey);
		}
		minKeys.add(0, currentMinKey);
		numKeys.add(0, remainingSize);
		currentMinKey += remainingSize;
		return new WorkingSet(workingSetSize, minKeys, numKeys);
	}
}
