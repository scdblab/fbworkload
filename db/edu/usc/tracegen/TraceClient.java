package edu.usc.tracegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import edu.usc.hoagie.Client;

/**
 * A trace client that writes a generated request to a file.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class TraceClient implements Client {

	private final BufferedWriter bw;

	public TraceClient(String outputFile) throws Exception {
		super();
		bw = new BufferedWriter(new FileWriter(new File(outputFile)));
	}

	@Override
	public void access(Operation op, long key, int keySize, int valueSize, long timestamp) {
		StringBuilder builder = new StringBuilder();
		builder.append(op);
		builder.append(",");
		builder.append(key);
		builder.append(",");
		builder.append(keySize);
		builder.append(",");
		builder.append(valueSize);
		builder.append(",");
		builder.append(timestamp);
		builder.append("\n");
		try {
			bw.write(builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String outputStats(long second) {
		System.out.println("Generated " + second + " seconds of requests");
		return "";
	}

	@Override
	public String finalStats() {
		System.out.println("Completed generating requests.");
		try {
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
