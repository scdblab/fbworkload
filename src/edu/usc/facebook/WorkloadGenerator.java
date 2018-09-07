package edu.usc.facebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.usc.base.Access;
import edu.usc.base.Access.Operation;
import edu.usc.base.Configuration;
import edu.usc.base.InterarrivalGapGenerator;
import edu.usc.base.KeyAccessGenerator;
import edu.usc.base.KeySizeGenerator;
import edu.usc.base.SpatialLocalityGenerator;
import edu.usc.base.TemporalLocalityGenerator;
import edu.usc.base.ValueSizeGenerator;
import edu.usc.base.WorkingSet;

public class WorkloadGenerator {

	private static final long ONE_HOUR = 3600000000l;

	private final Configuration config;
	private final InterarrivalGapGenerator interarrivalGapGenerator;
	private final KeySizeGenerator keySizeGenerator;
	private final ValueSizeGenerator valueSizeGenerator;
	private final SpatialLocalityGenerator spatialLocalityGenerator;
	private final TemporalLocalityGenerator temporalLocalityGenerator;
	private final Random rand;

	private final TraceRequestProcessor processor;

	public WorkloadGenerator(Configuration config, TraceRequestProcessor processor) {
		super();
		this.config = config;
		this.processor = processor;
		this.rand = new Random(config.getSeed());
		this.interarrivalGapGenerator = new InterarrivalGapGenerator(this.rand);
		this.keySizeGenerator = new KeySizeGenerator(rand);
		this.valueSizeGenerator = new ValueSizeGenerator(rand);
		this.spatialLocalityGenerator = new SpatialLocalityGenerator();
		this.temporalLocalityGenerator = new TemporalLocalityGenerator();
	}

	public void generate() {
		List<WorkingSet> workingSets = new ArrayList<>();

		for (int hour = 0; hour < config.getHours(); hour++) {
			int meanArrivaltime = this.interarrivalGapGenerator.mean(hour);
			long numberOfRequests = ONE_HOUR / meanArrivaltime;
			long workingSetSize = (long) (numberOfRequests * spatialLocalityGenerator.percentUniqueKeys(hour));

			if (hour < temporalLocalityGenerator.size()) {
				workingSets.add(0, temporalLocalityGenerator.prepare(workingSetSize));
				continue;
			}

			WorkingSet workingSet = temporalLocalityGenerator.computeWorkingSet(workingSets, workingSetSize);
			workingSets.add(0, workingSet);
			KeyAccessGenerator keyAccess = new KeyAccessGenerator(rand, workingSetSize);

			for (long now = 0; now < ONE_HOUR; now++) {
				int microsecond = interarrivalGapGenerator.generateInterarrivalTime(hour);
				long timestamp = now + microsecond;

				int keySize = keySizeGenerator.get();
				int valSize = valueSizeGenerator.get();
				long key = keyAccess.get();
				long realKey = workingSet.mapKey(key);

				Operation operation;

				double op = rand.nextDouble();
				if (op < config.getRead()) {
					operation = Operation.READ;
				} else if (op < config.getRead() + config.getReplace()) {
					operation = Operation.REPLACE;
				} else {
					operation = Operation.DELETE;
				}
				processor.access(new Access(operation, realKey, keySize, valSize, timestamp));
			}

		}
	}
}
