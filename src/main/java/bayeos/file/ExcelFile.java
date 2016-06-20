package bayeos.file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelFile implements SeriesFile {
	private int rowNum;	
	private static final Logger log = Logger.getLogger(ExcelFile.class);
	private String path;
	private Sheet sh;
	private CellStyle cs;
	private Workbook wb;
	private FileOutputStream out;
	
	private Calendar d = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+1"));	
	
	@Override
	public boolean open(String path) {
		this.path = path;
		try {
			out = new FileOutputStream(path);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			return false;
		}
		wb = new SXSSFWorkbook(1000);
		sh = wb.createSheet("Data");		
		DataFormat df = wb.createDataFormat();		
		cs = wb.createCellStyle();	
		cs.setDataFormat(df.getFormat("mm/dd/yyyy hh:mm:ss"));
		rowNum = 0;
		return true;
	}

	@Override
	public boolean close() {
		try {
			wb.write(out);
			((SXSSFWorkbook) wb).dispose();
			out.close();
		} catch (IOException e) {
			log.error(e.getMessage());
			return false;
		}		
		return true;
	}

	@Override
	public boolean writeRow(Date ts, Map<Integer, Float> values) {
		Row row = sh.createRow(rowNum);
		Cell cell = row.createCell(0);												
		d.setTimeInMillis(ts.getTime());
		cell.setCellValue(d.getTime());
		cell.setCellStyle(cs);		
						
		for (Integer cha : values.keySet()) {
			cell = row.createCell(cha);
			cell.setCellValue(values.get(cha));
		}
		rowNum++;
		return true;
	}

	@Override
	public String getPath() {
		return path;
	}

}
