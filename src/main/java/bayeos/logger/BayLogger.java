
package bayeos.logger;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BayLogger {
	StringProperty version  = new SimpleStringProperty();	
	StringProperty name = new SimpleStringProperty();	
	StringProperty port = new SimpleStringProperty();
	IntegerProperty baudRate = new SimpleIntegerProperty();	
	ObjectProperty<Date> dateOfNextFrame = new SimpleObjectProperty<Date>(); 
	ObjectProperty<Date> currentDate = new SimpleObjectProperty<Date>();
	IntegerProperty interval = new SimpleIntegerProperty();	
	
	public String getVersion() {
		return version.get();
	}

	public void setVersion(String version) {
		this.version.set(version);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getPort() {
		return port.get();
	}

	public void setPort(String port) {
		this.port.set(port);
	}

	public Integer getBaudRate() {
		return baudRate.get();
	}

	public void setBaudRate(Integer baudRate) {
		this.baudRate.set(baudRate);
	}

	public Date getDateOfNextFrame() {
		return dateOfNextFrame.get();
	}

	public void setDateOfNextFrame(Date dateOfNextFrame) {
		this.dateOfNextFrame.set(dateOfNextFrame);
	}

	public Date getCurrentDate() {
		return currentDate.get();
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate.set(currentDate);
	}

	public Integer getInterval() {
		return interval.get();
	}

	public void setInterval(Integer interval) {
		this.interval.set(interval);
	}


	public StringProperty versionProperty(){
		return version;
	}
	
	public StringProperty nameProperty(){
		return name;
	}
	
	public StringProperty portProperty(){
		return port;
	}
	
	public IntegerProperty baudRateProperty(){
		return baudRate;
	}
	
	
	public ObjectProperty<Date> dateOfNextFrameProperty(){
		return dateOfNextFrame;
	}
	
	public ObjectProperty<Date> currentDateProperty(){
		return currentDate;
	}
	
	public IntegerProperty intervalProperty(){
		return interval;
	}
	
	
			
	public boolean getTimeShift(Date c, int max_shift) {
		if (Math.abs(currentDate.get().getTime() - c.getTime())>max_shift*1000){
			return true;
		} else {
			return false;	
		}		
	}
	
	
	
}
