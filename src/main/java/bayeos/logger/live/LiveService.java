package bayeos.logger.live;


import bayeos.logger.Logger;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LiveService extends Service<Void> {
		
		private LiveDataTask task;
		private Logger logger;				
		private ListChangeListener<FrameData> listener;		
		
		
		public LiveService() {			
			
		}

		public void stop() {
			task.frameDataProperty().removeListener(listener);
			task.stop();			
		}
		
		
		@Override
		protected Task<Void> createTask() {
			task = new LiveDataTask(logger);
			task.frameDataProperty().addListener(listener);;
			return task;
		}

		public void setListener(ListChangeListener<FrameData> listener) {
				this.listener = listener;
		}
		
		public void setLogger(Logger logger) {
			this.logger = logger;
		}
		
		
		
	}