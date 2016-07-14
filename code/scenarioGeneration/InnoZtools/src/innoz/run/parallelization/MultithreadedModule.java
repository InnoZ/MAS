package innoz.run.parallelization;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

/**
 * 
 * Class that enables multi-threading. It holds an array of {@link Thread}, each invoking an {@link AlgoThread}.
 * This class was meant to provide a generic container that holds and handles Threads, thus it should not
 * be extended.</br>
 * 
 * The order of method calls for this class is:
 * <ol>
 * <li>Instantiate module
 * <li>Initialize threads
 * <li>Handle objects that should be processed
 * <li>Execute.
 * </ol>
 * 
 * @author dhosse
 *
 */
public final class MultithreadedModule {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private final static Logger log = Logger.getLogger(MultithreadedModule.class);
	
	private final AtomicReference<Throwable> hadException = new AtomicReference<>(null);
	private final ExceptionHandler exceptionHandler = new ExceptionHandler(this.hadException);
	
	protected final int numberOfThreads;
	/////////////////////////////////////////////////////////////////////////////////////////

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private Thread[] threads;
	protected AlgoThread[] algothreads;
	protected int count = 0;
	private String className;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Constructor that instantiates a new {@link MultithreadedModule} object.
	 * 
	 * @param numberOfThreads The number of threads that will be invoked by this module. Must be more than or equal one
	 * and less than the number of available (logical) cores.
	 */
	public MultithreadedModule(int numberOfThreads){
		
		this.numberOfThreads = numberOfThreads;
		
	}
	
	/**
	 * 
	 * Initializes as many threads and algo threads as were defined by numberOfThreads.
	 * 
	 * @param className The class name of the {@link AlgoThread} to be invoked.
	 * @param args The arguments needed for initialization of the AlgoThread(s).
	 */
	public final void initThreads(String className, Object... args){
		
		// Initialize the class name and the exception object
		this.className = className;
		this.hadException.set(null);
		
		// Create new arrays for threads and algothreads of the size of numberOfThreads
		this.threads = new Thread[this.numberOfThreads];
		this.algothreads = new AlgoThread[this.numberOfThreads];
		
		for(int i = 0; i < this.numberOfThreads; i++){
			
			try {

				// Add new instances of threads and algo threads to the array and add the exception handler
				AlgoThread algothread = (AlgoThread)Class.forName(className).newInstance();
				algothread.init(args);
				Thread thread = new Thread(algothread);
				thread.setUncaughtExceptionHandler(exceptionHandler);
				
				this.threads[i] = thread;
				this.algothreads[i] = algothread;
				
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

				e.printStackTrace();
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Assigns objects to the threads for further processing.
	 * 
	 * @param obj The object to be handled.
	 */
	public final void handle(Object obj){
		
		// Assign the object to a thread
		this.algothreads[this.count % this.numberOfThreads].addToThread(obj);
		this.count++;
		
	}
	
	/**
	 * 
	 * Starts the threads of this object. If all threads are finished, the execution automatically ends. 
	 * 
	 */
	public final void execute(){

		log.info("Starting " + this.threads.length + " threads for algo threads of type " + this.className);
		
		for(Thread thread : this.threads){
			
			thread.start();
			
		}
		
		try {

			for(Thread thread : this.threads){
			
				thread.join();
				
			}
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
			
		}
		
		log.info("all " + this.threads.length + " threads finished.");
		Throwable throwable = this.hadException.get();
		
		if (throwable != null) {
			
			throw new RuntimeException("Some threads crashed, thus not all objects may have been handled.", throwable);
			
		}
		
		this.algothreads = null;
		this.threads = null;
		this.count = 0;
		
	}
	
	final static class ExceptionHandler implements UncaughtExceptionHandler {

		private final AtomicReference<Throwable> hadException;

		public ExceptionHandler(final AtomicReference<Throwable> hadException) {
			this.hadException = hadException;
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			log.error("Thread " + t.getName() + " died with exception. Will stop after all threads finished.", e);
			this.hadException.set(e);
		}

	}
	
}
