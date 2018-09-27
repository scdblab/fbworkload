package edu.usc.hoagie;

/**
 * An interface that abstracts a client operation to process a request.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public interface Client {

	public static enum Operation {
		READ, REPLACE, DELETE;
	}

	public void access(Operation op, long key, int keySize, int valueSize, long timestamp);

	public String outputStats(long second);

	public String finalStats();
}
