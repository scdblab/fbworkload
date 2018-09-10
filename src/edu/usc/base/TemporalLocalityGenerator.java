package edu.usc.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemporalLocalityGenerator {

	private long currentMinKey;

	private List<Double> locality = new ArrayList<Double>(
			Arrays.<Double>asList(0.885, 0.04, 0.025, 0.008, 0.004, 0.003));

	public TemporalLocalityGenerator() {
		super();
		double remaining = 1.0 - locality.stream().mapToDouble(i -> {
			return i;
		}).sum();
		while (remaining > 0.003) {
			remaining -= 0.002;
			locality.add(0.002);
		}
		while (remaining >= 0) {
			remaining -= 0.001;
			if (remaining < 0) {
				break;
			}
			locality.add(0.001);
		}
		double sum = locality.stream().mapToDouble(i -> {
			return i;
		}).sum();
		Utility.CHECK(sum == 1.0, sum + " locality sum does not equal to 1.0");

		System.out.println("Temporal locality hours " + locality.size());
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
