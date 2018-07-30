package bayeos.logger;

import java.util.Date;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public abstract class ProgressTask<V> extends Task<V> {
	
    long startTime;
    
    public ProgressTask() {
    	super();
    	this.setOnRunning(new EventHandler<WorkerStateEvent>() {			
			@Override
			public void handle(WorkerStateEvent event) {					
				startTime = new Date().getTime();
			}
		});
	}
    
        
	@Override
	protected void updateProgress(long workDone, long max) {
		super.updateProgress(workDone, max);	
		updateRemaining(workDone,max);	
	}
	
	@Override
	protected void updateProgress(double workDone, double max) {
		super.updateProgress(workDone, max);
		updateRemaining(workDone,max);
	}

	
	private void updateRemaining(Number workDone, Number max) {
		
		if (workDone.floatValue() > 0.0) {
			int per = Math.round(workDone.floatValue() /  max.floatValue() * 100);		
			long millis = Math
					.round(((new Date().getTime() - startTime) / workDone.floatValue())
							* (max.floatValue() - workDone.floatValue()));
			int h = (int) ((millis / 1000) / 3600);
			int m = (int) (((millis / 1000) / 60) % 60);
			int s = (int) ((millis / 1000) % 60);
			updateMessage(String.format("%d%% done (%02d:%02d:%02d remaining)", per, h, m, s));	
		}
				
		
	}
	

}
