package bayeos.logger;


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
