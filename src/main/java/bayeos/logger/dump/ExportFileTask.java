package bayeos.logger.dump;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import bayeos.file.FrameFile;
import bayeos.frame.Parser;
import bayeos.logger.BulkReader;
import bayeos.logger.TaskController;
import javafx.concurrent.Task;

public class ExportFileTask extends Task<Boolean> {

	private DumpFile sFile;
	private FrameFile dFile;
	private static final Logger log = Logger.getLogger(ExportFileTask.class);

	public ExportFileTask(DumpFile source, FrameFile destination) {
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
		updateMessage(TaskController.getUpdateMsg("Export:", 0, sFile.length(), startTime));

		try {
			dFile.open();
			try (FileInputStream in = new FileInputStream(sFile)) {
				BulkReader reader = new BulkReader(in);
				byte[] data = null;
				long bytes = 0;
				while ((data = reader.readData()) != null) {
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

					try {
						dFile.writeFrame(Parser.parse(data, new Date(), sFile.getOrigin(), null));
					} catch (IOException e) {
						log.error("Failed to write values:" + e.getMessage());
						return false;
					}
					if (rowNum++ % 100 == 0) {
						updateProgress(bytes, sFile.length());
					}
				}
				updateProgress(bytes, sFile.length());
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			dFile.close();
		}

	}

}
