package bayeos.logger.dump;

import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import bayeos.file.SeriesFile;
import bayeos.frame.Parser;
import bayeos.logger.BulkReader;
import bayeos.logger.TaskController;
import javafx.concurrent.Task;

public class ExportFileTask extends Task<Boolean> {

	private DumpFile sFile;
	private SeriesFile dFile;
	private static final Logger log = Logger.getLogger(ExportFileTask.class);

	public ExportFileTask(DumpFile source, SeriesFile destination) {
		this.sFile = source;
		this.dFile = destination;		
	}

	@Override
	protected Boolean call() throws Exception {
		log.debug("Start export task");
		updateTitle("Export " + sFile.getAbsolutePath() + " to " + dFile.getPath());

		long startTime = new Date().getTime();
		int rowNum = 0;

		updateProgress(0, sFile.length());
		updateMessage(TaskController.getUpdateMsg("Export:", 0,	sFile.length(), startTime));
		

		try (FileInputStream in = new FileInputStream(sFile)){
			BulkReader reader = new BulkReader(in);
			byte[] data = null;	
			long bytes = 0;
			while ( (data = reader.readData()) != null) {
				bytes += data.length;
				if (isCancelled()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException interrupted) {
						if (isCancelled()) {
							updateMessage("Cancelled");
						}
					}
					return false;
				}
															
				Map<String,Object> ret = Parser.parse(data);								
				Map<String, Float> rowValues = (Map<String, Float>) ret.get("value");				
				Map<Integer,Float> row = new HashMap<>();
				for(Map.Entry<String, Float> e: rowValues.entrySet()) {
					row.put(Integer.valueOf(e.getKey()), e.getValue());
				}
								
				
				Date resTime = new Date(((long)(ret.get("ts"))/(1000*1000)));												
				if (resTime != null){
					if (!dFile.writeRow(resTime, row)){
						log.error("Failed to write values:" + row);
						return false;						
					}					
				}				
																
				if (rowNum++ % 100 == 0) {					
					updateProgress(bytes,sFile.length());
				}
			}
			updateProgress(bytes,sFile.length());
			return true;
		}
		
	} 

}
