package bayeos.logger;


import java.util.Date;
import java.util.prefs.Preferences;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class TaskController  {

	private Preferences pref = Preferences.userNodeForPackage(MainApp.class);
	private Stage stage;

	@FXML
	private Button btnCancel;
	
	@FXML
	private Label lblProgress;
	
	@FXML
	private ProgressBar progBar;
	
	private Task task;
		
	@FXML
	public void cancelTask(ActionEvent event) {			
		task.cancel();			
	}	
	
	public void setStage(Stage stage) {
		this.stage = stage;		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {			
			@Override
			public void handle(WindowEvent arg0) {
				task.cancel();				
			}
		});	
	}
	
	
	public static String getUpdateMsg(String task, long read, long bytes, long startTime) {
		int per = Math.round(read / (float) bytes * 100);
		long millis = Math
				.round(((new Date().getTime() - startTime) / (float) read)
						* (bytes - read));
		int h = (int) ((millis / 1000) / 3600);
		int m = (int) (((millis / 1000) / 60) % 60);
		int s = (int) ((millis / 1000) % 60);
		return String.format("%s: %d%% read (%02d:%02d:%02d remaining)",task, per, h, m, s);
	}

	public void startTask(Task task) {
		this.task = task;
		progBar.progressProperty().bind(task.progressProperty());
		lblProgress.textProperty().bind(task.messageProperty());				
		btnCancel.disableProperty().bind(Bindings.not(task.runningProperty()));				
		stage.titleProperty().bind(task.titleProperty());
		
		
		task.stateProperty().addListener(new ChangeListener<State>() {
			@Override
			public void changed(ObservableValue<? extends State> observable, State oldState, State newState) {
				if (newState == State.SUCCEEDED) {					
					stage.hide();
				} else if (newState == State.CANCELLED){				
					stage.hide();
				}
			}			
		});
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();		
	}	

}
