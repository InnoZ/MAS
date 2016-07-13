package innoz.run.parallelization;

public abstract class AlgoThread implements Runnable {

	public abstract void init(Object... args);

	abstract void addToThread(Object obj);
	
}
