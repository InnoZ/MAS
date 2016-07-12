package innoz.io.database;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import innoz.config.Configuration;
import innoz.io.database.datasets.OsmDataset;

public class MultithreadedDataModule {

	private static final Logger log = Logger.getLogger(MultithreadedDataModule.class);
	
	private Thread[] threads;
	private AlgoThread[] algothreads;
	private int count = 0;
	private final int numberOfThreads;
	private final DatabaseReader reader;
	
	private final AtomicReference<Throwable> hadException = new AtomicReference<>(null);
	private final ExceptionHandler exceptionHandler = new ExceptionHandler(this.hadException);
	
	public MultithreadedDataModule(final DatabaseReader reader, final Configuration configuration){
		this.reader = reader;
		this.numberOfThreads = configuration.getNumberOfThreads();
	}
	
	public void initThreads(String key){
		
		this.hadException.set(null);
		
		this.threads = new Thread[this.numberOfThreads];
		this.algothreads = new AlgoThread[this.numberOfThreads];
		
		for(int i = 0; i < this.numberOfThreads; i++){
			
			AlgoThread algothread = new AlgoThread(this.reader, key);
			Thread thread = new Thread(algothread);
			thread.setUncaughtExceptionHandler(exceptionHandler);
			
			this.threads[i] = thread;
			this.algothreads[i] = algothread;
			
		}
		
	}
	
	public final void handle(OsmDataset dataset){
		
		this.algothreads[this.count % this.numberOfThreads].addDatasetToThread(dataset);
		this.count++;
		
	}
	
	public final void execute(){
		
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
		log.info("[" + this.getClass().getName() + "] all " + this.threads.length + " threads finished.");
		Throwable throwable = this.hadException.get();
		if (throwable != null) {
			throw new RuntimeException("Some threads crashed, thus not all plans may have been handled.", throwable);
		}
		
		this.algothreads = null;
		this.threads = null;
		this.count = 0;
		
	}
	
	private final static class ExceptionHandler implements UncaughtExceptionHandler {

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