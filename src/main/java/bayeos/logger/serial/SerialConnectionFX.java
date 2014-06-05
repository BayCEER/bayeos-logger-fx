package bayeos.logger.serial;

import java.io.InputStream;
import java.io.OutputStream;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import serial.SerialConnection;

public class SerialConnectionFX {
		
	private SerialConnection con;
	
	private BooleanProperty connected = new SimpleBooleanProperty(this, "connected");
	
	private StringProperty message = new SimpleStringProperty(this,"message");
	
	public final StringProperty messageProperty(){
		return message;
	}
	
	public final BooleanProperty connectedProperty() {
		return connected;
	}
				
	public boolean connect(String port, Integer baudRate, Integer timeout) {
		con = new SerialConnection(port,baudRate,timeout);
		
		connected.setValue(con.connect());
		try {
			// Give logger some millis to open connection 
			Thread.sleep(200);
		} catch (InterruptedException e) {		
		}
		message.set("Connection:" + port +  ":" + baudRate);
		return connected.get();
	}
	
	public void disconnect() {
		con.disconnect();
		message.set("");
		connected.setValue(false);
	}
	
	public InputStream getInputStream(){
		return con.getInputStream();
	}
	
	public OutputStream getOutputStream(){
		return con.getOutputStream();
	}
 	
}
