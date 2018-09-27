package edu.usc.cache;

/**
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public interface Cache {
	public int get(long key);

	public void delete(long key);

	public int set(long key, int value, int size);
	
	public int size();
}
