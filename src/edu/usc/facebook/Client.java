package edu.usc.facebook;

import edu.usc.base.Access.Operation;

public interface Client {

	public void access(Operation op, long key, int keySize, int valueSize, long timestamp);

	public String outputStats(long time);

	public String finalStats();
}
