package bayeos.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelFile extends AbstractFrameFile {

	private Sheet sh;
	private CellStyle csTime;
	private SXSSFWorkbook wb;
	private FileOutputStream out;

	private String lastOrigin = "";
	private Map<String, Integer> lastRows;
	private Integer lastRow;

	private Calendar d = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+1"));

	public ExcelFile(String path) {
		super(path);
	}

	@Override
	public void open() throws IOException {
		out = new FileOutputStream(path);
		wb = new SXSSFWorkbook(1000);
		DataFormat df = wb.createDataFormat();
		csTime = wb.createCellStyle();
		csTime.setDataFormat(df.getFormat("dd/mm/yyyy hh:mm:ss"));
		lastRows = new Hashtable<>();
	}

	@Override
	public void close() throws IOException {
		wb.write(out);
		out.close();
		wb.dispose();
	}

	@Override
	public void writeDataFrame(Map<String, Object> frame) throws IOException {
		String origin = (String) frame.get("origin");	
		String sheetName = Paths.get(origin.replace("/",File.separator)).getFileName().toString();
		if (!lastOrigin.equals(origin)) {			
			// Switch to or create a new sheet
			Sheet s = wb.getSheet(sheetName);
			if (s != null) {
				sh = s;
				lastRow = lastRows.get(origin);
			} else {
				sh = wb.createSheet(sheetName);				
				lastRow = 0;
			}
			lastOrigin = origin;
		}

		Row row = sh.createRow(lastRow++);
		Cell cell = row.createCell(0);
		d.setTimeInMillis(new Date(((long) (frame.get("ts")) / (1000 * 1000))).getTime());
		cell.setCellValue(d.getTime());
		cell.setCellStyle(csTime);
		Map<String, Number> values = (Map<String, Number>) frame.get("value");
		addColumnIndex(origin, values);
		List<String> indexList = getColumnIndexList(origin);
		int index = 1;
		
		if (values != null) {
			for (String nr : indexList) {
				Number n = values.get(nr);
				if (n != null) {
					Cell c = row.createCell(index++, CellType.NUMERIC);
					c.setCellValue(n.doubleValue());
				}
			}	
		}
		
		lastRows.put(origin, lastRow);
	}

}
