package bayeos.logger.serial;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import javafx.concurrent.Task;
import serial.SerialConnection;
import serial.SerialLoggerFinder;
import bayeos.logger.MainApp;

public class FindPortTask extends Task<String> {

	int baudrate = 38400;
	
	
	private Preferences pref = Preferences.userNodeForPackage(MainApp.class);

	public FindPortTask(int baudrate) {
		this.baudrate = baudrate;
	}

	@Override
	protected String call() throws Exception {
		updateTitle("Searching for BayEOS Logger");		
		ArrayList<String> ports = new ArrayList<String>(10);		
		ports.addAll(SerialConnection.getAvailableSerialPorts());
		updateProgress(0, ports.size());
		String lastPort = pref.get("port",null);						
		if (lastPort!=null){			
			if (ports.contains(lastPort)){
				// Put port on first position
				ports.remove(lastPort);
				ports.add(0,lastPort);				
			} 
		}							
		int i=1;
		for(String port:ports){			
			if (isCancelled()) break;
			updateMessage(String.format("Port:%s",port));
			updateProgress(i++, ports.size());
			if (SerialLoggerFinder.isConnected(port, baudrate)){			
				return port;
			}
		}
		return null;
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		updateMessage("Done");
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		updateMessage("Cancelled");
	}

	@Override
	protected void failed() {
		super.failed();
		updateMessage("Failed");
	}

}
