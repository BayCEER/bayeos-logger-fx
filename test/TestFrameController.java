
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import frame.channel.DataFrame;
import frame.BayEOS.Number;




public class TestFrameController {

	@Test
	public void testUpload() throws ClientProtocolException, IOException {		
		List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);		
		nvp.add(new BasicNameValuePair("sender", "bti3kb"));		
		nvp.add(new BasicNameValuePair("bayeosframes[]",new String(Base64.encodeBase64(new DataFrame<Float>().add(1.0F).getBytes(Number.Float32)))));
				
		HttpResponse res = Request.Post("http://localhost:8080/bayeos-gateway/frame/save")
		.addHeader("Authorization","Basic " + new String(Base64.encodeBase64(("import:xbee").getBytes())))	
		.bodyForm(nvp)
		.execute().returnResponse();										
		assertEquals(200,res.getStatusLine().getStatusCode());
				
	}
	
	@Test
	public void testWrongFrame() throws ClientProtocolException, IOException {		
		List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);		
			
		nvp.add(new BasicNameValuePair("bayeosframes[]","wrong"));
				
		HttpResponse res = Request.Post("http://localhost:8080/bayeos-gateway/frame/save")
		.addHeader("Authorization","Basic " + new String(Base64.encodeBase64(("import:xbee").getBytes())))	
		.bodyForm(nvp)
		.execute().returnResponse();										
		assertEquals(500,res.getStatusLine().getStatusCode());
				
	}

	
}
