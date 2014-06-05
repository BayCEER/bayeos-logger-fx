package bayeos.logger.dump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class SimpleHTTPClient {
	
	Logger log = Logger.getLogger(SimpleHTTPClient.class);
	private String url;
	private String user;
	private String password;	
	
					
	public SimpleHTTPClient(String url, String user, String password) {	
				this.url = url;
				this.user = user;
				this.password = password;
	}
					
	public ReturnCode postFrames(List<String>frames, String sender) throws IOException{						
		if (frames.size()==0) {
			return ReturnCode.OK;
		}
						
		List<NameValuePair> nvp = new ArrayList<NameValuePair>(frames.size() + 1);			
		if (sender!=null && sender.length()>0){				
			nvp.add(new BasicNameValuePair("sender", sender));	
		}			
		for(int i=0;i<frames.size();i++) {
			nvp.add(new BasicNameValuePair("bayeosframes[]",frames.get(i)));			
		}
		
		HttpResponse res = Request.Post(url)
				.addHeader("Authorization","Basic " + new String(Base64.encodeBase64((user + ":" + password).getBytes())))
				.bodyForm(nvp)
				.execute()
				.returnResponse();
		
		return new ReturnCode(res.getStatusLine().getStatusCode(),EntityUtils.toString(res.getEntity()));	
	}
	
		
	
}
