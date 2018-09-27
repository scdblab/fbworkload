package edu.usc.workload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class Configuration {
	private double read;
	private double replace;
	private double delete;
	private int hours;
	private long seed;
	private double zipf;
	// optional
	private String cacheType;

	public Configuration(String configFile) throws Exception {
		Properties prop = new Properties();
		prop.load(new BufferedReader(new FileReader(new File(configFile))));
		zipf = Double.parseDouble(prop.getProperty("zipf"));
		read = Double.parseDouble(prop.getProperty("read"));
		replace = Double.parseDouble(prop.getProperty("replace"));
		delete = Double.parseDouble(prop.getProperty("delete"));
		hours = Integer.parseInt(prop.getProperty("hours"));
		seed = Integer.parseInt(prop.getProperty("seed"));
		cacheType = prop.getProperty("cacheType", "");
	}

	public String getCacheType() {
		return cacheType;
	}

	public double getZipf() {
		return zipf;
	}

	public void setRead(double read) {
		this.read = read;
	}

	public void setReplace(double replace) {
		this.replace = replace;
	}

	public void setDelete(double delete) {
		this.delete = delete;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public void setSeed(long seed) {
		this.seed = seed;
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

	public long getSeed() {
		return seed;
	}

	@Override
	public String toString() {
		return "Configuration [read=" + read + ", replace=" + replace + ", delete=" + delete + ", hours=" + hours
				+ ", seed=" + seed + ", zipf=" + zipf + ", cacheType[optional]=" + cacheType + "]";
	}

}
