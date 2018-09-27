package edu.usc.distributions;

/**
 * A generator that is capable of generating numeric values.
 *
 * Copied from YCSB. https://github.com/brianfrankcooper/YCSB
 * 
 * @author YCSB contributors
 * 
 */
public abstract class NumberGenerator extends Generator<Number> {
	private Number lastVal;

	/**
	 * Set the last value generated. NumberGenerator subclasses must use this call
	 * to properly set the last value, or the {@link #lastValue()} calls won't work.
	 */
	protected void setLastValue(Number last) {
		lastVal = last;
	}

	@Override
	public Number lastValue() {
		return lastVal;
	}

	/**
	 * Return the expected value (mean) of the values this generator will return.
	 */
	public abstract double mean();
}
