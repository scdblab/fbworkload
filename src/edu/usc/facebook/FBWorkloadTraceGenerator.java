package edu.usc.facebook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;

import edu.usc.distributions.ZipfianGenerator;

public class FBWorkloadTraceGenerator {

	private static final BigInteger ONE_HOUR = new BigInteger("3600000000");

	public static void main(String[] args) throws Exception {
		computeHourlyTrace(args);
	}
	
	private static void computeHourlyTrace(String[] args) throws IOException, FileNotFoundException {
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
		
		int hour = 0;

		new File(traceFile).createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(String.format("%s-%dh", traceFile, hour))));

		ZipfianGenerator zipf = new ZipfianGenerator(items, rand);

		System.out.println(ONE_HOUR);
		BigInteger now = new BigInteger("0");
		StringBuilder trace = new StringBuilder();
		
		for (long i = 1; i <= requests; i++) {
			int key = zipf.nextValue().intValue();
			
			int keySize = fbworkload.generateKeySize();
			int valueSize = fbworkload.generateValueSize();

			int micro = fbworkload.generateInterarrivalTime(hour % 24);
			now = now.add(BigInteger.valueOf(micro));
			if (now.compareTo(ONE_HOUR) == 1) {
				now = BigInteger.valueOf(0);
				hour += 1;
				bw.write(trace.toString());
				trace = new StringBuilder();
				bw.close();
				bw = new BufferedWriter(new FileWriter(new File(String.format("%s-%dh", traceFile, hour))));
			}

			double op = rand.nextDouble();
			if (op <= read) {
				trace.append("READ,");
			} else if (op <= read + replace) {
				trace.append("REPLACE,");
			} else {
				trace.append("DELETE,");
			}
			trace.append(String.format("%d,%d,%d,%s\n", key, keySize, valueSize, now.toString()));

			if (i % 100000 == 0) {
				bw.write(trace.toString());
				trace = new StringBuilder();
				System.out.println(String.format("Generated %d requests", i));
			}
		}
		bw.write(trace.toString());
		bw.close();
	}

	private static void generateHourlyTrace(String[] args) throws IOException, FileNotFoundException {
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
		
		int hour = 0;

		new File(traceFile).createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(String.format("%s-%dh", traceFile, hour))));

		ZipfianGenerator zipf = new ZipfianGenerator(items, rand);

		System.out.println(ONE_HOUR);
		BigInteger now = new BigInteger("0");
		StringBuilder trace = new StringBuilder();
		
		for (long i = 1; i <= requests; i++) {
			int key = zipf.nextValue().intValue();
			
			int keySize = fbworkload.generateKeySize();
			int valueSize = fbworkload.generateValueSize();

			int micro = fbworkload.generateInterarrivalTime(hour % 24);
			now = now.add(BigInteger.valueOf(micro));
			if (now.compareTo(ONE_HOUR) == 1) {
				now = BigInteger.valueOf(0);
				hour += 1;
				bw.write(trace.toString());
				trace = new StringBuilder();
				bw.close();
				bw = new BufferedWriter(new FileWriter(new File(String.format("%s-%dh", traceFile, hour))));
			}

			double op = rand.nextDouble();
			if (op <= read) {
				trace.append("READ,");
			} else if (op <= read + replace) {
				trace.append("REPLACE,");
			} else {
				trace.append("DELETE,");
			}
			trace.append(String.format("%d,%d,%d,%s\n", key, keySize, valueSize, now.toString()));

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
