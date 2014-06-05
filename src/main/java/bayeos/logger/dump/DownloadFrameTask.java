package bayeos.logger.dump;

import java.util.Date;
import java.util.Map;

import javafx.concurrent.Task;
import logger.DataMode;
import logger.LoggerConnection;
import logger.StopMode;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import bayeos.logger.TaskController;
import binary.IntArray;
import frame.parser.DataReader;

public class DownloadFrameTask extends Task<Board> {

	LoggerConnection logCon;
	DataMode mode;

	private static Logger log = Logger.getLogger(DownloadFrameTask.class);

	public DownloadFrameTask(LoggerConnection con, DataMode mode) {
		this.logCon = con;
		this.mode = mode;
	}

	@Override
	protected Board call() throws Exception {
		
		Board board = null;
		
		
		
		
		try {			
			Date startDate = null;
			Date endDate = null;
			Integer frames = 0;
			
			log.debug("Start download frame task");
			updateTitle("Download data from " + logCon.getName());
			long startTime = new Date().getTime();
			board = new Board(logCon.getName());
			
			long bytes = logCon.startData(mode);
			if (bytes == 0) {
				return null;
			}
			updateProgress(0, bytes);
			
			board.Id = DAO.getBoardDAO().add(board);
			board.Records = 0;	
			
			long read = 0;						
			while (read < bytes) {
				if (isCancelled()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException interrupted) {
						if (isCancelled()) {
							updateMessage("Cancelled");
						}
					}
					return board;
				}
					
				byte[] b = IntArray.toByteArray(logCon.readData(),0);				
				read = read + b.length;
				
				DataReader reader = new DataReader();				
				Map<String,Object> ret = reader.read(b,"COM",new Date());
				if (ret.get("values") != null) {					
					Date rs = (Date) ret.get("result_time");
					if (frames == 0) {
						startDate = rs; endDate = rs;
					}					
					if (rs.before(startDate)) {
						startDate = rs;
					}					
					if (rs.after(endDate)) {
						endDate = rs;
					}					
				}
																																																																		
				updateProgress(read, bytes);
				if (frames % 100 == 0) {
					updateMessage(TaskController.getUpdateMsg("Frame Download",	read, bytes, startTime));
				}

				
				// Save it
				DAO.getFrameDAO().addFrame(Base64.encodeBase64String(b), board.Id);				

			}
			board.End = endDate;
			board.Start = startDate;
			board.Records = frames;
			DAO.getBoardDAO().update(board);
			logCon.stopData(StopMode.STOP);
			return board;

		} catch (Exception e) {
			log.error(e.getMessage());
			logCon.stopData(StopMode.CANCEL);
			return board;
		}
	}
}
