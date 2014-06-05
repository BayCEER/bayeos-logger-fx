package bayeos.logger.dump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import bayeos.logger.TaskController;
import frame.parser.DataReader;

public class ExportExcelTask extends Task<Boolean> {

	private Board board;
	private File file;
	private Connection con;
	private static final Logger log = Logger.getLogger(ExportExcelTask.class);

	public ExportExcelTask(Connection con, Board b, File file) {
		this.board = b;
		this.file = file;
		this.con = con;
	}

	@Override
	protected Boolean call() throws Exception {
		log.debug("Start export task");
		updateTitle("Export data to " + file.getAbsolutePath());

		SXSSFWorkbook wb = new SXSSFWorkbook(1000);
		Sheet sh = wb.createSheet(board.getName());

		long startTime = new Date().getTime();
		int rowNum = 0;

		updateProgress(0, board.getRecords());
		updateMessage(TaskController.getUpdateMsg("Export:", 0,	board.getRecords(), startTime));
		
		PreparedStatement st = null;
		FileOutputStream out = null;
		ResultSet rs = null;
		
		Calendar d = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+1"));		
		
		DataFormat df = wb.createDataFormat();		
		CellStyle cs = wb.createCellStyle();	
		cs.setDataFormat(df.getFormat("mm/dd/yyyy hh:mm:ss"));
		 
		
		try {
			st = con.prepareStatement("select value from frame where board_id = ? order by id");
			st.setInt(1, board.getId());
			rs = st.executeQuery();
			Cell cell = null;
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
				
				Row row = sh.createRow(rowNum);
				String s = rs.getString(1);
				
								
				// log.info("Row:" + IntArray.toStringList(Base64.decodeBase64(s)));
				
				DataReader reader = new DataReader();
				Map<String,Object> ret = reader.read(Base64.decodeBase64(s), board.getName(), new Date());
				
				Date resTime = (Date)ret.get("result_time");
				
				cell = row.createCell(0);												
				d.setTimeInMillis(resTime.getTime());
				cell.setCellValue(d.getTime());
				cell.setCellStyle(cs);
				
				Map<Integer, Float> values = (Map<Integer, Float>) ret.get("values");				
				for (Integer cha : values.keySet()) {
					cell = row.createCell(cha);
					cell.setCellValue(values.get(cha));
				}
				rowNum++;
				if (rowNum % 100 == 0) {
					updateMessage(TaskController.getUpdateMsg("Export:", rowNum, board.getRecords(), startTime));
					updateProgress(rowNum, board.getRecords());
				}
			}
			out = new FileOutputStream(file);
			wb.write(out);
			out.close();
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
			try {
				wb.dispose();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

	}

}
