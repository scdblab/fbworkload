package edu.usc.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Configuration {
	private double read;
	private double replace;
	private double delete;
	private int hours;
	private long seed;
	private boolean enableSize;
	private boolean enableLoadChange;
	private boolean enableSpatial;
	private boolean enableTemporal;

	public Configuration(String configFile) throws Exception {
		Properties prop = new Properties();
		prop.load(new BufferedReader(new FileReader(new File(configFile))));
		read = Double.parseDouble(prop.getProperty("read"));
		replace = Double.parseDouble(prop.getProperty("replace"));
		delete = Double.parseDouble(prop.getProperty("delete"));
		hours = Integer.parseInt(prop.getProperty("hours"));
		seed = Integer.parseInt(prop.getProperty("seed"));
		enableSize = Boolean.parseBoolean(prop.getProperty("enableSize"));
		enableLoadChange = Boolean.parseBoolean(prop.getProperty("enableLoadChange"));
		enableSpatial = Boolean.parseBoolean(prop.getProperty("enableSpatial"));
		enableTemporal = Boolean.parseBoolean(prop.getProperty("enableTemporal"));
	}

	public Configuration(double read, double replace, double delete, int hours, long seed, boolean enableSize,
			boolean enableLoadChange, boolean enableSpatial, boolean enableTemporal) {
		super();
		this.read = read;
		this.replace = replace;
		this.delete = delete;
		this.hours = hours;
		this.seed = seed;
		this.enableSize = enableSize;
		this.enableLoadChange = enableLoadChange;
		this.enableSpatial = enableSpatial;
		this.enableTemporal = enableTemporal;
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

	public void setEnableSize(boolean enableSize) {
		this.enableSize = enableSize;
	}

	public void setEnableLoadChange(boolean enableLoadChange) {
		this.enableLoadChange = enableLoadChange;
	}

	public void setEnableSpatial(boolean enableSpatial) {
		this.enableSpatial = enableSpatial;
	}

	public void setEnableTemporal(boolean enableTemporal) {
		this.enableTemporal = enableTemporal;
	}

	public boolean isEnableSize() {
		return enableSize;
	}

	public boolean isEnableLoadChange() {
		return enableLoadChange;
	}

	public boolean isEnableSpatial() {
		return enableSpatial;
	}

	public boolean isEnableTemporal() {
		return enableTemporal;
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
				+ ", enableSize=" + enableSize + ", enableLoadChange=" + enableLoadChange + ", enableSpatial="
				+ enableSpatial + ", enableTemporal=" + enableTemporal + "]";
	}

}
