package bayeos.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFrameFile extends AbstractFrameFile  {

	private ZipOutputStream zout;
	private File tempDir;
	private String lastOrigin = "";
	private String lastType = "";		
	private FileWriter fileWriter;
	
	public static final String MSG_TYPE = "msg";
	public static final String CSV_TYPE = "csv";
	public static final String SEP = ";";
	
	private static final SimpleDateFormat df;
	static {
		df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT+1"));
	}
		
	public ZipFrameFile(String path) {
		super(path);
	}
		
	@Override
	public void open() throws IOException {
		zout = new ZipOutputStream(new FileOutputStream(path));
		tempDir = Files.createTempDirectory("frames").toFile();
		tempDir.deleteOnExit();							
	}

	@Override
	public void close() throws IOException {
		if (fileWriter != null) {
			fileWriter.flush();
			fileWriter.close();
		}		
		List<File> files =  new ArrayList<File>();
		getAllFiles(tempDir, files);		
		for(File f:files) {								
				zout.putNextEntry(new ZipEntry(f.getName()));				
				Files.copy(f.toPath(), zout);
		}
		zout.close();						
	}

	
	
	private void setFileWriter(String origin, String type) throws IOException {				
		if (!(lastOrigin.equals(origin) && lastType.equals(type))){			
			if (fileWriter != null) {
				fileWriter.flush();
				fileWriter.close();
			}			
			String name = origin.replace("/", File.separator);
			Path p = Paths.get(tempDir.getAbsolutePath(), name + "." + type);
			p.getParent().toFile().mkdirs();			
			fileWriter = new FileWriter(p.toFile(),true);
			lastOrigin = origin;
			lastType = type;
		}
		
	}
	
	private void getAllFiles(File dir, List<File> fileList) throws IOException {
			File[] files = dir.listFiles();
			for (File file : files) {				
				if (file.isDirectory()) {
					getAllFiles(file, fileList);
				} else {
					fileList.add(file);
				}
			}					
	}
	

	@Override
	public void writeMessage(Map<String, Object> frame) throws IOException {
		String origin = (String)frame.get("origin");
		setFileWriter(origin,MSG_TYPE);		
		fileWriter.write(df.format(new Date(((long)(frame.get("ts"))/(1000*1000)))));
		fileWriter.write(SEP);
		fileWriter.write((String)frame.get("value"));
		fileWriter.write("\n");			
	}

	@Override
	public void writeDataFrame(Map<String, Object> frame) throws IOException {
		String origin = (String)frame.get("origin");
		setFileWriter(origin,CSV_TYPE);		
		Date ts = new Date(((long)(frame.get("ts"))/(1000*1000)));		
		fileWriter.write(df.format(ts));				
		Map<String, Number> values = (Map<String, Number>) frame.get("value");
		addColumnIndex(origin, values);	
		for(String e:getColumnIndexList(origin)) {
			fileWriter.write(SEP);
			Number n = values.get(e);
			if (n!=null) {
				fileWriter.write(n.toString());
			}
		}
		fileWriter.write("\n");			
	}

	
	
}
