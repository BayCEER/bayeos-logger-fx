package bayeos.logger.dump;

import static bayeos.logger.LoggerConstants.DM_FULL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import bayeos.logger.BulkWriter;
import bayeos.logger.ProgressTask;

public class DownloadFileTask extends ProgressTask<File> {
	private bayeos.logger.Logger logger;
	private File file;
	private static Logger log = Logger.getLogger(DownloadFileTask.class);

	public DownloadFileTask(bayeos.logger.Logger con, File file) {
		this.logger = con;
		this.file = file;
	}

	@Override
	protected File call() throws Exception {
		updateTitle(String.format("Dump %s to %s", logger.getName(), file.getAbsolutePath()));
	
		BufferedOutputStream bout = null;
		try {
			bout = new BufferedOutputStream(new FileOutputStream(file));
			BulkWriter bWriter = new BulkWriter(bout);

			long read = 0;
			long bytes = logger.startBulkData(DM_FULL);

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

				byte[] bulk = logger.readBulk();

				bWriter.write(bulk);
				read = read + bulk.length - 5;
				updateProgress((read > bytes) ? bytes : read, bytes);	
			}
			bout.flush();
			bout.close();
			logger.stopMode();

		} catch (IOException e) {
			log.error(e.getMessage());
			logger.breakSocket();
			logger.stopMode();

		} finally {
			if (bout != null)
				bout.close();
		}
		return null;
	}

}