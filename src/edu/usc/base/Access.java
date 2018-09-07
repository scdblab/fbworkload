package edu.usc.base;

public class Access {

	public enum Operation {
		READ, DELETE, REPLACE
	}

	private Operation op;
	private long key;
	private int keySize;
	private int valueSize;
	private long timestamp;

	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		this.key = key;
	}

	public int getKeySize() {
		return keySize;
	}

	public void setKeySize(int keySize) {
		this.keySize = keySize;
	}

	public int getValueSize() {
		return valueSize;
	}

	public void setValueSize(int valueSize) {
		this.valueSize = valueSize;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Access(Operation op, long key, int keySize, int valueSize, long timestamp) {
		super();
		this.op = op;
		this.key = key;
		this.keySize = keySize;
		this.valueSize = valueSize;
		this.timestamp = timestamp;
	}

	public Operation getOp() {
		return op;
	}

	public void setOp(Operation op) {
		this.op = op;
	}

}
