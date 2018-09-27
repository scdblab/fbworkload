package edu.usc.hoagie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import edu.usc.hoagie.Client.Operation;
import edu.usc.workload.Configuration;
import edu.usc.workload.InterarrivalGapGenerator;
import edu.usc.workload.KeyAccessGenerator;
import edu.usc.workload.KeySizeGenerator;
import edu.usc.workload.SpatialLocalityGenerator;
import edu.usc.workload.TemporalLocalityGenerator;
import edu.usc.workload.ValueSizeGenerator;
import edu.usc.workload.WorkingSet;

/**
 * Hoagie: A Database and Workload Generator using Published Specifications.
 * 
 * It generates requests based on published statistic of Facebook and calls the
 * client.access to process a request. It invokes client.outputStats after
 * processing one-second requests.
 * 
 * The generator terminates after processing the user specified number of hours
 * and calls client.finalStats.
 * 
 * A user may provide a file to write the stats into. Otherwise, the per-second
 * and final stats will be output to console.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
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
	private final Optional<File> outputFile;
	private final Client client;
	private Optional<BufferedWriter> bw = Optional.empty();
	private List<WorkingSet> workingSetsCanBeDeleted = new ArrayList<>();

	public WorkloadGenerator(Configuration config, Client client, Optional<File> outputFile) {
		super();
		this.config = config;
		this.client = client;
		this.outputFile = outputFile;
		if (outputFile.isPresent()) {
			try {
				this.bw = Optional.of(new BufferedWriter(new FileWriter(this.outputFile.get())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.rand = new Random(config.getSeed());
		this.interarrivalGapGenerator = new InterarrivalGapGenerator(this.rand);
		this.keySizeGenerator = new KeySizeGenerator(rand);
		this.valueSizeGenerator = new ValueSizeGenerator(rand);
		this.spatialLocalityGenerator = new SpatialLocalityGenerator();
		this.temporalLocalityGenerator = new TemporalLocalityGenerator();
	}

	private void write(String line) {
		if (bw.isPresent()) {
			try {
				bw.get().write(line + "\n");
				bw.get().flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(line);
		}
	}

	public void close() {
		try {
			write(this.client.finalStats());
			if (bw.isPresent()) {
				bw.get().flush();
				bw.get().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WorkingSet run() {
		// Prepare working sets.
		List<WorkingSet> workingSets = prepare();
		for (int hour = 0; hour < config.getHours(); hour++) {
			int meanArrivaltime = this.interarrivalGapGenerator.mean(hour);
			long numberOfRequests = ONE_HOUR / meanArrivaltime;

			// Compute working set.
			long workingSetSize = (long) (numberOfRequests * spatialLocalityGenerator.percentUniqueKeys(hour));
			WorkingSet workingSet = temporalLocalityGenerator.computeWorkingSet(workingSets, workingSetSize);
			workingSets.add(0, workingSet);
			KeyAccessGenerator keyAccess = new KeyAccessGenerator(rand, config, workingSetSize);
			int second = 1;

			if (hour >= temporalLocalityGenerator.size()) {
				workingSetsCanBeDeleted.add(workingSets.get(hour));
			}

			// Generate requests within this hour.
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
				client.access(operation, realKey, keySize, valSize, timestamp);

				if (now > second * ONE_SECOND) {
					// output stats after processing one second requests.
					write(client.outputStats(hour * 60 * 60 + second));
					second++;
				}
			}
		}
		// Return the latest working set.
		return workingSets.get(0);
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

	public List<WorkingSet> getWorkingSetsCanBeDeleted() {
		return workingSetsCanBeDeleted;
	}
}
