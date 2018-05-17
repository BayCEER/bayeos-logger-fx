package bayeos.logger;

import java.util.Date;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

public class LoggerProperties {

	private FloatProperty version;
	private ObjectProperty<String> name;
	private ObjectProperty<String> samplingInterval;
	private ObjectProperty<Date> currentTime;
	private ObjectProperty<Date> nextTime;
	private ObjectProperty<Long> newRecords;
	private ObjectProperty<Boolean> batteryStatus;
	
	
	// version
	public final Float getVersion() {
		if (version != null) {
			return version.get();
		} else {
			return null;
		}
	}

	public final void setVersion(Float value) {
		this.version.set(value);
	}

	public final FloatProperty versionProperty() {
		if (version == null) {
			version = new SimpleFloatProperty();
		}
		return version;
	}

	// name
	public final String getName() {
		if (name != null) {
			return name.get();
		} else {
			return null;
		}
	}

	public final void setName(String value) {
		this.name.set(value);
	}

	public final ObjectProperty<String> nameProperty() {
		if (name == null) {
			name = new SimpleObjectProperty<String>();
		}
		return name;
	}

	// samplingIntervall
	public final String getSamplingInterval() {
		if (samplingInterval != null) {
			return samplingInterval.get();
		} else {
			return null;
		}
	}

	public final void setSamplingInterval(String value) {		
		this.samplingInterval.set(value);
	}

	public final ObjectProperty<String> samplingIntervalProperty() {
		if (samplingInterval == null) {
			samplingInterval = new SimpleObjectProperty<String>();
		}
		return samplingInterval;
	}

	// currentTime
	public final Date getCurrentTime() {
		if (currentTime != null) {
			return currentTime.get();
		} else {
			return null;
		}
	}
	
	public final void setCurrentTime(Date value) {
		this.currentTime.set(value);
	}

	public final ObjectProperty<Date> currentTimeProperty() {
		if (currentTime == null) {
			currentTime = new SimpleObjectProperty<Date>();
		}
		return currentTime;
	}

	// nextTime
	public final Date getNextTime() {
		if (nextTime != null) {
			return nextTime.get();
		} else {
			return null;
		}
	}

	public final void setNextTime(Date value) {
		this.nextTime.set(value);
	}

	public final ObjectProperty<Date> nextTimeProperty() {
		if (nextTime == null) {
			nextTime = new SimpleObjectProperty<Date>();
		}
		return nextTime;
	}

	// newRecords
	public final Long getNewRecords() {
		if (newRecords != null) {
			return newRecords.get();
		} else {
			return null;
		}
	}

	public final void setNewRecords(Long value) {
		this.newRecords.set(value);
	}

	public final ObjectProperty<Long> newRecordsProperty() {
		if (newRecords == null) {
			newRecords = new SimpleObjectProperty<Long>();
		}
		return newRecords;
	}

	// batteryStatus
	public final Boolean getBatteryStatus() {
		if (batteryStatus != null) {
			return batteryStatus.get();
		} else {
			return null;
		}
	}

	public final void setBatteryStatus(Boolean value) {
		this.batteryStatus.setValue(value);
	}

	public final ObjectProperty<Boolean> batteryStatusProperty() {
		if (batteryStatus == null) {
			batteryStatus = new SimpleObjectProperty<Boolean>();
		}
		return batteryStatus;
	}
	

}
