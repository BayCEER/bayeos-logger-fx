package bayeos.logger.live;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import bayeos.frame.FrameParserException;
import bayeos.frame.Parser;
import bayeos.logger.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class LiveDataTask extends Task<Void> {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LiveDataTask.class);
	private boolean active = true;

	private bayeos.logger.Logger logger;

	private SimpleListProperty<FrameData> frameData = new SimpleListProperty<FrameData>(this, "frameData",
			FXCollections.observableArrayList());

	public final ObservableList<FrameData> getFrameData() {
		return frameData.get();
	}

	public final ReadOnlyListProperty<FrameData> frameDataProperty() {
		return frameData;
	}

	public LiveDataTask(Logger logger) {
		this.logger = logger;
	}

	public void stop() {
		active = false;
	}

	@Override
	public Void call() {
		try {
			String loggerName = logger.getName();
			logger.startLiveData();
			while (active) {
				try {
				byte[] data = logger.readData();
				
				if (data != null) {
					Map<String, Object> frame = Parser.parse(data, new Date(), loggerName, null);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							String origin = frame.get("origin").toString();
							switch ((String) frame.get("type")) {
							case "DataFrame":
								Date ts = new Date(((long) (frame.get("ts")) / (1000 * 1000)));
								frameData.get()
										.add(new FrameData(ts, origin, (Map<String, Number>) frame.get("value")));
								break;
							case "Message":
								log.info("Origin: " + origin + " Message:" + frame.get("value").toString());
							default:
								break;
							}
						}
					});				
				} else {
					Thread.sleep(500);
				}
				
				} catch (IOException e) {
					log.debug(e.getMessage());
				}
			}
		} catch (IOException| FrameParserException| InterruptedException e) {
			log.error(e.getMessage());		
		} finally {
			try {
				logger.stopLiveData();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}
}