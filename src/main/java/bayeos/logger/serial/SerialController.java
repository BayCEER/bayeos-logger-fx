package bayeos.logger.serial;

import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

/* 
 * Dialog to select a serial connection 
 */
public class SerialController {

	private Stage stage;
	private boolean connectClicked;
	
	public boolean isConnectClicked() {
		return connectClicked;
	}

	public void setConnectClicked(boolean connectClicked) {
		this.connectClicked = connectClicked;
	}

	@FXML 
	private ChoiceBox<String>portList;
	
		
	

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {					
			
	}

	/**
	 * Sets the stage of this dialog.
	 * @param dialogStage
	 */
	public void setParentStage(Stage stage) {
		this.stage = stage;
	}
	
	
	
	
	public void setPort(String port){
		portList.setValue(port);
	}
	
	public void setPorts(Set<String> ports){
		ObservableList<String> list = FXCollections.observableArrayList(ports);	
		portList.setItems(list);		
	}
	
	
	@FXML public void connectAction(ActionEvent event){
		connectClicked = true;
		stage.close();		
 	}
	
	@FXML
	private void cancelAction(ActionEvent event) {
		connectClicked = false;
		stage.close();
	}

	
	public String getPort() {		
		return portList.getValue();
	}
}
