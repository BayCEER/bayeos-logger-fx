package bayeos.logger.dump;

import java.util.Date;


import java.util.Map;

import javafx.concurrent.Task;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import bayeos.frame.Parser;
import bayeos.logger.TaskController;
import static bayeos.logger.LoggerConstants.*;

public class DownloadFrameTask extends Task<Board> {

	bayeos.logger.Logger logger;
	byte mode;
	

	private static Logger log = Logger.getLogger(DownloadFrameTask.class);

	public DownloadFrameTask(bayeos.logger.Logger logger, byte mode) {
		this.logger = logger;
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
			String name = logger.getName();
			updateTitle("Download data from " + name);
			long startTime = new Date().getTime();
			board = new Board(name);
			
			long bytes = logger.startData(mode);
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
					
				byte[] b = logger.readData();				
				read = read + b.length;
				
								
				Map<String,Object> ret = Parser.parse(b);
				if (ret.get("value") != null) {
					
					Date rs = new Date(((long)(ret.get("ts"))/(1000*1000)));					
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
			logger.stopData(SM_STOP);
			return board;

		} catch (Exception e) {
			log.error(e.getMessage());
			logger.stopData(SM_CANCEL);
			return board;
		}
	}
}
