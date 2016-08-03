package com.innoz.toolbox.run.parallelization;

/**
 * 
 * Class that provides a framework for threads being executed multi-threaded.
 * 
 * @author dhosse
 *
 */
public abstract class AlgoThread implements Runnable {

	public abstract void init(Object... args);

	abstract void addToThread(Object obj);
	
}
