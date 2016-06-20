package bayeos.file;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class CSVFile implements SeriesFile {
	private static final Logger log = Logger.getLogger(CSVFile.class);
	private PrintWriter wout;
	private String path;;
	
	private static final SimpleDateFormat df;
	static {
		df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT+1"));
	}
	
	@Override
	public boolean open(String path) {		
		this.path = path;
		try {						
			wout = new PrintWriter(path);		
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			return false;
		}
		
		return true;
	}

	@Override
	public boolean close() {		
		wout.flush();
		wout.close();				
		return true;
	}

	@Override
	public boolean writeRow(Date ts, Map<Integer, Float> values) {		
			wout.print(df.format(ts));			
			Integer max = 0;
			for(Integer nr:values.keySet()){
				if (max<=nr) max=nr;
			}
			for (int i=1;i<=max;i++){
				wout.print(',');
				Float v = values.get(i);
				if (v!=null){
					wout.print(v);
				}				
			}
			wout.println();			
		return true;
	}

	@Override
	public String getPath() {
		return path;
	}
}
