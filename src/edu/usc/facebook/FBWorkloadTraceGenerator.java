package edu.usc.facebook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Random;

public class FBWorkloadTraceGenerator {
	public static void main(String[] args) throws Exception {
		long seed = 1000;
		Random rand = new Random(seed);
		FBWorkloads fbworkload = new FBWorkloads(rand);
		String spec = args[0];
		String traceFile = args[1];

		Properties prop = new Properties();
		prop.load(new BufferedReader(new FileReader(new File(spec))));

		double read = Double.parseDouble(prop.getProperty("read"));
		double replace = Double.parseDouble(prop.getProperty("replace"));
		double delete = Double.parseDouble(prop.getProperty("delete"));
		long items = Long.parseLong(prop.getProperty("items"));
		long requests = Long.parseLong(prop.getProperty("requests"));

		new File(traceFile).createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(traceFile)));

		ZipfianGenerator zipf = new ZipfianGenerator(items, rand);

		long now = 0;
		StringBuilder trace = new StringBuilder();
		for (long i = 1; i <= requests; i++) {
			int key = zipf.nextValue().intValue();
			int keySize = fbworkload.generateKeySize();
			int valueSize = fbworkload.generateValueSize();

			int micro = fbworkload.generateInterarrivalTime();
			now += micro;

			double op = rand.nextDouble();
			if (op <= read) {
				trace.append("READ,");
			} else if (op <= read + replace) {
				trace.append("REPLACE,");
			} else {
				trace.append("DELETE,");
			}
			trace.append(String.format("%d,%d,%d,%d\n", key, keySize, valueSize, now));

			if (i % 100000 == 0) {
				bw.write(trace.toString());
				trace = new StringBuilder();
				System.out.println(String.format("Generated %d requests", i));
			}
		}
		bw.write(trace.toString());
		bw.close();
	}
}
