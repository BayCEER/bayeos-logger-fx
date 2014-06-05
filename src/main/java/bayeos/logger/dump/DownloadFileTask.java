package bayeos.logger.dump;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javafx.concurrent.Task;
import logger.BulkWriter;
import logger.DataMode;
import logger.LoggerConnection;

import org.apache.log4j.Logger;

public class DownloadFileTask extends Task {
	private LoggerConnection logCon;
	private File file;
	private static Logger log = Logger.getLogger(DownloadFileTask.class);
	
	public DownloadFileTask(LoggerConnection con, File file) {
		this.logCon = con;
		this.file = file;
	}

		@Override
		protected Object call() throws Exception {			
			updateTitle(String.format("Dump %s to %s",logCon.getName(),file.getAbsolutePath())); 
			long startTime = new Date().getTime();
			BufferedOutputStream bout = null;
			try {
				bout = new BufferedOutputStream(new FileOutputStream(file));
				BulkWriter bWriter = new BulkWriter(bout);

				long read = 0;
				long bytes = logCon.startBulkData(DataMode.FULL);
				long bulks = 0;

				while (read < bytes) {
					if (isCancelled()) {
						try {
			                Thread.sleep(100);
			            } catch (InterruptedException interrupted) {
			                if (isCancelled()) {
			                    updateMessage("Cancelled");	                    
			                }
			            }						
						return null;						
					}

					int[] bulk = logCon.readBulk();
					bulks++;
					bWriter.write(bulk);
					read = read + bulk.length - 5;

					updateProgress((read>bytes)?bytes:read, bytes);

					if (bulks % 10 == 0) {
						int per = Math.round(read / (float) bytes * 100);
						long millis = Math
								.round(((new Date().getTime() - startTime) / (float) read)
										* (bytes - read));
						int h = (int) ((millis / 1000) / 3600);
						int m = (int) (((millis / 1000) / 60) % 60);
						int s = (int) ((millis / 1000) % 60);

						updateMessage(String.format(
								"%d%% read (%02d:%02d:%02d remaining)", per, h,
								m, s));
					}
				}
				bout.flush();
				bout.close();
				
				logCon.stopMode();

			} catch (IOException e) {
				log.error(e.getMessage());
				logCon.breakSocket();
				logCon.stopMode();

			} finally {
				if (bout != null)
					bout.close();
			}
			return null;
		}

	}