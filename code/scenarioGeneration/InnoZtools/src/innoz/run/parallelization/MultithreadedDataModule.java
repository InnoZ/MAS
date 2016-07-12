package innoz.run.parallelization;

import innoz.config.Configuration;
import innoz.io.database.DatabaseReader;
import innoz.io.database.datasets.OsmDataset;

public class MultithreadedDataModule extends AbstractMultithreadedModule {

	final DatabaseReader reader;
	
	public MultithreadedDataModule(final DatabaseReader reader, final Configuration configuration){
		
		super(configuration.getNumberOfThreads());
		this.reader = reader;
	
	}
	
	@Override
	public final void handle(Object dataset){
		
		((DataProcessingAlgoThread)this.algothreads[this.count % this.numberOfThreads]).
			addDatasetToThread((OsmDataset)dataset);
		this.count++;
		
	}
	
}