package bayeos.logger.dump;

import static bayeos.logger.LoggerConstants.BC_SET_READ_TO_LAST_OF_BINARY_END_POS;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import bayeos.logger.BulkWriter;
import bayeos.logger.MainController;
import bayeos.logger.TaskController;
import javafx.concurrent.Task;


public class DownloadBulkTask extends Task<DumpFile> {
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
			long startTime = new Date().getTime();
			df = new DumpFile(MainController.dumpFileDir.getPath(), name);
			
			long bytes = logger.startBulkData(mode);
			if (bytes == 0) {
				return null;
			}

			updateProgress(0, bytes);
			log.info("Downloading data to " + df.getAbsolutePath());
			try (BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(df))) {
				BulkWriter bulkWriter = new BulkWriter(fout);				
				long read = 0;
				while (read < bytes) {
					updateProgress(read, bytes);
					if (read % 100 == 0) {
						updateMessage(TaskController.getUpdateMsg("Bulk download:", read, bytes, startTime));
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
				fout.flush();				
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
