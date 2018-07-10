package bayeos.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public abstract class AbstractFrameFile implements FrameFile {
	
	protected String path;
	protected Map<String, List<String>> indexMap;
	
	
	public AbstractFrameFile(String path) {
		this.path = path;
		this.indexMap = new Hashtable();
	}
		
	
	public void addColumnIndex(String origin, Map<String, Number> values) {
		if (values == null) return;
		ArrayList<String> sortList = new ArrayList<String>();		
		sortList.addAll(values.keySet());		
		Collections.sort(sortList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return extractInt(o1) - extractInt(o2);
			}			
			int extractInt(String s) {
	            String num = s.replaceAll("\\D", "");	         
	            return num.isEmpty() ? 0 : Integer.parseInt(num);
	        }			
		});
		
		for(String nr:sortList) {
			addColumnIndex(origin, nr);
		}
	}
		
	public void addColumnIndex(String origin, String nr) {		
		if (!indexMap.containsKey(origin)) {
			indexMap.put(origin, new ArrayList());			
		}				
		List<String> indexList = indexMap.get(origin);		
		if (!indexList.contains(nr)) {
			indexList.add(nr);			
		}
	}
	
	public List<String> getColumnIndexList(String origin){
		return indexMap.get(origin);		
	}
	
	@Override
	public String getPath() {
		return path;
		
	}
	
	
	
	@Override
	public void writeFrame(Map<String, Object> frame) throws IOException  {			
		if (frame.containsKey("type")) {
			switch ((String)frame.get("type")) {
			case "DataFrame":
				writeDataFrame(frame);				
				break;
			case "Message":
				writeMessage(frame);
			default:
				break;
			}								
		}
	} 

	public void writeMessage(Map<String, Object> frame) throws IOException {
				
	}


	public void writeDataFrame(Map<String, Object> frame) throws IOException {
		
	}


}
