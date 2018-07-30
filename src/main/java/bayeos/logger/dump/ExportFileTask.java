package bayeos.logger.dump;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import bayeos.file.FrameFile;
import bayeos.frame.Parser;
import bayeos.logger.BulkReader;
import bayeos.logger.ProgressTask;

public class ExportFileTask extends ProgressTask<Boolean> {

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
		
		int rowNum = 0;

		updateProgress(0, sFile.length());
		
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
						Map<String, Object> f = Parser.parse(data, new Date(), sFile.getOrigin(), null);
						if (f!=null) {
							dFile.writeFrame(f);	
						} else {
							log.warn("Failed to parse frame:" + data);
						}
														
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
