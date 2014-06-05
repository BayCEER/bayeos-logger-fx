package bayeos.logger.dump;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javafx.concurrent.Task;
import logger.BufferCommand;
import logger.BulkWriter;
import logger.DataMode;
import logger.LoggerFileReader;
import logger.LoggerConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import bayeos.logger.TaskController;


public class DownloadBulkTask extends Task<Board> {
	LoggerConnection logCon;
	DataMode mode;
	
	private static Logger log = Logger.getLogger(DownloadBulkTask.class);
	
	public DownloadBulkTask(LoggerConnection con, DataMode mode) {
		this.logCon = con;
		this.mode = mode;
	}

	@Override
	protected Board call()  {
		BufferedOutputStream bulkOut = null;
		BufferedInputStream bulkIn = null;
		Board board = null;
		try {
		
		log.debug("Start download bulk task");
		updateTitle("Download data from " + logCon.getName());
		
		long startTime = new Date().getTime();
		board = new Board(logCon.getName());
		
	

			long read = 0;
			long bytes = logCon.startBulkData(mode);
			if (bytes == 0) {
				return null;
			}
			
			board.Id = DAO.getBoardDAO().add(board);
			board.Records = 0;				
			updateProgress(0, bytes);
			
			// Download of bulks in temporary file
			File file = File.createTempFile("BAYEOS", ".DB");
			file.deleteOnExit();
			
			log.info("Downloading data to " + file.getAbsolutePath());
			bulkOut = new BufferedOutputStream(new FileOutputStream(file));
			BulkWriter bulkWriter = new BulkWriter(bulkOut);
			long bulkBytes = 0;
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
					return board;					
				}
				int[] bulk = logCon.readBulk();
				bulkBytes=bulkBytes+bulk.length;
				read = read + bulk.length-5;					
				bulkWriter.write(bulk);						
			}
			bulkOut.flush();
			bulkOut.close();
							
			
			Float secs =(new Date().getTime() - startTime)/1000F;					
			log.info(String.format("Bulk download performance: %d [Bytes/sec]", Math.round(bulkBytes/secs)));					
			
			
			bulkIn = new BufferedInputStream(new FileInputStream(file));
			LoggerFileReader fileReader = new LoggerFileReader(bulkIn);
						
			int frameCount = 0;
			startTime = new Date().getTime();
			byte[] da;
			while ((da = fileReader.readData()) != null) {					
				// log.debug(String.format("Write:%d\tTotal:%d",frameReader.getBytesRead(),bytes));
				updateProgress(fileReader.getBytesRead(bytes),bytes);
				updateMessage(TaskController.getUpdateMsg("Bulk import", fileReader.getBytesRead(bytes), bytes, startTime));
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
				DAO.getFrameDAO().addFrame(Base64.encodeBase64String(da), board.Id);
				frameCount++;
			}
			board.Start = fileReader.getMinStart();
			board.End = fileReader.getMaxStart();
			board.Records = frameCount;
			DAO.getBoardDAO().update(board);

			logCon.sendBufferCommand(BufferCommand.SET_READ_TO_LAST_OF_BINARY_END_POS);
							
			return board;
		
		} catch (Exception e) {
			log.error(e.getMessage());
			return board;
		} finally {
			try {
				logCon.breakSocket(); logCon.stopMode();} catch (IOException e) {log.error(e);}
			try { if (bulkOut != null) bulkOut.close();	} catch (IOException e) {log.error(e);}
			try { if (bulkIn != null) bulkIn.close();} catch (IOException e) {log.error(e);}
			
		}

	}

}
