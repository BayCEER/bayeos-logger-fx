package bayeos.logger.dump;

import static bayeos.logger.LoggerConstants.DM_FULL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.log4j.Logger;
import bayeos.logger.BulkWriter;
import javafx.concurrent.Task;

public class DownloadFileTask extends Task {
	private bayeos.logger.Logger logger;
	private File file;
	private static Logger log = Logger.getLogger(DownloadFileTask.class);

	public DownloadFileTask(bayeos.logger.Logger con, File file) {
		this.logger = con;
		this.file = file;
	}

	@Override
	protected Object call() throws Exception {
		updateTitle(String.format("Dump %s to %s", logger.getName(), file.getAbsolutePath()));
		long startTime = new Date().getTime();
		BufferedOutputStream bout = null;
		try {
			bout = new BufferedOutputStream(new FileOutputStream(file));
			BulkWriter bWriter = new BulkWriter(bout);

			long read = 0;
			long bytes = logger.startBulkData(DM_FULL);
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

				byte[] bulk = logger.readBulk();
				bulks++;
				bWriter.write(bulk);
				read = read + bulk.length - 5;

				updateProgress((read > bytes) ? bytes : read, bytes);

				if (bulks % 10 == 0) {
					int per = Math.round(read / (float) bytes * 100);
					long millis = Math.round(((new Date().getTime() - startTime) / (float) read) * (bytes - read));
					int h = (int) ((millis / 1000) / 3600);
					int m = (int) (((millis / 1000) / 60) % 60);
					int s = (int) ((millis / 1000) % 60);

					updateMessage(String.format("%d%% read (%02d:%02d:%02d remaining)", per, h, m, s));
				}
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