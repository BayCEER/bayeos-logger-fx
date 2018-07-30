package bayeos.logger.dump;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import bayeos.logger.BulkReader;
import bayeos.logger.ProgressTask;
import de.unibayreuth.bayeos.connection.Connection;

@SuppressWarnings("restriction")
public class UploadDumpTask extends ProgressTask<Boolean> {

	private DumpFile df;
	private Connection con;

	private final static int FRAMES_PER_POST = 1000;

	private static final Logger log = Logger.getLogger(UploadDumpTask.class);

	public UploadDumpTask(DumpFile file, Map<String, Object> ret) {
		this.df = file;
		this.con = (Connection) ret.get("con");
	}

	@Override
	protected Boolean call() throws Exception {
		updateTitle("Uploading data to " + con.getURL());
		log.debug("Start upload task");

		SimpleHTTPClient client = null;
		FileInputStream in = null;
		try {
			client = new SimpleHTTPClient(con.getURL(), con.getUserName(), con.getPassword());
			updateProgress(0, df.getLength());

			in = new FileInputStream(df);
			BulkReader reader = new BulkReader(in);
			byte[] data = null;

			List<String> frames = new ArrayList<>(FRAMES_PER_POST);

			long bytes = 0;
			while ((data = reader.readData()) != null) {
				bytes += data.length;

				if (isCancelled()) {
					updateMessage("Cancelled");
					return false;
				}

				frames.add(Base64.encodeBase64String(data));
				if (frames.size() == FRAMES_PER_POST) {
					updateProgress(bytes, df.length());			
					client.postFrames(frames, df.getOrigin());
					frames.clear();
				}
			}

			if (!frames.isEmpty()) {
				client.postFrames(frames, df.getOrigin());
				updateProgress(bytes, df.length());				
				frames.clear();
			}

		} catch (IOException e) {
			log.error(e.getMessage());
			return false;

		} finally {
			if (in != null)
				in.close();
		}

		return true;
	}

}
