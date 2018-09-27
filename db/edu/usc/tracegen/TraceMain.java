package edu.usc.tracegen;

import java.io.File;
import java.util.Optional;

import edu.usc.hoagie.WorkloadGenerator;
import edu.usc.workload.Configuration;

/**
 * An example program that writes a generated sequence of requests to a file.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class TraceMain {
	public static void main(String[] args) throws Exception {
		String configFile = args[0];
		String output = args[1];

		Configuration config = new Configuration(configFile);
		System.out.println("Start Hoagie with configuration " + config);
		System.out.println("Write requests into file " + output);

		if (!new File(output).exists()) {
			throw new IllegalAccessError("File " + output + " does not exist.");
		}

		TraceClient client = new TraceClient(output);
		WorkloadGenerator gen = new WorkloadGenerator(config, client, Optional.of(new File(output)));
		gen.run();
		gen.close();
	}
}
