package edu.usc.db;

public interface Cache {
	public int get(long key);

	public void delete(long key);

	public int set(long key, int value, int size);
	
	public int size();
}
