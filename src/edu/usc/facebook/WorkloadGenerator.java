package edu.usc.facebook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private static final long ONE_SECOND = 1000000l;

	private final Configuration config;
	private final InterarrivalGapGenerator interarrivalGapGenerator;
	private final KeySizeGenerator keySizeGenerator;
	private final ValueSizeGenerator valueSizeGenerator;
	private final SpatialLocalityGenerator spatialLocalityGenerator;
	private final TemporalLocalityGenerator temporalLocalityGenerator;
	private final Random rand;
	private final File outputFile;
	private BufferedWriter bw;

	private final Client processor;

	public WorkloadGenerator(Configuration config, Client processor, File outputFile) {
		super();
		this.config = config;
		this.processor = processor;
		this.outputFile = outputFile;
		try {
			this.bw = new BufferedWriter(new FileWriter(this.outputFile));
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.rand = new Random(config.getSeed());
		this.interarrivalGapGenerator = new InterarrivalGapGenerator(this.rand);
		this.keySizeGenerator = new KeySizeGenerator(rand);
		this.valueSizeGenerator = new ValueSizeGenerator(rand);
		this.spatialLocalityGenerator = new SpatialLocalityGenerator();
		this.temporalLocalityGenerator = new TemporalLocalityGenerator();
	}

	private void write(String line) {
		try {
			bw.write(line + "\n");
			// bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			write(this.processor.finalStats());
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WorkingSet run() {
		List<WorkingSet> workingSets = prepare();
		for (int hour = 0; hour < config.getHours(); hour++) {
			int meanArrivaltime = this.interarrivalGapGenerator.mean(hour);
			long numberOfRequests = ONE_HOUR / meanArrivaltime;
			long workingSetSize = (long) (numberOfRequests * spatialLocalityGenerator.percentUniqueKeys(hour));

			WorkingSet workingSet = temporalLocalityGenerator.computeWorkingSet(workingSets, workingSetSize);
			workingSets.add(0, workingSet);
			KeyAccessGenerator keyAccess = new KeyAccessGenerator(rand, config, workingSetSize);

			int second = 1;
			for (long now = 0; now < ONE_HOUR;) {
				int microsecond = interarrivalGapGenerator.generateInterarrivalTime(hour);
				now += microsecond;
				long timestamp = now;

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
				processor.access(operation, realKey, keySize, valSize, timestamp);

				if (now > second * ONE_SECOND) {
					write(processor.outputStats(hour * 60 * 60 + second));
					second++;
				}
			}
		}
		for (int hour = 0; hour < config.getHours(); hour++) {
			write(workingSets.get(hour).toString());
		}
		return workingSets.get(0);
	}

	public void runYCSB(long databaseSize) {
		long throughputsec = 1000 * 1000 / this.interarrivalGapGenerator.mean();
		KeyAccessGenerator keyAccess = new KeyAccessGenerator(rand, config, databaseSize);
		int meanKey = (int) keySizeGenerator.getKeySizeDist().getMean();
		int meanVal = (int) valueSizeGenerator.getValueSizeDist().getMean();
		for (int hour = 0; hour < config.getHours(); hour++) {
			long oneHour = 60 * 60;

			if (config.isEnableLoadChange()) {
				int second = 1;
				for (long now = 0; now < ONE_HOUR;) {
					int microsecond = interarrivalGapGenerator.generateInterarrivalTime(hour);
					now += microsecond;
					long timestamp = now;

					int keySize = keySizeGenerator.get();
					int valSize = valueSizeGenerator.get();
					long key = keyAccess.get();

					Operation operation;

					double op = rand.nextDouble();
					if (op < config.getRead()) {
						operation = Operation.READ;
					} else if (op < config.getRead() + config.getReplace()) {
						operation = Operation.REPLACE;
					} else {
						operation = Operation.DELETE;
					}
					processor.access(operation, key, keySize, valSize, timestamp);

					if (now > second * ONE_SECOND) {
						write(processor.outputStats(hour * 60 * 60 + second));
						second++;
					}

				}
			} else {
				for (long second = 0; second < oneHour; second++) {
					for (long req = 0; req < throughputsec; req++) {
						long key = keyAccess.get();
						int keySize = keySizeGenerator.get();
						int valSize = valueSizeGenerator.get();

						if (!config.isEnableSize()) {
							keySize = meanKey;
							valSize = meanVal;
						}

						Operation operation;
						double op = rand.nextDouble();
						if (op < config.getRead()) {
							operation = Operation.READ;
						} else if (op < config.getRead() + config.getReplace()) {
							operation = Operation.REPLACE;
						} else {
							operation = Operation.DELETE;
						}
						processor.access(operation, key, keySize, valSize, 0);
					}
					write(processor.outputStats(hour * 60 * 60 + second));
				}
			}
		}
	}

	private List<WorkingSet> prepare() {
		List<WorkingSet> workingSets = new ArrayList<>();

		while (true) {
			int meanArrivaltime = this.interarrivalGapGenerator.mean(0);
			long numberOfRequests = ONE_HOUR / meanArrivaltime;
			long workingSetSize = (long) (numberOfRequests * spatialLocalityGenerator.percentUniqueKeys(0));

			if (workingSets.size() < temporalLocalityGenerator.size()) {
				WorkingSet workingSet = temporalLocalityGenerator.prepare(workingSetSize);
				workingSets.add(0, workingSet);
			} else {
				break;
			}
		}
		return workingSets;
	}
}
