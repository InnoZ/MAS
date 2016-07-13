package innoz.run.parallelization;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

public class AbstractMultithreadedModule {

	private final static Logger log = Logger.getLogger(AbstractMultithreadedModule.class);
	
	protected int numberOfThreads;
	private Thread[] threads;
	protected AlgoThread[] algothreads;
	protected int count = 0;
	
	private final AtomicReference<Throwable> hadException = new AtomicReference<>(null);
	private final ExceptionHandler exceptionHandler = new ExceptionHandler(this.hadException);
	
	private String className;
	
	public AbstractMultithreadedModule(int numberOfThreads){
		
		this.numberOfThreads = numberOfThreads;
		
	}
	
	public final void initThreads(String className, Object... args){
		
		this.className = className;
		this.hadException.set(null);
		
		this.threads = new Thread[this.numberOfThreads];
		this.algothreads = new AlgoThread[this.numberOfThreads];
		
		for(int i = 0; i < this.numberOfThreads; i++){
			
			try {
			
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
	
	public final void handle(Object obj){
		
		this.algothreads[this.count % this.numberOfThreads].addToThread(obj);
		this.count++;
		
	}
	
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
