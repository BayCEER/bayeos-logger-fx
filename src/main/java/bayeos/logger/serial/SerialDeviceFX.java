package bayeos.logger.serial;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import bayeos.serialdevice.ISerialDevice;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

public class SerialDeviceFX implements ISerialDevice {
	
	private static final Logger log = Logger.getLogger(SerialDeviceFX.class);

	private int timeout = 2000;
		
	private SerialPort port;
	
	private BooleanProperty connected = new SimpleBooleanProperty(this, "connected");
	private StringProperty message = new SimpleStringProperty(this,"message");
	
	
	private int available;
			
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public final StringProperty messageProperty(){
		return message;
	}
	
	public final BooleanProperty connectedProperty() {
		return connected;
	}
				
	public boolean connect(String portName) {
				
		port = new SerialPort(portName);
		
		try {
			port.openPort();
			
			port.setParams(SerialPort.BAUDRATE_38400,
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE);
			
			port.addEventListener(new SerialPortEventListener() {
				@Override
				public void serialEvent(SerialPortEvent event) {
						if (event.getEventType() == SerialPortEvent.RXCHAR) {							
							available = event.getEventValue();
							log.debug("Available:" + available);
						}
					
				}
			});
			
		} catch (SerialPortException e1) {
			log.error(e1.getMessage());
			return false;
		}
		
				
		connected.setValue(port.isOpened());
		
		try {
			// Give logger some millis to open connection 
			Thread.sleep(200);
		} catch (InterruptedException e) {		
		}
							
		message.set("Connection:" + port.getPortName() );
		return connected.get();
	}
	
	public void disconnect() {
		try {
			port.closePort();
		} catch (SerialPortException e) {
			log.error(e.getMessage());
		};
		message.set("");
		connected.setValue(false);
	}

	public Set<String> getPortNames() {
		HashSet<String> r = new HashSet<String>();
		String[] ports = SerialPortList.getPortNames();
		for (int i = 0; i < ports.length; i++) {
			r.add(ports[i]);
		}
		return r;
	}

	@Override
	public int read() throws IOException {		
		try {
			byte[] b = port.readBytes(1,timeout);
			if (available> 0) {
				available--;
			} 			
			return 0xff & b[0];			
		} catch (SerialPortException | SerialPortTimeoutException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void write(byte[] data) throws IOException {
			try {
				port.writeBytes(data);
			} catch (SerialPortException e) {
				throw new IOException(e.getMessage());
			}
		
	}

	
	
	
 	
}
