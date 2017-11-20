package bayeos.file;

import java.io.IOException;
import java.util.Map;

public interface FrameFile  {	
	public void writeFrame(Map<String, Object> values) throws IOException;
	void open() throws IOException;
	void close() throws IOException;
	String getPath();
}
