package bayeos.logger.dump;

import static bayeos.logger.LoggerConstants.BC_SET_READ_TO_LAST_OF_BINARY_END_POS;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import bayeos.logger.BulkWriter;
import bayeos.logger.MainController;
import bayeos.logger.ProgressTask;


public class DownloadBulkTask extends ProgressTask<DumpFile> {
	bayeos.logger.Logger logger;
	byte mode;
	
	private static Logger log = Logger.getLogger(DownloadBulkTask.class);
	
	public DownloadBulkTask(bayeos.logger.Logger logger, byte mode) {
		this.logger = logger;
		this.mode = mode;
	}

	@Override
	protected DumpFile call() {		
		DumpFile df = null;
		try {

			log.debug("Start download bulk task");
			String name = logger.getName();
			
			updateTitle("Download data from " + name);

			df = new DumpFile(MainController.dumpFileDir.getPath(), name);
			
			long bytes = logger.startBulkData(mode);
			if (bytes == 0) {
				return null;
			}

			updateProgress(0, bytes);
			log.info("Downloading data to " + df.getAbsolutePath());			
			try (RandomAccessFile fout = new RandomAccessFile(df, "rw")) {
				BulkWriter bulkWriter = new BulkWriter(fout);				
				long read = 0;
				while (read < bytes) {					
					if (read % 100 == 0) {
						updateProgress(read, bytes);							
					}
					if (isCancelled()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException interrupted) {
							if (isCancelled()) {
								updateMessage("Cancelled");
							}
						}
						return df;
					}
					byte[] bulk = logger.readBulk();					
					read += bulk.length - 5;
					bulkWriter.write(bulk);
				}							
			}
			
			log.debug("Send buffer command.");
			logger.sendBufferCommand(BC_SET_READ_TO_LAST_OF_BINARY_END_POS);

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				logger.breakSocket();
				logger.stopMode();
			} catch (IOException e) {
				log.error(e);
			}
		}

		return df;

	}

}
