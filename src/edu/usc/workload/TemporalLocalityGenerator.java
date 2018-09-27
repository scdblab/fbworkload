package edu.usc.workload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The generator models the temporal locality based on statistics published by
 * Facebook.
 * 
 * Berk Atikoglu, Yuehai Xu, Eitan Frachtenberg, Song Jiang, and Mike Paleczny.
 * 2012. Workload analysis of a large-scale key-value store. In Proceedings of
 * the 12th ACM SIGMETRICS/PERFORMANCE joint international conference on
 * Measurement and Modeling of Computer Systems (SIGMETRICS '12). ACM, New York,
 * NY, USA, 53-64. DOI=http://dx.doi.org/10.1145/2254756.2254766
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
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
		locality.remove(locality.size() - 1);
		locality.add(0.0005);
		locality.add(0.0005);
		double sum = locality.stream().mapToDouble(i -> {
			return i;
		}).sum();
		Utility.CHECK(sum == 1.0, sum + " locality sum does not equal to 1.0");
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
