package innoz.io.database;

public class MultithreadedDataModule {

	private Thread[] threads;
	private AlgoThread[] algothreads;
	private int count = 0;
	private final int numberOfThreads = 2;
	private final DatabaseReader reader;
	
	public MultithreadedDataModule(final DatabaseReader reader){
		this.reader = reader;
	}
	
	public void initThreads(String key){
		
		this.threads = new Thread[this.numberOfThreads];
		this.algothreads = new AlgoThread[this.numberOfThreads];
		
		for(int i = 0; i < 2; i++){
			
			AlgoThread algothread = new AlgoThread(this.reader, key);
			Thread thread = new Thread(algothread);
			this.threads[i] = thread;
			this.algothreads[i] = algothread;
			
		}
		
	}
	
	public final void handle(OsmPolygonDataset dataset){
		
		this.algothreads[this.count % this.numberOfThreads].addDatasetToThread(dataset);
		
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
		
		this.algothreads = null;
		this.threads = null;
		this.count = 0;
		
	}
	
}