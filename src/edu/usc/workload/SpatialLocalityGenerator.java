package edu.usc.workload;

/**
 * The generator models the spatial locality based on statistics published by
 * Facebook.
 * 
 * Berk Atikoglu, Yuehai Xu, Eitan Frachtenberg, Song Jiang, and Mike Paleczny.
 * 2012. Workload analysis of a large-scale key-value store. In Proceedings of
 * the 12th ACM SIGMETRICS/PERFORMANCE joint international conference on
 * Measurement and Modeling of Computer Systems (SIGMETRICS '12). ACM, New York,
 * NY, USA, 53-64. DOI=http://dx.doi.org/10.1145/2254756.2254766
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class SpatialLocalityGenerator {
	public double percentUniqueKeys(int hour) {
		return 0.207;
	}
}
