package bayeos.logger.live;

import java.util.Date;
import java.util.Map;

public class FrameData {

	Date ts;
	String origin;
	Map<String, Number> values;


	public FrameData(Date ts, String origin, Map<String, Number> values) {
		super();
		this.ts = ts;
		this.origin = origin;
		
		
		this.values = values;
	}



	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Map<String, Number> getValues() {
		return values;
	}

	public void setValues(Map<String, Number> values) {
		this.values = values;
	}

	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		return b.append("Origin:").append(origin).append(" Time:").append(ts).append(" Value:").append(values).toString();
	}

}
