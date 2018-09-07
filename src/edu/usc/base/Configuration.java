package edu.usc.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Configuration {
	private final double read;
	private final double replace;
	private final double delete;
	private final int hours;
	private final int temporalHours;
	private final long seed;

	public Configuration(String configFile) throws Exception {
		Properties prop = new Properties();
		prop.load(new BufferedReader(new FileReader(new File(configFile))));
		read = Double.parseDouble(prop.getProperty("read"));
		replace = Double.parseDouble(prop.getProperty("replace"));
		delete = Double.parseDouble(prop.getProperty("delete"));
		hours = Integer.parseInt(prop.getProperty("hours"));
		temporalHours = Integer.parseInt(prop.getProperty("temporalHours"));
		seed = Integer.parseInt(prop.getProperty("seed"));
	}

	public double getRead() {
		return read;
	}

	public double getReplace() {
		return replace;
	}

	public double getDelete() {
		return delete;
	}

	public int getHours() {
		return hours;
	}

	public int getTemporalHours() {
		return temporalHours;
	}

	public long getSeed() {
		return seed;
	}

}
