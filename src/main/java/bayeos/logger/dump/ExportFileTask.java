package bayeos.logger.dump;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import javafx.concurrent.Task;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import bayeos.file.SeriesFile;
import bayeos.frame.Parser;
import bayeos.logger.TaskController;

public class ExportFileTask extends Task<Boolean> {

	private Board board;
	private SeriesFile file;
	private Connection con;
	private static final Logger log = Logger.getLogger(ExportFileTask.class);

	public ExportFileTask(Connection con, Board b, SeriesFile file) {
		this.board = b;
		this.file = file;
		this.con = con;
	}

	@Override
	protected Boolean call() throws Exception {
		log.debug("Start export task");
		updateTitle("Export data to " + file.getPath());

		long startTime = new Date().getTime();
		int rowNum = 0;

		updateProgress(0, board.getRecords());
		updateMessage(TaskController.getUpdateMsg("Export:", 0,	board.getRecords(), startTime));
		
		PreparedStatement st = null;
		ResultSet rs = null;		
		Calendar d = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+1"));		
				
		
		try {
			st = con.prepareStatement("select value from frame where board_id = ? order by id");
			st.setInt(1, board.getId());
			rs = st.executeQuery();
			while (rs.next()) {
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
				
				String s = rs.getString(1);			
				
				Map<String,Object> ret = Parser.parse(Base64.decodeBase64(s));
				Map<Integer, Float> values = (Map<Integer, Float>) ret.get("value");
				
				Date resTime = new Date(((long)(ret.get("ts"))/(1000*1000)));												
				if (resTime != null){
					if (!file.writeRow(resTime, values)){
						return false;						
					}
					
				}				
												
				rowNum++;
				if (rowNum % 100 == 0) {
					updateMessage(TaskController.getUpdateMsg("Export:", rowNum, board.getRecords(), startTime));
					updateProgress(rowNum, board.getRecords());
				}
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}						
		}

	}

}
