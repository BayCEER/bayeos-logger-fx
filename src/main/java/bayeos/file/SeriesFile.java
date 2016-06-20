package bayeos.file;

import java.util.Date;
import java.util.Map;

public interface SeriesFile {
	public boolean open(String path);
	public boolean close();
	public boolean writeRow(Date ts, Map<Integer, Float> values);
	public String getPath();
}
