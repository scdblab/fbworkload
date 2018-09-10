package edu.usc.facebook;

import edu.usc.base.Access;

public interface TraceRequestProcessor {

	public void access(Access access);
	
	public String outputStats();
	
	public String finalStats();
}
